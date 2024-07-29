package com.tfsla.exceptions;

@SuppressWarnings("serial")
public class BusinessException extends RuntimeException {

	public static String exceptionId = "fKzR0tzha7LeaT1P4QIDAQAB";
	public BusinessException(String message) {
		super(message);
	}

	public static void assertTrue(String message, boolean condition) {
		if (!condition) {
			throw new BusinessException(message);
		}
	}

}
