package com.tfsla.diario.webservices.common.interfaces;

import com.tfsla.diario.webservices.common.Token;
import com.tfsla.diario.webservices.common.WebSession;
import com.tfsla.diario.webservices.common.exceptions.DisabledUserException;
import com.tfsla.diario.webservices.common.exceptions.InvalidLoginException;
import com.tfsla.diario.webservices.common.exceptions.InvalidTokenException;
import com.tfsla.diario.webservices.common.exceptions.TokenExpiredException;

/**
 * Represents the AuthorizationService interface
 */
public interface IAuthorizationService {
	
	/**
	 * Retrieves a token to be associated to an active web session
	 * @param username CMS username to be authenticated
	 * @param password password for the username provided
	 * @return an issued token to be managed as a web session
	 * @throws InvalidLoginException if the credentials provided are incorrect
	 * or if there is any other problem during the authentication
	 * @throws DisabledUserException 
	 */
	Token requestToken(String username, String password) throws InvalidLoginException, DisabledUserException;
	
	/**
	 * Checks the status of a token to see if it is still valid or has already expired
	 * @param token the token to be verified
	 * @return a WebSession instance retrieved from the Session Pool
	 * @throws TokenExpiredException if the token is expired
	 * @throws InvalidTokenException if the token is invalid
	 */
	WebSession checkToken(String token) throws TokenExpiredException, InvalidTokenException;
}
