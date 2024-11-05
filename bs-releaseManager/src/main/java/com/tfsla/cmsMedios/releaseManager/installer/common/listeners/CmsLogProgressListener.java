package com.tfsla.cmsMedios.releaseManager.installer.common.listeners;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressListener;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressStatus;

public class CmsLogProgressListener implements SetupProgressListener {

	private static final Log LOG = CmsLog.getLog(CmsLogProgressListener.class);
	
	@Override
	public void progress(String message, int percentage, SetupProgressStatus status) {
		LOG.debug(String.format("%s - %s", status, message));
	}

	@Override
	public void error(String message, Exception e) {
		LOG.error(String.format("ERROR: %s", message), e);
	}

	@Override
	public void completed(String message, Boolean mustReload) {
		LOG.debug(String.format("COMPLETED - %s, must reload tomcat App: %s", message, mustReload));
	}

	@Override
	public void completePartial(String message) {
		LOG.debug(String.format("PARTIAL - %s", message));
	}

	@Override
	public void reset() { }
}
