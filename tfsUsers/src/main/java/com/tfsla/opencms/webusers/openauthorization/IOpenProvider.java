package com.tfsla.opencms.webusers.openauthorization;

import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfiguration;

public interface IOpenProvider {
	String getProviderName();
	String GetLoginUrl() throws Exception;
	UserProfileData GetUserProfileData() throws Exception;
	ProviderConfiguration getConfiguration() throws Exception;
	Object getProviderData() throws Exception;
}
