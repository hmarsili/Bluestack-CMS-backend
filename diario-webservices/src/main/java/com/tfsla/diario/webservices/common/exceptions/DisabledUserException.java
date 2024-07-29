package com.tfsla.diario.webservices.common.exceptions;

public class DisabledUserException extends Exception {

	private static final long serialVersionUID = -633118889969673032L;

	public DisabledUserException() {
		super("The user is disabled");
	}
	
}
