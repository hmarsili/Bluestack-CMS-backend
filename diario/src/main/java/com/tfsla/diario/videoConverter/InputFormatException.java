package com.tfsla.diario.videoConverter;

/**
 * This expection is thrown if a source file format is not recognized.
 */
public class InputFormatException extends EncoderException {

	private static final long serialVersionUID = 1L;

	InputFormatException() {
		super();
	}

	InputFormatException(String message) {
		super(message);
	}

}

