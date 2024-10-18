package com.tfsla.diario.webservices.common.interfaces;

import jakarta.servlet.http.HttpServletRequest;

public interface IUnsubscribePushClientService {

	/**
	 * Removes a client from the push notification services
	 * @param token id of the client to be unsubscribed
	 * @param platform the platform of the service (android, apple, etc)
	 * @throws Exception
	 */
	void unsubscribe(String token, String platform) throws Exception;
	
	void unsubscribe(String token, String platform, String topic, HttpServletRequest request) throws Exception;
	void unsubscribe(String token, String platform, String topic, String site, int publication) throws Exception;
}
