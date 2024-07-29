package com.tfsla.diario.utils;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;


public class SeparateLogger {

	private static final Log LOG = CmsLog.getLog(SeparateLogger.class);

	public static void FatalLog(String message,Exception e) {
		LOG.fatal(message,e);
	}
	
	public static void FatalLog(String message) {
		LOG.fatal(message);
	}
	
	public static void WarnLog(String message,Exception e) {
		LOG.warn(message,e);
	}
	
	public static void WarnLog(String message) {
		LOG.warn(message);
	}

	public static void InforLog(String message,Exception e) {
		LOG.info(message,e);
	}
	
	public static void InfoLog(String message) {
		LOG.info(message);
	}
	
	public static void ErrorLog(String message,Exception e) {
		LOG.error(message,e);
	}
	
	public static void ErrorLog(String message) {
		LOG.error(message);
	}
	
	public static void DebugLog(String message,Exception e) {
		LOG.debug(message,e);
	}
	
	public static void DebugLog(String message) {
		LOG.debug(message);
	}
}
