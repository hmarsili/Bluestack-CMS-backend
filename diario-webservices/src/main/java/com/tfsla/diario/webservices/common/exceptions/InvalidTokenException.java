package com.tfsla.diario.webservices.common.exceptions;

public class InvalidTokenException extends Exception {

	private static final long serialVersionUID = -5404915799822737637L;

	public InvalidTokenException(String message) {
		super(message);
	}
	
	public InvalidTokenException() {
		super("The token provided is invalid or has expired");
	}
	
}
