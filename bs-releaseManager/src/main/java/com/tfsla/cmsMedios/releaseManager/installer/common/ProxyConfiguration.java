package com.tfsla.cmsMedios.releaseManager.installer.common;

public class ProxyConfiguration {
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getWorkstation() {
		return workstation;
	}
	public void setWorkstation(String workstation) {
		this.workstation = workstation;
	}
	public String getNonProxyHosts() {
		return nonProxyHosts;
	}
	public void setNonProxyHosts(String nonProxyHosts) {
		this.nonProxyHosts = nonProxyHosts;
	}
	public Boolean getPreemptiveBasicAuth() {
		return preemptiveBasicAuth;
	}
	public void setPreemptiveBasicAuth(Boolean preemptiveBasicAuth) {
		this.preemptiveBasicAuth = preemptiveBasicAuth;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Boolean getUseProxy() {
		return useProxy;
	}
	public void setUseProxy(Boolean useProxy) {
		this.useProxy = useProxy;
	}
	String domain;
	String host;
	String password;
	String username;
	String workstation;
	String nonProxyHosts;
	Boolean preemptiveBasicAuth;
	Boolean useProxy = false;
	int port;
}
