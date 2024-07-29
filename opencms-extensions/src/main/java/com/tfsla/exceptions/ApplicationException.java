package com.tfsla.exceptions;

/**
 * Una exception de la Aplicacion
 *  
 * @author lgassman
 */
public class ApplicationException extends RuntimeException {


	private static final long serialVersionUID = 7584103864008009877L;

	public ApplicationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ApplicationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ApplicationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public static void assertNotNull(Object object, String message) {
		assertTrue(object != null, message);
	}

	public static void assertTrue(boolean condition, String message) {
		if(!condition) {
			throw new ApplicationException(message);
		}
	}

	public static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}
}
