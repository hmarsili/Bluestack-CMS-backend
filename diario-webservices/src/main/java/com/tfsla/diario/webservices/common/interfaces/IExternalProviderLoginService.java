package com.tfsla.diario.webservices.common.interfaces;

import com.tfsla.diario.webservices.common.Token;

public interface IExternalProviderLoginService {

	Token loginAndRegister() throws Throwable;
}
