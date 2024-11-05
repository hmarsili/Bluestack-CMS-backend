package com.tfsla.cmsMedios.releaseManager.installer.service;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

public abstract class BaseConfigurationService {
	
	public BaseConfigurationService(CmsObject cmsObject) throws Exception {
		this.site = cmsObject.getRequestContext().getSiteRoot();
		this.publication = String.valueOf(SitePublicationService.getPublicationId(cmsObject));
	}
	
	public Boolean hasValue(String paramName) {
		String stringVal = this.getStringParameter(paramName);
		return stringVal != null && !stringVal.equals("");
	}
	
	
	protected Boolean getBooleanParameter(String paramName) {
		try {
			return _config.getBooleanParam(site, publication, this.getModuleName(), paramName);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected Integer getIntegerParameter(String paramName) {
		try {
			return _config.getIntegerParam(site, publication, this.getModuleName(), paramName);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	protected String getStringParameter(String paramName) {
		try {
			return _config.getParam(site, publication, this.getModuleName(), paramName);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	protected abstract String getModuleName();
	
	protected static CPMConfig _config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	protected CmsObject cmsObject;
	protected String site;
	protected String publication;
}
