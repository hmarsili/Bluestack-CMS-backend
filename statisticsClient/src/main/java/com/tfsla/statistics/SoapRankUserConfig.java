package com.tfsla.statistics;

import java.util.Properties;
import java.io.*;

public class SoapRankUserConfig {

	private static SoapRankUserConfig instance = new SoapRankUserConfig();
	
	public static SoapRankUserConfig getInstance()
	{
		return instance;
	}
	
	private String rankUser_address = null;
		//"http://localhost:8180/com.tfsla.statistics/services/TfsRankingUsers";

	public String getRankUser_address()
	{
		return rankUser_address;
	}
	
	private SoapRankUserConfig()
	{
		Properties properties = new Properties();
		try {
			properties.load(SoapRankPageConfig.class.getResourceAsStream("SoapConfig.properties"));
		} catch (IOException e) {
		}
		
		rankUser_address = (String)properties.get("rankUser_address");	
	}

}
