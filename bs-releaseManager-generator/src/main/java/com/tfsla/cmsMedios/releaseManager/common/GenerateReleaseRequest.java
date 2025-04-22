package com.tfsla.cmsMedios.releaseManager.common;

import com.tfsla.cmsMedios.releaseManager.common.ConnectorConfiguration;

public class GenerateReleaseRequest {
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getPreviousTagName() {
		return previousTagName;
	}
	public void setPreviousTagName(String previousTagName) {
		this.previousTagName = previousTagName;
	}
	public ConnectorConfiguration getCoreConfiguration() {
		return coreConfiguration;
	}
	public void setCoreConfiguration(ConnectorConfiguration coreConfiguration) {
		this.coreConfiguration = coreConfiguration;
	}
	public ConnectorConfiguration getVfsConfiguration() {
		return vfsConfiguration;
	}
	public void setVfsConfiguration(ConnectorConfiguration vfsConfiguration) {
		this.vfsConfiguration = vfsConfiguration;
	}
	public String getReleasesDirectory() {
		return releasesDirectory;
	}
	public void setReleasesDirectory(String releasesDirectory) {
		this.releasesDirectory = releasesDirectory;
	}
	public String getProtectedFiles() {
		return protectedFiles;
	}
	public void setProtectedFiles(String protectedFiles) {
		this.protectedFiles = protectedFiles;
	}
	public String getRemovedJars() {
		return removedJars;
	}
	public void setRemovedJars(String removedJars) {
		this.removedJars = removedJars;
	}
	
	public String getUpdateBannerLink() {
		return updateBannerLink;
	}
	public void setUpdateBannerLink(String updateBannerLink) {
		this.updateBannerLink = updateBannerLink;
	}
	
	public String getUpdateBannerVFSLink() {
		return updateBannerVFSLink;
	}
	public void setUpdateBannerVFSLink(String updateBannerVFSLink) {
		this.updateBannerVFSLink = updateBannerVFSLink;
	}
	
	public String getGitCoreFolder() {
		return gitCoreFolder;
	}
	public void setGitCoreFolder(String gitCoreFolder) {
		this.gitCoreFolder = gitCoreFolder;
	}
	
	public String getReleaseVersion() {
		return releaseVersion;
	}
	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}
	
	ConnectorConfiguration coreConfiguration;
	ConnectorConfiguration vfsConfiguration;
	String tagName;
	String previousTagName;
	String releasesDirectory;
	String protectedFiles;
	String removedJars;
	String updateBannerLink;
	String updateBannerVFSLink;
	String gitCoreFolder;
	String releaseVersion;
}
