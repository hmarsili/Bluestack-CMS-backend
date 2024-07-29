package com.tfsla.utils;

import java.io.IOException;
import java.util.Properties;


public class TfsPreviewUserProvider {

	private String userName = "";
	private String password = "";

	private static TfsPreviewUserProvider instance = new TfsPreviewUserProvider();
	
	public static TfsPreviewUserProvider getInstance()
	{
		return instance;
	}
	
	private TfsPreviewUserProvider()
	{
		Properties properties = new Properties();
		try {
			properties.load(TfsPreviewUserProvider.class.getResourceAsStream("TfsPreviewUserProvider.properties"));
		} catch (IOException e) {
		}
		
		userName = (String)properties.get("userName");	
		password = (String)properties.get("password");
		
	}

	public String getPassword() {
		return password;
	}

	public String getUserName() {
		return userName;
	}


}
