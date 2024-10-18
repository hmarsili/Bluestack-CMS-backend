package com.tfsla.diario.webservices.common.interfaces;

import jakarta.servlet.http.HttpServletRequest;

public interface IRegisterPushClientService {

	/**
	 * Registers a client to receive push notification services. Will be associated to
	 * the default site and publication
	 * @param token id of the client to be registered
	 * @param email optional, from the client
	 * @param additionalInfo optional, from the client
	 * @param platform the platform where the user comes from (android, apple, etc)
	 * @param request current servlet request
	 * @throws Exception
	 */
	void register(String token, String email, String platform, String topic, String additionalInfo, HttpServletRequest request) throws Exception;
	
	/**
	 * Registers a client to receive push notification services
	 * @param token id of the client to be registered
	 * @param email optional, from the client
	 * @param additionalInfo optional, from the client
	 * @param platform the platform where the user comes from (android, apple, etc)
	 * @param site the site where the user requested to register
	 * @param publication the publication where the user requested to register
	 * @throws Exception
	 */
	void register(String token, String email, String platform, String topic, String additionalInfo, String site, int publication) throws Exception;
}
