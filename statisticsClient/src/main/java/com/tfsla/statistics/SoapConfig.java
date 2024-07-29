package com.tfsla.statistics;

import java.util.Properties;
import java.io.*;

public class SoapConfig {


	private static SoapConfig instance = new SoapConfig();
	
	public static SoapConfig getInstance()
	{
		return instance;
	}
	
//	private String statistics_address = null;
		//"http://localhost:8180/com.tfsla.statistics/services/TfsRankingUsers";

	private int MaxConnectionsPerHost=800;
	private int SocketTimeOutMilliSeconds=240000;
	private int ConnectionTimeOutMilliSeconds=240000;
	private boolean useCachedViewHits=false;
	private int cachedHitMaxHitPerSend=20;
	private int cachedHitMaxLimit=1000;
	private int connectionRetryCount = 3;
	
	private int remoteErrorAllowedPerPeriod = 10;
	private int remoteErrorPeriodSeconds = 240;
	private int remoteErrorBlockPeriodSeconds = 300;
	
//	public String getStatistics_address()
//	{
//		return statistics_address;
//	}
	
	private SoapConfig()
	{
		Properties properties = new Properties();
		try {
			properties.load(SoapConfig.class.getResourceAsStream("SoapConfig.properties"));
		} catch (IOException e) {
		}
		
//		statistics_address = (String)properties.get("stat_address");
		
		try {
			MaxConnectionsPerHost= Integer.parseInt((String)properties.get("MaxConnectionsPerHost"));
			SocketTimeOutMilliSeconds= Integer.parseInt((String)properties.get("SocketTimeOutMilliSeconds"));
			ConnectionTimeOutMilliSeconds= Integer.parseInt((String)properties.get("ConnectionTimeOutMilliSeconds"));
			useCachedViewHits = ((String)properties.get("useCachedViewHits")).trim().toLowerCase().equals("true") ? true : false;
			cachedHitMaxHitPerSend = Integer.parseInt((String)properties.get("cachedHitMaxHitPerSend"));
			cachedHitMaxLimit = Integer.parseInt((String)properties.get("cachedHitMaxLimit"));
			connectionRetryCount = Integer.parseInt((String)properties.get("connectionRetryCount"));
			
			remoteErrorAllowedPerPeriod = Integer.parseInt((String)properties.get("remoteErrorAllowedPerPeriod"));
			remoteErrorPeriodSeconds = Integer.parseInt((String)properties.get("remoteErrorPeriodSeconds"));
			remoteErrorBlockPeriodSeconds = Integer.parseInt((String)properties.get("remoteErrorBlockPeriodSeconds"));

		}
		catch (NumberFormatException ex)
		{
			ex.printStackTrace();
		}
	}

	public int getMaxConnectionsPerHost() {
		return MaxConnectionsPerHost;
	}

	public int getSocketTimeOutMilliSeconds() {
		return SocketTimeOutMilliSeconds;
	}

	public int getConnectionTimeOutMilliSeconds() {
		return ConnectionTimeOutMilliSeconds;
	}

	public boolean isUseCachedViewHits()
	{
		return useCachedViewHits;
	}

	public int getCachedHitMaxHitPerSend() {
		return cachedHitMaxHitPerSend;
	}

	public int getCachedHitMaxLimit() {
		return cachedHitMaxLimit;
	}

	public int getConnectionRetryCount() {
		return connectionRetryCount;
	}
	
	public int getRemoteErrorAllowedPerPeriod() {
		return remoteErrorAllowedPerPeriod;
	}

	public int getRemoteErrorPeriodSeconds() {
		return remoteErrorPeriodSeconds;
	}

	public int getRemoteErrorBlockPeriodSeconds() {
		return remoteErrorBlockPeriodSeconds;
	}


}
