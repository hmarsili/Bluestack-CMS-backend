package com.tfsla.diario.auditActions.resourceMonitor;

import java.util.*;

public class ResourceMonitorManager {

	Map<Integer,I_ResourceMonitor>  resourceMonitors = new HashMap<Integer,I_ResourceMonitor>();
	
	public static ResourceMonitorManager getInstance(){
		return instance;
	} 
	
	static private ResourceMonitorManager instance = new ResourceMonitorManager();
	
	private ResourceMonitorManager(){
		I_ResourceMonitor newsMonitor = new NewsMonitor();
		resourceMonitors.put(newsMonitor.getResourceType(), newsMonitor);
	}
	
	public I_ResourceMonitor getResourceMonitor(int type) {
		return resourceMonitors.get(type);
	}
}
