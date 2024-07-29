package com.tfsla.diario.webservices.common.interfaces;

import net.sf.json.JSON;

import com.tfsla.diario.webservices.common.exceptions.InvalidTokenException;
import com.tfsla.diario.webservices.common.exceptions.TokenExpiredException;

/**
 * Represents a web service provided within the cms-medios core framework
 */
public interface ITfsWebService {
	
	/**
	 * Executes the web service call and retrieves the response in JSON format
	 * @return JSON response
	 * @throws TokenExpiredException if the session token is expired
	 * @throws InvalidTokenException if the session token is invalid
	 * @throws Throwable 
	 */
	JSON execute() throws TokenExpiredException, InvalidTokenException, Throwable;
	
}
