package com.tfsla.utils;

import org.opencms.configuration.CmsMedios;

@Deprecated
public class TfsAdminUserProvider {

	private String moduleName = "adminUser";

	private static TfsAdminUserProvider instance = new TfsAdminUserProvider();
	
	public static TfsAdminUserProvider getInstance()
	{
		return instance;
	}
	
	private TfsAdminUserProvider()
	{
		
	}

	//public String getPassword() {
	//	return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam("", "", moduleName,"password");
	//}

	//public String getUserName() {
	//	return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam("", "", moduleName,"userName");
	//}


}
