package com.tfsla.cmsMedios.releaseManager.github.service.core;

import com.tfsla.cmsMedios.releaseManager.common.ConnectorConfiguration;

public abstract class GithubService extends GithubComponent {
	public GithubService(ConnectorConfiguration config) {
		super(config);
		this.connector = new GithubConnector(config);
	}
	
	protected GithubConnector connector;
}
