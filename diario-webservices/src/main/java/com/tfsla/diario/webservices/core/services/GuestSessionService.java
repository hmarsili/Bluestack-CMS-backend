package com.tfsla.diario.webservices.core.services;

import javax.servlet.http.HttpServletRequest;

import com.tfsla.diario.webservices.core.GuestSessionManager;

public abstract class GuestSessionService extends TfsWebService {

	public GuestSessionService(HttpServletRequest request) throws Throwable {
		super(GuestSessionManager.checkForGuestSession(request));
	}
}
