package com.tfsla.opencms.exceptions;


@SuppressWarnings("serial")
public class ProgramException extends RuntimeException {


	public ProgramException(String message, Exception e) {
		super(message, e);
	}

	private ProgramException() {
	}

	public ProgramException(String message) {
		super(message);
	}

	public static ProgramException wrap(String message, Exception e) {
		return new ProgramException(message, e);
	}

	public static void assertTrue(String message, boolean expression) {
		if (!expression) {
			throw new ProgramException(message);
		}
	}
}
