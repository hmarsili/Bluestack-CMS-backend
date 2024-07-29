package com.tfsla.cdnIntegration.service.cdnConnector;

public abstract class A_ContentDeliveryNetwork implements I_ContentDeliveryNetwork {

	protected String module = "contentDeliveryNetwork";
	
	protected boolean isActive;
	protected int maxFilesToInvalidate;
	protected String user;
	protected String key;
	protected String name;
	protected int retries;
	
	public I_ContentDeliveryNetwork configure(
			boolean active,
			int maxFilesToInvalidate,
			String user,
			String key) {
		
		this.isActive = active;
		this.maxFilesToInvalidate = maxFilesToInvalidate;
		this.user = user;
		this.key = key;
		
		return this;
	}
	
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public int getMaxFilesToInvalidate() {
		return maxFilesToInvalidate;
	}
	
	public void setMaxFilesToInvalidate(int maxFilesToInvalidate) {
		this.maxFilesToInvalidate = maxFilesToInvalidate;
	}
	
	protected String getUser() {
		return user;
	}
	
	protected void setUser(String user) {
		this.user = user;
	}
	
	protected String getKey() {
		return key;
	}
	
	protected void setKey(String key) {
		this.key = key;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}


	public int getRetries() {
		return retries;
	}


	public void setRetries(int retries) {
		this.retries = retries;
	}
	
}
