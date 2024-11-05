package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;

import com.tfsla.cmsMedios.releaseManager.installer.common.ClusterNode;
import com.tfsla.cmsMedios.releaseManager.installer.common.DeployMessage;
import com.tfsla.cmsMedios.releaseManager.installer.common.DeployReleaseRequest;

import net.sf.json.JSONObject;

public abstract class DeployStepContext {
	
	public void init(DeployReleaseRequest deployRequest, CmsObject cmsObject, HttpServletRequest request, ClusterNode node) {
		this.deployRequest = deployRequest;
		this.cmsObject = cmsObject;
		this.request = request;
		this.node = node;
	}
	
	protected DeployReleaseRequest deployRequest;
	protected CmsObject cmsObject;
	protected HttpServletRequest request;
	protected ClusterNode node;
	
	public abstract void deploy() throws Exception;
	
	public abstract String getPartialMessage();
	
	public abstract String getStepName();

	public static Boolean getMustReload() {
		return mustReload;
	}
	
	public static Boolean getMustConfig() {
		return mustConfig;
	}
	
	public static String getReleasePath() {
		return releasePath;
	}
	
	public static JSONObject getManifest() {
		return manifest;
	}
	
	public static ArrayList<DeployMessage> getDeployMessages() {
		return deployMessages;
	}
	
	public static String getBackupDirectory() {
		return backupDir;
	}
	
	protected static Boolean mustReload = false;
	protected static Boolean mustConfig = false;
	protected static String releasePath;
	protected static JSONObject manifest;
	protected static String backupDir;
	protected static ArrayList<DeployMessage> deployMessages = new ArrayList<DeployMessage>();

	public static void reset() {
		deployMessages = new ArrayList<DeployMessage>();
		manifest = null;
		mustConfig = false;
		mustReload = false;
		backupDir = null;
	}
}
