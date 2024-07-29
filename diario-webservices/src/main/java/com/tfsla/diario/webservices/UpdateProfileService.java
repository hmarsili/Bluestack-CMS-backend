package com.tfsla.diario.webservices;

import javax.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.interfaces.IUpdateProfileService;
import com.tfsla.diario.webservices.common.interfaces.IUsersEditService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.OfflineProjectService;
import com.tfsla.diario.webservices.helpers.UserJSONHelper;
import com.tfsla.opencms.webusers.RegistrationModule;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class UpdateProfileService extends OfflineProjectService implements IUpdateProfileService {

	protected JSONObject requestItem;
	
	public UpdateProfileService(HttpServletRequest request) throws Throwable {
		super(request);
		String stringRequest = ServiceHelper.getRequestAsString(request);
		JSONObject jsonRequest = JSONObject.fromObject(stringRequest);
		this.requestItem = jsonRequest.getJSONObject(StringConstants.DATA);
	}
	
	protected String getJsonStringValue(String key, JSONObject jsonObject) {
		if(jsonObject.containsKey(key))
			return jsonObject.getString(key);
		return "";
	}
	
	@Override
	protected JSON doExecute() throws Throwable {
	
		UserJSONHelper helper = new UserJSONHelper();
		RegistrationModule regModule = RegistrationModule.getInstance(cms);
		
		
		CmsUser user = helper.getCmsUserFromJSON(requestItem, cms);
		 
		if (!user.getId().toString().equals(cms.getRequestContext().currentUser().getId().toString()))
				throw new Exception(ExceptionMessages.ERROR_PROFILE_NOT_FROM_USER);
		
		JSONObject item = new JSONObject();
		try {
			this.switchToOfflineSession();
			
			user = helper.updateCmsUserFromJSON(requestItem, cms, regModule);
			
			item.put(StringConstants.USERNAME, user.getName());
			item.put(StringConstants.USERID, user.getId().toString());
			item.put(StringConstants.STATUS, StringConstants.OK);
		} catch(Exception e) {
			item.put(StringConstants.STATUS, StringConstants.ERROR);
			item.put(StringConstants.MESSAGE, e.getMessage());
			e.printStackTrace();
		} finally {
			this.restoreSession();
		}
		
		return item;
	}

}
