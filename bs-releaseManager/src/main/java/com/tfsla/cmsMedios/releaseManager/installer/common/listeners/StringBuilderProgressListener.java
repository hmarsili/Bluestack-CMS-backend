package com.tfsla.cmsMedios.releaseManager.installer.common.listeners;

import java.sql.Timestamp;

import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgress;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressListener;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressStatus;

public class StringBuilderProgressListener implements SetupProgressListener {
	
	private static StringBuilderProgressListener _instance = new StringBuilderProgressListener();
	private static StringBuilder _stringBuilder = new StringBuilder();
	
	private StringBuilderProgressListener() { }
	
	public static synchronized StringBuilderProgressListener getInstance() {
		return _instance;
	}
	
	public static synchronized String getLog() {
		return _stringBuilder.toString();
	}
	
	public static synchronized void clear() {
		_stringBuilder = new StringBuilder();
	}
	
	private static synchronized void addToLog(SetupProgress progress) {
		_stringBuilder.append(String.format("%s :: %s - %s %s %s \n",
			new Timestamp(System.currentTimeMillis()),
			progress.getStatus(),
			progress.getMessage(),
			progress.getPercentage()>0? String.format("(%s)", progress.getPercentage()) : "",
			progress.getIsPartial() ? "- paso completo" : ""
		));
	}
	
	@Override
	public void progress(String message, int percentage, SetupProgressStatus status) {
		addToLog(new SetupProgress() {{
			setMessage(message);
			setPercentage(percentage);
			setStatus(status);
		}});
	}

	@Override
	public void error(String message, Exception e) {
		addToLog(new SetupProgress() {{
			setMessage(message);
			setStatus(SetupProgressStatus.ERROR);
		}});
	}

	@Override
	public void completed(String message, Boolean mustReload) {
		addToLog(new SetupProgress() {{
			setMessage(message);
			setStatus(SetupProgressStatus.COMPLETED);
			setIsPartial(true);
			setMustReload(mustReload);
		}});
	}

	@Override
	public void completePartial(String message) {
		addToLog(new SetupProgress() {{
			setMessage(message);
			setStatus(SetupProgressStatus.MESSAGE);
			setIsPartial(true);
		}});
	}

	@Override
	public void reset() {
		clear();
	}
}
