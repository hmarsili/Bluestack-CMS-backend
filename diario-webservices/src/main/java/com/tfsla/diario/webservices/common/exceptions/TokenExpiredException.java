package com.tfsla.diario.webservices.common.exceptions;

public class TokenExpiredException extends Exception {

	private static final long serialVersionUID = -3615896180031772485L;

	public TokenExpiredException() {
		super("The token provided has expired");
	}
	
}
