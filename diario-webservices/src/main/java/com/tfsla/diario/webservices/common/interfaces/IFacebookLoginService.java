package com.tfsla.diario.webservices.common.interfaces;

import com.tfsla.diario.webservices.common.Token;

public interface IFacebookLoginService {
	
	/**
	 * Authenticates facebook users by checking their facebook access tokens
	 * @param userToken facebook user token
	 * @return Token issued for the current WP session
	 */
	Token login(String userToken) throws Throwable;
	
	/**
	 * Authenticates facebook users by checking their facebook access tokens
	 * and signs up an account into the CMS
	 * @param userToken facebook user token
	 * @param email user email to create a new Account
	 * @return Token issued for the current WP session
	 * @throws Throwable
	 */
	Token loginAndRegister(String userToken, String email) throws Throwable;
}
