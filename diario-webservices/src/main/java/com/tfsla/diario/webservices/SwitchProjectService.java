package com.tfsla.diario.webservices;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsProject;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.interfaces.ISwitchProjectService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.TfsWebService;

public class SwitchProjectService extends TfsWebService implements ISwitchProjectService {

	public SwitchProjectService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	@Override
	protected JSON doExecute() throws Throwable {
		if(!this.configuration.getAllowProjectSwitch()) {
			throw new Exception(String.format(ExceptionMessages.ERROR_PROJECT_SWITCH_NOT_ALLOWED, 
					this.cms.getRequestContext().getSiteRoot(), 
					this.getCurrentPublication()
				)
			);
		}
		
		String project = this.assertRequestParameter(StringConstants.PROJECT);
		CmsProject cmsProject = this.cms.readProject(project);
		if(cmsProject == null) {
			throw new Exception(String.format(ExceptionMessages.ERROR_INVALID_PROJECT, project));
		}
		this.cms.getRequestContext().setCurrentProject(cmsProject);
		
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put(StringConstants.STATUS, StringConstants.OK);
		jsonResponse.put(StringConstants.PROJECT, cms.getRequestContext().currentProject().getName());
		return jsonResponse;
	}
}
