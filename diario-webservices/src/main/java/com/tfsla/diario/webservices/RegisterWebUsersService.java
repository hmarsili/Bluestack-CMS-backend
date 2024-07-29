package com.tfsla.diario.webservices;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.interfaces.IRegisterWebUserService;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.GuestSessionManager;
import com.tfsla.diario.webservices.core.services.TfsWebService;
import com.tfsla.diario.webservices.helpers.UserJSONHelper;
import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.webusersposts.helper.AdminCmsObjectHelper;

public class RegisterWebUsersService extends TfsWebService implements IRegisterWebUserService {

	private String publication;
	private String siteName;
	
	public RegisterWebUsersService(HttpServletRequest request) throws Throwable {
		super(GuestSessionManager.checkForGuestSession(request, null, null));
		String stringRequest = ServiceHelper.getRequestAsString(request);
		this.jsonRequest = JSONObject.fromObject(stringRequest);
		
		this.publication = request.getParameter("publication");
		this.siteName = request.getParameter("siteName");
	}

	@Override
	protected JSON doExecute() throws Throwable {
		
		UserJSONHelper helper = new UserJSONHelper();
		
		RegistrationModule regModule = null;
		
		if(this.siteName!=null && this.publication!=null)
			regModule = RegistrationModule.getInstance(this.siteName, this.publication);
		else
			regModule = RegistrationModule.getInstance(getAdminCmsObject(null));
		
		JSONObject ret = new JSONObject();
		
		try {
			CmsUser user = helper.createCmsUserFromJSON(this.jsonRequest, getAdminCmsObject(this.siteName), regModule, false, this.publication);
			ret.put(StringConstants.USERNAME, user.getName());
			ret.put(StringConstants.USERID, user.getId().toString());
			ret.put(StringConstants.STATUS, StringConstants.OK);
		} catch(Exception e) {
			ret.put(StringConstants.STATUS, StringConstants.ERROR);
			ret.put(StringConstants.MESSAGE, e.getMessage());
		}
		
		return ret;
	}
	
	protected static synchronized CmsObject getAdminCmsObject(String siteName) throws CmsException {
		if(adminCmsObject == null) {
			adminCmsObject = AdminCmsObjectHelper.getAdminCmsObject();
			
			if(siteName!=null) {
				adminCmsObject.getRequestContext().setSiteRoot(siteName);
				adminCmsObject.getRequestContext().setUri(siteName);
			}else
				adminCmsObject.getRequestContext().setSiteRoot("/");
			
			adminCmsObject.getRequestContext().setCurrentProject(adminCmsObject.readProject(StringConstants.OFFLINE));
		}
		return adminCmsObject;
	}
	
	protected JSONObject jsonRequest;
	protected static CmsObject adminCmsObject;
}
