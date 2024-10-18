package com.tfsla.diario.webservices;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import net.sf.json.JSONArray;

import com.tfsla.diario.webservices.common.ContentsHelper;
import com.tfsla.diario.webservices.common.interfaces.INewsAddService;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.*;

public class NewsAddService extends NewsManagerService implements INewsAddService {

	public NewsAddService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	@Override
	protected CmsFile getCmsFile(JSONArray noticia, CmsObject cmsObject) throws Exception {
		if (folderDate==null)
			return ContentsHelper.createResource(cmsObject, this.newsType, this.publication);
		else 
			return ContentsHelper.createResource(cmsObject, this.newsType, this.publication,folderDate);
	}
	
	@Override
	protected void onItemError(String resourceName) {
		if(resourceName != null && !resourceName.equals("")) {
			try {
				cms.deleteResource(resourceName, CmsResource.DELETE_REMOVE_SIBLINGS);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	protected int getPermissionRequired() {
		return StringConstants.PERMISSION_CREATE;
	}
}