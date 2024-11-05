package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.util.ArrayList;
import java.util.List;

import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressListener;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressStatus;
import com.tfsla.cmsMedios.releaseManager.installer.common.listeners.CmsLogProgressListener;
import com.tfsla.cmsMedios.releaseManager.installer.common.listeners.JSPProgressListener;
import com.tfsla.cmsMedios.releaseManager.installer.common.listeners.StringBuilderProgressListener;

public class SetupProgressService {
	
	private List<SetupProgressListener> listeners = null;
	private static SetupProgressService instance = null;
	
	private SetupProgressService() {
		listeners = new ArrayList<SetupProgressListener>();
		listeners.add(new CmsLogProgressListener());
		listeners.add(JSPProgressListener.getInstance());
		listeners.add(StringBuilderProgressListener.getInstance());
	}
	
	public static synchronized SetupProgressService getInstance() {
		if(instance == null) {
			instance = new SetupProgressService();
		}
		return instance;
	}
	
	public static synchronized List<SetupProgressListener> getListeners() {
		return getInstance().listeners;
	}
		
	public static synchronized void removeListener(SetupProgressListener listener) {
		getInstance().listeners.remove(listener);
	}
	
	public static synchronized void addListener(SetupProgressListener listener) {
		getInstance().listeners.add(listener);
	}
	
	public static synchronized void completed(String message, Boolean mustReload) {
		for(SetupProgressListener listener : getInstance().listeners) {
			listener.completed(message, mustReload);
		}
	}
	
	public static synchronized void completePartial(String message) {
		for(SetupProgressListener listener : getInstance().listeners) {
			listener.completePartial(message);
		}
	}
	
	public static synchronized void error(Exception e) {
		error(e.getMessage(), e);
	}
	
	public static synchronized void error(String message) {
		error(message, null);
	}
	
	public static synchronized void error(String message, Exception e) {
		for(SetupProgressListener listener : getInstance().listeners) {
			listener.error(message, e);
		}
	}
	
	public static synchronized void warning(String message) {
		reportProgress(message, 0, SetupProgressStatus.WARNING);
	}
	
	public static synchronized void reportProgress(String message) {
		reportProgress(message, 0, SetupProgressStatus.MESSAGE);
	}
	
	public static synchronized void reportProgress(String message, SetupProgressStatus status) {
		reportProgress(message, 0, status);
	}
	
	public static synchronized void reportProgress(int percentage) {
		reportProgress("", percentage, SetupProgressStatus.MESSAGE);
	}
	
	public static synchronized void reportProgress(String message, int percentage, SetupProgressStatus status) {
		for(SetupProgressListener listener : getInstance().listeners) {
			listener.progress(message, percentage, status);
		}
	}
}
