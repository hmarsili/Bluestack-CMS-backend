package com.tfsla.cmsMedios.releaseManager.installer.service;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

import com.tfsla.cmsMedios.releaseManager.installer.common.TomcatManagerConfiguration;

//TODO: make this class extend BaseConfigurationService
public class TomcatManagerConfigurationService {
	/**
	 * Returns a dummy TomcatManagerConfiguration entity filled up with Tomcat Manager configuration parameters
	 * @param cmsObject current session CmsObject
	 * @return an TomcatManagerConfiguration instance with parameters to manage a Tomcat App
	 * @throws Exception
	 */
	public static TomcatManagerConfiguration getConfiguration(CmsObject cmsObject) throws Exception {
		String site = cmsObject.getRequestContext().getSiteRoot();
		String publication = String.valueOf(SitePublicationService.getPublicationId(cmsObject));
		
		TomcatManagerConfiguration config = new TomcatManagerConfiguration();
		config.setUsername(_config.getParam(site, publication, MODULE_NAME, "tomcatManagerUsername"));
		config.setPassword(_config.getParam(site, publication, MODULE_NAME, "tomcatManagerPassword"));
		config.setAppEndpoint(_config.getParam(site, publication, MODULE_NAME, "tomcatManagerAppEndpoint"));
		
		return config;
	}
	
	private static CPMConfig _config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private static final String MODULE_NAME = "releaseManager";
}
