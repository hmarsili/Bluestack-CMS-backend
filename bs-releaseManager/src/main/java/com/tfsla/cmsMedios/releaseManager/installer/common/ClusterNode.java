package com.tfsla.cmsMedios.releaseManager.installer.common;

import java.util.ArrayList;

public class ClusterNode {
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getIP() {
		return IP;
	}
	public void setIP(String iP) {
		IP = iP;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRM() {
		return RM;
	}
	public void setRM(String rM) {
		RM = rM;
	}
	public Boolean getIsWP() {
		return isWP;
	}
	public void setIsWP(Boolean isWP) {
		this.isWP = isWP;
	}
	public Boolean getNeedsConfiguration() {
		return needsConfiguration;
	}
	public void setNeedsConfiguration(Boolean needsConfiguration) {
		this.needsConfiguration = needsConfiguration;
	}
	public String getManifest() {
		return manifest;
	}
	public void setManifest(String manifest) {
		this.manifest = manifest;
	}
	public String getReadme() {
		return readme;
	}
	public void setReadme(String readme) {
		this.readme = readme;
	}
	public ArrayList<DeployMessage> getMessages() {
		return messages;
	}
	public void setMessages(ArrayList<DeployMessage> messages) {
		this.messages = messages;
	}
	int ID;
	String IP;
	String name;
	String RM;
	String manifest;
	String readme;
	Boolean isWP;
	Boolean needsConfiguration;
	ArrayList<DeployMessage> messages;
}
