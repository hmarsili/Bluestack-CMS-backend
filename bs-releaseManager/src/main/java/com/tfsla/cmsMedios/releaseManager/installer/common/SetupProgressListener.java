package com.tfsla.cmsMedios.releaseManager.installer.common;

public interface SetupProgressListener {
	
	public void progress(String message, int percentage, SetupProgressStatus status);
	
	public void error(String message, Exception e);
	
	public void completed(String message, Boolean mustReload);
	
	public void completePartial(String message);
	
	public void reset();
}
