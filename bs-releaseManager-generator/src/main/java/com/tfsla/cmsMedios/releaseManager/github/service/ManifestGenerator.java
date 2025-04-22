package com.tfsla.cmsMedios.releaseManager.github.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ManifestGenerator {
	private JSONObject manifest;
	private JSONObject files;
	private JSONArray config;
	private JSONArray attachedFiles;
	private JSONArray protectedFiles;
	private JSONArray removedJars;
	private JSONArray jars;
	private JSONArray scripts;
	private JSONArray cmsMedios;
	private String updateBannerLink;
	private String updateBannerVfsLink;
	private String releaseVersion;
	
	public ManifestGenerator(String tagName) {
		this.manifest = new JSONObject();
		this.manifest.put("rm", tagName);
		this.config = new JSONArray();
		this.scripts = new JSONArray();
		this.cmsMedios = new JSONArray();
		this.attachedFiles = new JSONArray();
		this.protectedFiles = new JSONArray();
		this.removedJars = new JSONArray();
		this.jars = new JSONArray();
		this.files = new JSONObject();
		this.updateBannerLink = "";
		this.updateBannerVfsLink = "";
		files.put("added", new JSONArray());
		files.put("modified", new JSONArray());
		files.put("removed", new JSONArray());
	}
	
	public void addAttachedFile(String attachedFileName) {
		attachedFiles.add(attachedFileName);
	}
	
	public void addProtectedFile(String protectedFilePath) {
		protectedFiles.add(protectedFilePath);
	}
	
	public void addRemovedJar(String removedJar) {
		removedJars.add(removedJar);
	}
	
	public void addJar(String jarName) {
		jars.add(jarName);
	}
	
	public void addConfig(String configName) {
		config.add(configName);
	}
	
	public void addScript(String scriptName) {
		scripts.add(scriptName);
	}
	
	public void addCmsMediosConfig(String cmsMediosXML) {
		cmsMedios.add(cmsMediosXML);
	}
	
	public void addFileCreated(String fileName) {
		this.addFile(fileName, "added");
	}
	
	public void addFileChanged(String fileName) {
		this.addFile(fileName, "modified");
	}
	
	public void addFileRemoved(String fileName) {
		this.addFile(fileName, "removed");
	}
	
	public void addFile(String fileName, String fileStatus) {
		files.getJSONArray(fileStatus).add(fileName);
	}
	
	public void addUpdateBannerLink(String updateBannerLink) {
		this.updateBannerLink = updateBannerLink;
	}
	
	public void addupdateBannerVfsLink(String updateBannerVfsLink) {
		this.updateBannerVfsLink = updateBannerVfsLink;
	}
	
	public void addReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}
	
	public JSONObject getManifest() {
		
		this.manifest.put("updateBannerLink", updateBannerLink);
		this.manifest.put("updateBannerVfsLink", updateBannerVfsLink);
		this.manifest.put("releaseVersion", releaseVersion);
		
		if(config.size() > 0) {
			this.manifest.put("config", config);
		}
		if(scripts.size() > 0) {
			this.manifest.put("scripts", scripts);
		}
		if(cmsMedios.size() > 0) {
			this.manifest.put("cmsMedios", cmsMedios);
		}
		if(jars.size() > 0) {
			this.manifest.put("jars", jars);
		}
		if(removedJars.size() > 0) {
			this.manifest.put("jars-removed", removedJars);
		}
		if(attachedFiles.size() > 0) {
			this.manifest.put("attachedFiles", attachedFiles);
		}
		if(protectedFiles.size() > 0) {
			this.manifest.put("protectedFiles", protectedFiles);
		}
		if(files.getJSONArray("added").size() > 0 
				|| files.getJSONArray("modified").size() > 0 
				|| files.getJSONArray("removed").size() > 0) {
			this.manifest.put("files", files);
		}
		
		return this.manifest;
	}
}