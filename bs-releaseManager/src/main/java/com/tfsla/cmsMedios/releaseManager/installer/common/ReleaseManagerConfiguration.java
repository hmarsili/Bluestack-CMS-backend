package com.tfsla.cmsMedios.releaseManager.installer.common;

import java.util.List;

public class ReleaseManagerConfiguration {
	public String getReleasesDirectory() {
		return releasesDirectory;
	}
	public void setReleasesDirectory(String releasesDirectory) {
		this.releasesDirectory = releasesDirectory;
	}
	public AmazonConfiguration getAmazonConfiguration() {
		return amazonConfiguration;
	}
	public void setAmazonConfiguration(AmazonConfiguration amazonConfiguration) {
		this.amazonConfiguration = amazonConfiguration;
	}
	public String getTempDir() {
		if(!tempDir.endsWith("/")) {
			tempDir += "/";
		}
		return tempDir;
	}
	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
	public String getJarsDir() {
		if(!jarsDir.endsWith("/")) {
			jarsDir += "/";
		}
		return jarsDir;
	}
	public void setJarsDir(String jarsDir) {
		this.jarsDir = jarsDir;
	}
	public String getConfigDir() {
		if(!configDir.endsWith("/")) {
			configDir += "/";
		}
		return configDir;
	}
	public void setConfigDir(String configDir) {
		this.configDir = configDir;
	}
	public TomcatManagerConfiguration getTomcatManagerConfiguration() {
		return tomcatManagerConfiguration;
	}
	public void setTomcatManagerConfiguration(TomcatManagerConfiguration tomcatManagerConfiguration) {
		this.tomcatManagerConfiguration = tomcatManagerConfiguration;
	}
	public String getModulesAvailableDir() {
		if(!modulesAvailableDir.endsWith("/")) {
			modulesAvailableDir += "/";
		}
		return modulesAvailableDir;
	}
	public void setModulesAvailableDir(String modulesAvailableDir) {
		this.modulesAvailableDir = modulesAvailableDir;
	}
	public String getModulesEnabledDir() {
		if(!modulesEnabledDir.endsWith("/")) {
			modulesEnabledDir += "/";
		}
		return modulesEnabledDir;
	}
	public void setModulesEnabledDir(String modulesEnabledDir) {
		this.modulesEnabledDir = modulesEnabledDir;
	}
	public List<String> getCustomizedFiles() {
		return customizedFiles;
	}
	public void setCustomizedFiles(List<String> customizedFiles) {
		this.customizedFiles = customizedFiles;
	}
	protected AmazonConfiguration amazonConfiguration;
	protected TomcatManagerConfiguration tomcatManagerConfiguration;
	protected String releasesDirectory;
	protected String tempDir;
	protected String jarsDir;
	protected String configDir;
	protected String modulesAvailableDir;
	protected String modulesEnabledDir;
	protected List<String> customizedFiles;
}
