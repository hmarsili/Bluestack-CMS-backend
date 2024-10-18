package com.tfsla.diario.webservices;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsUser;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.interfaces.IUsersAddService;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.OfflineProjectService;
import com.tfsla.diario.webservices.helpers.AuditPermissionsHelper;
import com.tfsla.diario.webservices.helpers.UserJSONHelper;
import com.tfsla.opencms.webusers.RegistrationModule;

public class UsersAddService extends OfflineProjectService implements IUsersAddService {

	public UsersAddService(HttpServletRequest request) throws Throwable {
		super(request);
		String stringRequest = ServiceHelper.getRequestAsString(request);
		JSONObject jsonRequest = JSONObject.fromObject(stringRequest);
		this.requestItems = jsonRequest.getJSONArray(StringConstants.DATA);
	}

	@Override
	protected JSON doExecute() throws Throwable {
		AuditPermissionsHelper.checkUserPermission(cms, StringConstants.PERMISSION_CREATE, StringConstants.PERMISSION_MODULE_USERS);
		
		UserJSONHelper helper = new UserJSONHelper();
		JSONArray jsonResponse = new JSONArray();
		RegistrationModule regModule = RegistrationModule.getInstance(cms);
		
		try {
			this.switchToOfflineSession();
			
			for(int i=0; i<requestItems.size(); i++) {
				JSONObject item = new JSONObject();
				try {
					JSONObject jsonUser = requestItems.getJSONObject(i);
					CmsUser user = helper.createCmsUserFromJSON(jsonUser, cms, regModule);
					
					item.put(StringConstants.USERNAME, user.getName());
					item.put(StringConstants.USERID, user.getId().toString());
					item.put(StringConstants.STATUS, StringConstants.OK);
					item.put(StringConstants.INDEX, i);
				} catch(Exception e) {
					item.put(StringConstants.STATUS, StringConstants.ERROR);
					item.put(StringConstants.INDEX, i);
					item.put(StringConstants.MESSAGE, e.getMessage());
				}
				jsonResponse.add(item);
			}
		} catch(Exception e) {
			JSONObject item = new JSONObject();
			item.put(StringConstants.STATUS, StringConstants.ERROR);
			item.put(StringConstants.MESSAGE, e.getMessage());
			jsonResponse.add(item);
		} finally {
			this.restoreSession();
		}
				
		return jsonResponse;
	}

	protected String getJsonStringValue(String key, JSONObject jsonObject) {
		if(jsonObject.containsKey(key))
			return jsonObject.getString(key);
		return "";
	}
	
	protected JSONArray requestItems;
}