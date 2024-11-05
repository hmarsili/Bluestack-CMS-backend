package com.tfsla.cmsMedios.releaseManager.installer.common;

public class DeployReleaseRequest {
	
	public String getReleaseName() {
		return releaseName;
	}
	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}
	public ReleaseManagerConfiguration getConfig() {
		return config;
	}
	public void setConfig(ReleaseManagerConfiguration config) {
		this.config = config;
	}
	
	protected String releaseName;
	protected ReleaseManagerConfiguration config;
}