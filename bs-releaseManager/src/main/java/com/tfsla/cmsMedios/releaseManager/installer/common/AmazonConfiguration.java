package com.tfsla.cmsMedios.releaseManager.installer.common;

public class AmazonConfiguration {
	public AmazonConfiguration() {
		this.proxyConfiguration = new ProxyConfiguration();
	}
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	public String getAccessID() {
		return accessID;
	}
	public void setAccessID(String accessID) {
		this.accessID = accessID;
	}
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getReleasesDirectory() {
		return releasesDirectory;
	}
	public void setReleasesDirectory(String releasesDirectory) {
		this.releasesDirectory = releasesDirectory;
	}
	public Boolean useProxy() {
		return proxyConfiguration.useProxy;
	}
	public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
		this.proxyConfiguration = proxyConfiguration;
	}
	public ProxyConfiguration getProxyConfiguration() {
		return this.proxyConfiguration;
	}
	String accessID;
	String accessKey;
	String region;
	String bucket;
	String releasesDirectory;
	ProxyConfiguration proxyConfiguration;
}
