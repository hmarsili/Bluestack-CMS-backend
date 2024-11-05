package com.tfsla.cmsMedios.releaseManager.installer.service;

import org.opencms.file.CmsObject;

import com.tfsla.cmsMedios.releaseManager.installer.common.ProxyConfiguration;

/**
 * Provides services for configuring remote networks accessing by passing through Proxy servers.
 * Encapsulates CmsMedios.xml configuration processing with dummy entities
 * @see com.tfsla.cmsMedios.releaseManager.installer.common.ProxyConfiguration
 */
public class ProxyConfigurationService extends BaseConfigurationService {
	
	public ProxyConfigurationService(CmsObject cmsObject) throws Exception {
		super(cmsObject);
	}
	
	/**
	 * Returns a dummy ProxyConfiguration entity filled up with Proxy configuration parameters
	 * @return a ProxyConfiguration instance with parameters to access remote networks through a proxy server
	 * @throws Exception
	 */
	public ProxyConfiguration getConfiguration() throws Exception {
		Boolean useProxy = false;
		ProxyConfiguration config = new ProxyConfiguration();
		if (this.hasValue("domain")) {
			config.setDomain(this.getStringParameter("domain"));
			useProxy = true;
		}
		if (this.hasValue("host")) {
			config.setHost(this.getStringParameter("host"));
			useProxy = true;
		}
		if (this.hasValue("nonProxyHosts")) {
			config.setNonProxyHosts(this.getStringParameter("nonProxyHosts"));
			useProxy = true;
		}
		if (this.hasValue("password")) {
			config.setPassword(this.getStringParameter("password"));
			useProxy = true;
		}
		if (this.hasValue("username")) {
			config.setUsername(this.getStringParameter("username"));
			useProxy = true;
		}
		if (this.hasValue("workstation")) {
			config.setWorkstation(this.getStringParameter("workstation"));
			useProxy = true;
		}
		if (this.hasValue("port")) {
			config.setPort(this.getIntegerParameter("port"));
			useProxy = true;
		}
		if (this.hasValue("preemptiveBasicAuth")) {
			config.setPreemptiveBasicAuth(this.getBooleanParameter("preemptiveBasicAuth"));
			useProxy = true;
		}
		config.setUseProxy(useProxy);
		return config;
	}
	
	@Override
	protected String getModuleName() {
		return "proxy";
	}
}