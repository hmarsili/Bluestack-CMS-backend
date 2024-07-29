package com.tfsla.opencms.webusers.openauthorization.common.exceptions;

public class InvalidConfigurationException extends Exception {
	private String moduleName;
	
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	private static final long serialVersionUID = 1L;
}
