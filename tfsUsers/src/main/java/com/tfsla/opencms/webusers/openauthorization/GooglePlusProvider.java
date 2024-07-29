package com.tfsla.opencms.webusers.openauthorization;

import javax.servlet.http.HttpServletRequest;

public class GooglePlusProvider extends GoogleProvider implements IOpenProvider {
	protected final String providerName = "googlePlus";
	
	public String getProviderName() {
		return this.providerName;
	}
	
	@Override
	protected String getModuleName() {
		return "webusers-googlePlus";
	}
	
	public GooglePlusProvider(HttpServletRequest request) throws Exception {
		super(request);
	}
}