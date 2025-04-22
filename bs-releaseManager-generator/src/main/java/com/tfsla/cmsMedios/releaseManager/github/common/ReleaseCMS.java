package com.tfsla.cmsMedios.releaseManager.github.common;

public class ReleaseCMS {
	
	public String getReleaseName() {
		return releaseName;
	}
	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}
	public String getReleasePath() {
		return releasePath;
	}
	public void setReleasePath(String releasePath) {
		this.releasePath = releasePath;
	}
	public String getManifestPath() {
		return manifestPath;
	}
	public void setManifestPath(String manifestPath) {
		this.manifestPath = manifestPath;
	}
	
	String releaseName;
	String releasePath;
	String manifestPath;
}
