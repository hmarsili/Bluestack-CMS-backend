package com.tfsla.cmsMedios.releaseManager.installer.common;

import java.util.Date;

public class ClusterNodeHistory {
	public SetupResult getSetupResult() {
		return setupResult;
	}
	public void setSetupResult(SetupResult setupResult) {
		this.setupResult = setupResult;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getNodeID() {
		return nodeID;
	}
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}
	public String getRM() {
		return RM;
	}
	public void setRM(String rM) {
		RM = rM;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	public String getManifest() {
		return manifest;
	}
	public void setManifest(String manifest) {
		this.manifest = manifest;
	}
	SetupResult setupResult;
	Date date;
	int nodeID;
	String RM;
	String log;
	String manifest;
}
