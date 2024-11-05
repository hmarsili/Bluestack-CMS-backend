package com.tfsla.cmsMedios.releaseManager.installer.common;

public class TomcatManagerConfiguration {
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAppEndpoint() {
		return appEndpoint;
	}
	public void setAppEndpoint(String appEndpoint) {
		this.appEndpoint = appEndpoint;
	}
	String username;
	String password;
	String appEndpoint = "http://localhost:8080/manager/";
}