package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.util.ArrayList;
import java.util.List;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

import com.tfsla.cmsMedios.releaseManager.installer.common.ReleaseManagerConfiguration;

/**
 * Provides services for accessing Release Manager configuration parameters.
 * Encapsulates CmsMedios.xml configuration processing with dummy entities
 * @see com.tfsla.cmsMedios.releaseManager.installer.common.ReleaseManagerConfiguration
 */
//TODO: make this class extend BaseConfigurationService
public class ReleaseManagerConfigurationService {
	
	/**
	 * Returns a dummy ReleaseManagerConfiguration entity filled up with Release Manager configuration parameters
	 * @param cmsObject current session CmsObject
	 * @return an ReleaseManagerConfiguration instance with parameters needed for deploying releases
	 * @throws Exception
	 */
	public static synchronized ReleaseManagerConfiguration getConfiguration(CmsObject cmsObject) throws Exception {
		String site = cmsObject.getRequestContext().getSiteRoot();
		String publication = String.valueOf(SitePublicationService.getPublicationId(cmsObject));
		
		ReleaseManagerConfiguration config = new ReleaseManagerConfiguration();
		config.setAmazonConfiguration(AmazonConfigurationService.getConfiguration(cmsObject));
		config.setTomcatManagerConfiguration(TomcatManagerConfigurationService.getConfiguration(cmsObject));
		config.setConfigDir(_config.getParam(site, publication, MODULE_NAME, "configurationDirectory"));
		config.setJarsDir(_config.getParam(site, publication, MODULE_NAME, "jarsDirectory"));
		config.setReleasesDirectory(_config.getParam(site, publication, MODULE_NAME, "releasesDirectory"));
		config.setTempDir(_config.getParam(site, publication, MODULE_NAME, "tempDirectory"));
		config.setModulesAvailableDir(_config.getParam(site, publication, MODULE_NAME, "modulesAvailableDirectory"));
		config.setModulesEnabledDir(_config.getParam(site, publication, MODULE_NAME, "modulesEnabledDirectory"));
		List<String> customizedFiles = _config.getParamList(site, publication, MODULE_NAME, "customizedFiles");
		if(customizedFiles == null) {
			customizedFiles = new ArrayList<String>();
		}
		config.setCustomizedFiles(customizedFiles);
		return config;
	}
	
	private static CPMConfig _config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private static final String MODULE_NAME = "releaseManager";
}
