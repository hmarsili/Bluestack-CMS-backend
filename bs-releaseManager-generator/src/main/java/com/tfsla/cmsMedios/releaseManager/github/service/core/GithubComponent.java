package com.tfsla.cmsMedios.releaseManager.github.service.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tfsla.cmsMedios.releaseManager.common.ConnectorConfiguration;
import com.tfsla.cmsMedios.releaseManager.github.common.Strings;

public abstract class GithubComponent {
	public GithubComponent(ConnectorConfiguration config) {
		this.config = config;
		this.log = LogFactory.getLog(this.getClass());
		this.log.debug(String.format(Strings.SERVICE_INIT, 
			this.getClass().getName(), 
			config.getRepo(), 
			config.getOwner(),
			config.getToken(),
			config.getVersion()
		));
	}
	
	protected Log log;
	protected ConnectorConfiguration config;
}