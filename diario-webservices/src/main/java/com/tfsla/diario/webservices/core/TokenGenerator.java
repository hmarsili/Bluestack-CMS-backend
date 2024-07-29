package com.tfsla.diario.webservices.core;

import java.util.UUID;

import com.tfsla.diario.webservices.common.Token;

/**
 * Generates tokens to manage web sessions
 */
public final class TokenGenerator {
	
	/**
	 * Sets tokens duration in seconds
	 * @param tokenDuration tokens duration in seconds
	 */
	public static void setTokenDuration(int tokenDuration) {
		if(tokenDuration > 0) {
			TokenGenerator.tokenDuration = tokenDuration;
		}
	}
	
	/**
	 * Generates a new issued token
	 * @return returns a new Token
	 */
	public static Token getToken() {
		Token token = new Token() {{
			setValue(UUID.randomUUID().toString());
			setDuration(TokenGenerator.tokenDuration);
		}};
		return token;
	}
	
	/**
	 * Generates a new issued token
	 * @param duration token duration
	 * @return returns a new Token
	 */
	public static Token getToken(final long duration) {
		Token token = new Token() {{
			setValue(UUID.randomUUID().toString());
			setDuration(duration);
		}};
		return token;
	}
	
	private static int tokenDuration = 60;
}