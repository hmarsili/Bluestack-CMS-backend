package com.tfsla.cdnIntegration.model;

import java.util.*;
import org.opencms.file.CmsResource;

public class PurgePackage {
	
	public static int STATUS_NEW = 0;
	public static int STATUS_DONE = 1;
	public static int STATUS_ERROR = 2;
	public static int STATUS_PENDING = 3;
	
	private String processId;
	private Date timestamp;
	private int retries;
	private int status;
	private List<CmsResource> resources;
	
	public PurgePackage() {
		resources = new ArrayList<CmsResource>();
	}
	
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public int getRetries() {
		return retries;
	}
	public void setRetries(int retries) {
		this.retries = retries;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public List<CmsResource> getResources() {
		return resources;
	}
	
}
