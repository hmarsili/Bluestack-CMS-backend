package com.tfsla.templateManager.service;

public class ConfigurationTemplateItemProcessor {
	private String containerName;
	private Boolean hide;
	private String configuration;
	
	public ConfigurationTemplateItemProcessor()
	{
		
	}	
		
	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}	
	
	public void setHide(Boolean hide) {
		this.hide = hide;
	}
	
	public Boolean getHide() {
		return hide;
	}
	
	public String getContainerName() {
		return containerName;
	}	
	
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}		
}
