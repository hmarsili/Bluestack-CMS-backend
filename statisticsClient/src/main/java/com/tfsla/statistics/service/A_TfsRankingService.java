package com.tfsla.statistics.service;

import java.util.Date;

import com.tfsla.statistics.SoapConfig;

public abstract class A_TfsRankingService {

	static int errorCount = 0;
	static Date blockedStamp = new Date();
	static boolean blocked = false;
	
	protected boolean isBlocked()
	{
		if (!blocked)
			return false;
		
		Date now = new Date();
		
		long milliseconds = now.getTime() - blockedStamp.getTime();
		
		if (SoapConfig.getInstance().getRemoteErrorBlockPeriodSeconds()*1000 - milliseconds < 0)
		{
			errorCount = 0;
			blocked = false;
			return false;
		}
		
		return true;
	}
	
	protected void manageError()
	{
		errorCount++;
		
		if (blocked)
			return;
		
		Date now = new Date();
		long milliseconds = now.getTime() - blockedStamp.getTime();
		
		if (milliseconds - SoapConfig.getInstance().getRemoteErrorBlockPeriodSeconds() * 1000 < 0)
		{
			if (errorCount > SoapConfig.getInstance().getRemoteErrorAllowedPerPeriod())
				blocked = true;
			else
				errorCount =1;	
			blockedStamp = now;
		}
	}

}
