package com.tfsla.opencms.webusers.openauthorization.common.exceptions;

public class InvalidConversionException extends Exception {
	
	private String valueConverterType;
	private Object value;
	private Exception innerException;
	private static final long serialVersionUID = 1L;
	
	public InvalidConversionException() {
		super("Cannot convert the value by using the converter provided");
	}
	
	public String getValueConverterType() {
		return valueConverterType;
	}
	
	public void setValueConverterType(String valueConverterType) {
		this.valueConverterType = valueConverterType;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public Exception getInnerException() {
		return innerException;
	}
	
	public void setInnerException(Exception innerException) {
		this.innerException = innerException;
	}
	
}
