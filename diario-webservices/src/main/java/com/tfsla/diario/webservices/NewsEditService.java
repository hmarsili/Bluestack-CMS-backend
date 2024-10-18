package com.tfsla.diario.webservices;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import com.tfsla.diario.webservices.common.interfaces.INewsEditService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.NewsManagerService;
import com.tfsla.diario.webservices.helpers.VFSUnlockerHelper;

public class NewsEditService extends NewsManagerService implements INewsEditService  {

	public NewsEditService(HttpServletRequest request) throws Throwable {
		super(request);
		
		this.skipParameters = new ArrayList<String>();
		this.skipParameters.add("url");
	}

	@Override
	protected CmsFile getCmsFile(JSONArray noticia, CmsObject cmsObject) throws Exception {
		String url = null;
		for(int index=0; index<noticia.size(); index++) {
			JSONObject itemNoticia = noticia.getJSONObject(index);
			if(!itemNoticia.containsKey("url")) continue;
			url = itemNoticia.getString("url");
		}
		if(url == null || url.equals("")) {
			throw new Exception(String.format(ExceptionMessages.MISSING_OR_EMPTY_PARAMETER, "url"));
		}
		
		VFSUnlockerHelper.stealLock(cmsObject, url);
		
		CmsFile file = cmsObject.readFile(url);
		
		this.assertUserPermission(file);
		
		return file;
	}
	
	private void assertUserPermission(CmsFile file) throws Exception {
		//If has an upper permission, don't need this check
		if(this.permissionLevel == StringConstants.PERMISSION_EDIT) {
			CmsUser creator = cms.readUser(file.getUserCreated());
			String currentUser = cms.getRequestContext().currentUser().getName();
			if(!currentUser.equals(creator.getName())) {
				throw new Exception(String.format(ExceptionMessages.ERROR_USER_NOT_CREATOR, currentUser));
			}
		}
	}
	
	@Override
	protected int getPermissionRequired() {
		return StringConstants.PERMISSION_EDIT;
	}
}
