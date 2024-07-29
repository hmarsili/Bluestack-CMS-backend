package com.tfsla.statistics;

import java.util.Properties;
import java.io.*;

public class SoapRankPageConfig {

	private static SoapRankPageConfig instance = new SoapRankPageConfig();
	
	public static SoapRankPageConfig getInstance()
	{
		return instance;
	}
	
	private String rankViews_address = null;
		//"http://localhost:8180/com.tfsla.statistics/services/TfsRankingUsers";

	public String getRankViews_address()
	{
		return rankViews_address;
	}
	
	private SoapRankPageConfig()
	{
		Properties properties = new Properties();
		try {
			properties.load(SoapRankPageConfig.class.getResourceAsStream("SoapConfig.properties"));
		} catch (IOException e) {
		}
		
		rankViews_address = (String)properties.get("rankView_address");	
	}

}
