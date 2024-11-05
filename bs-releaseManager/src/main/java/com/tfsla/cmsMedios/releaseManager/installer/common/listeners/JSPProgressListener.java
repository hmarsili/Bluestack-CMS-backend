package com.tfsla.cmsMedios.releaseManager.installer.common.listeners;

import java.util.ArrayList;
import java.util.List;

import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgress;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressListener;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupProgressStatus;

public class JSPProgressListener implements SetupProgressListener {

	private static JSPProgressListener _instance = new JSPProgressListener();
	private static List<SetupProgress> _events_queue = new ArrayList<SetupProgress>();
	
	private JSPProgressListener() { }
	
	public static synchronized JSPProgressListener getInstance() {
		return _instance;
	}
	
	public static synchronized List<SetupProgress> getMessages() {
		List<SetupProgress> ret = new ArrayList<SetupProgress>();
		for(SetupProgress progress : _events_queue) {
			ret.add(progress);
		}
		clear();
		return ret;
	}
	
	public static synchronized void clear() {
		_events_queue = new ArrayList<SetupProgress>();
	}
	
	private static synchronized void addToQueue(SetupProgress progress) {
		_events_queue.add(progress);
	}
	
	@Override
	public void progress(String message, int percentage, SetupProgressStatus status) {
		addToQueue(new SetupProgress() {{
			setMessage(message);
			setPercentage(percentage);
			setStatus(status);
		}});
	}

	@Override
	public void error(String message, Exception e) {
		addToQueue(new SetupProgress() {{
			setMessage(message);
			setStatus(SetupProgressStatus.ERROR);
		}});
	}

	@Override
	public void completed(String message, Boolean mustReload) {
		addToQueue(new SetupProgress() {{
			setMessage(message);
			setStatus(SetupProgressStatus.COMPLETED);
			setIsPartial(true);
			setMustReload(mustReload);
		}});
	}

	@Override
	public void completePartial(String message) {
		addToQueue(new SetupProgress() {{
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
