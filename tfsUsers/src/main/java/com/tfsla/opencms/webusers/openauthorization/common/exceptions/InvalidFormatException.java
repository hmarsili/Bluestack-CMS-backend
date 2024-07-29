package com.tfsla.opencms.webusers.openauthorization.common.exceptions;

public class InvalidFormatException extends Exception {

	private String format;
	private static final long serialVersionUID = 1L;
	
	public InvalidFormatException() {
		super("The format provided is not recognized as a valid format to be parsed");
	}
	
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}

}
