package com.tfsla.cmsMedios.releaseManager.installer.service;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

import com.tfsla.cmsMedios.releaseManager.installer.common.AmazonConfiguration;

/**
 * Provides services for accessing Amazon configuration parameters.
 * Encapsulates CmsMedios.xml configuration processing with dummy entities
 * @see com.tfsla.cmsMedios.releaseManager.installer.common.AmazonConfiguration
 */
//TODO: make this class extend BaseConfigurationService
public class AmazonConfigurationService {
	
	/**
	 * Returns a dummy AmazonConfiguration entity filled up with Amazon configuration parameters
	 * @param cmsObject current session CmsObject
	 * @return an AmazonConfiguration instance with parameters to integrate with Amazon S3
	 * @throws Exception
	 */
	public static AmazonConfiguration getConfiguration(CmsObject cmsObject) throws Exception {
		String site = cmsObject.getRequestContext().getSiteRoot();
		String publication = String.valueOf(SitePublicationService.getPublicationId(cmsObject));
		
		AmazonConfiguration amzConfig = new AmazonConfiguration();
		amzConfig.setAccessID(_config.getParam(site, publication, MODULE_NAME, "amazonAccessID"));
		amzConfig.setAccessKey(_config.getParam(site, publication, MODULE_NAME, "amazonAccessKey"));
		amzConfig.setBucket(_config.getParam(site, publication, MODULE_NAME, "amazonBucket"));
		amzConfig.setRegion(_config.getParam(site, publication, MODULE_NAME, "amazonRegion"));
		amzConfig.setReleasesDirectory(_config.getParam(site, publication, MODULE_NAME, "amazonReleasesDirectory"));
		
		ProxyConfigurationService proxyService = new ProxyConfigurationService(cmsObject);
		amzConfig.setProxyConfiguration(proxyService.getConfiguration());
		
		return amzConfig;
	}
	
	private static CPMConfig _config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private static final String MODULE_NAME = "releaseManager";
}