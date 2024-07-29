package com.tfsla.opencms.webusers.openauthorization.common.exceptions;

public class InvalidPropertyException extends Exception {
	
	private Exception innerException;
	private String propertyName;
	private Object value;
	private static final long serialVersionUID = 1L;
	
	public InvalidPropertyException() {
		super("Cannot set property in UserProfileData instance");
	}
	
	public Exception getInnerException() {
		return innerException;
	}

	public void setInnerException(Exception innerException) {
		this.innerException = innerException;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
