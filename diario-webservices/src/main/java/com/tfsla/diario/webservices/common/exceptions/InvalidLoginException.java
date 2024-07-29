package com.tfsla.diario.webservices.common.exceptions;

public class InvalidLoginException extends Exception {

	private static final long serialVersionUID = -6007239450209824732L;

	public InvalidLoginException() {
		super("Invalid username or password");
	}
	
}
