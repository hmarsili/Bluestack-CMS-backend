package com.tfsla.diario.webservices.common;

import java.util.List;

public class WebServicesConfiguration {
	private int tokensDuration;
	private String guestToken;
	private String defaultPublication;
	private String defaultSite;
	private List<String> usersHiddenFields;
	private List<String> imageExtensions;
	private List<String> videoExtensions;
	private Boolean allowProjectSwitch;
	
	public WebServicesConfiguration() {
		allowProjectSwitch = false;
	}
	public int getTokensDuration() {
		return tokensDuration;
	}
	public void setTokensDuration(int tokensDuration) {
		this.tokensDuration = tokensDuration;
	}
	public List<String> getUsersHiddenFields() {
		return usersHiddenFields;
	}
	public void setUsersHiddenFields(List<String> usersHiddenFields) {
		this.usersHiddenFields = usersHiddenFields;
	}
	public List<String> getImageExtensions() {
		return imageExtensions;
	}
	public void setImageExtensions(List<String> imageExtensions) {
		this.imageExtensions = imageExtensions;
	}
	public List<String> getVideoExtensions() {
		return videoExtensions;
	}
	public void setVideoExtensions(List<String> videoExtensions) {
		this.videoExtensions = videoExtensions;
	}
	public String getGuestToken() {
		return guestToken;
	}
	public void setGuestToken(String guestToken) {
		this.guestToken = guestToken;
	}
	public Boolean getAllowProjectSwitch() {
		return allowProjectSwitch;
	}
	public void setAllowProjectSwitch(Boolean allowProjectSwitch) {
		this.allowProjectSwitch = allowProjectSwitch;
	}
	public String getDefaultSite() {
		return defaultSite;
	}
	public String getDefaultPublication() {
		return defaultPublication;
	}
	public void setDefaultSite(String defaultSite) {
		this.defaultSite = defaultSite;
	}
	public void setDefaultPublication(String defaultPublication) {
		this.defaultPublication =  defaultPublication;
	}
	
}
