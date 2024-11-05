package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;

import com.tfsla.cmsMedios.releaseManager.installer.common.ReleaseManagerConfiguration;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BackupGenerator {
	
	public static synchronized String backup(JSONObject manifest, CmsObject cmsObject, ReleaseManagerConfiguration config) throws IOException, CmsException {
		
		String backupDir = config.getTempDir() + manifest.getString("rm") + "_backup/";
		SetupProgressService.reportProgress("Creating a restore point on " + backupDir);
		String sourcePath = config.getConfigDir() + "cmsMedios.xml";
		File sourceFile = new File(sourcePath);
		File destFile = new File(backupDir + "config/cmsMedios.xml");
		if(destFile.exists()) {
			destFile.delete();
		}
		destFile.getParentFile().mkdirs();
		Files.copy(sourceFile.toPath(), destFile.toPath());
		
		if(manifest.containsKey("config")) {
			backupElements(manifest, "config", backupDir, config.getConfigDir());
		}
		
		if(manifest.containsKey("config-removed")) {
			backupElements(manifest, "config-removed", backupDir, config.getConfigDir());
		}
		
		if(manifest.containsKey("jars")) {
			backupElements(manifest, "jars", backupDir, config.getJarsDir());
		}
		
		if(manifest.containsKey("jars-removed")) {
			backupElements(manifest, "jars-removed", backupDir, config.getJarsDir());
		}
		
		if(manifest.containsKey("cmsMedios")) {
			backupElements(manifest, "cmsMedios", backupDir, config.getModulesEnabledDir());
		}
		
		if(manifest.containsKey("files")) {
			JSONObject files = manifest.getJSONObject("files");
			if(files.containsKey("removed")) {
				for(Object file : files.getJSONArray("removed")) {
					try {
						byte[] fileContents = cmsObject.readFile(file.toString()).getContents();
						File backupFile = new File(backupDir + "files/added/" + file.toString());
						if(backupFile.exists()) {
							backupFile.delete();
						}
						backupFile.getParentFile().mkdirs();
						FileUtils.writeByteArrayToFile(backupFile, fileContents);
					} catch(CmsVfsResourceNotFoundException e) {
						SetupProgressService.warning(e.getMessage());
					}
				}
			}
			if(files.containsKey("modified")) {
				for(Object file : files.getJSONArray("modified")) {
					try {
						byte[] fileContents = cmsObject.readFile(file.toString()).getContents();
						File backupFile = new File(backupDir + "files/modified/" + file.toString());
						if(backupFile.exists()) {
							backupFile.delete();
						}
						backupFile.getParentFile().mkdirs();
						FileUtils.writeByteArrayToFile(backupFile, fileContents);
					} catch(CmsVfsResourceNotFoundException e) {
						SetupProgressService.warning(e.getMessage());
					}
				}
			}
		}
		SetupProgressService.reportProgress("Backup created");
		return backupDir;
	}
	
	private static synchronized void backupElements(JSONObject manifest, String elements, String backupDir, String sourceDir) throws IOException {
		if(manifest.containsKey(elements)) {
			JSONArray files = manifest.getJSONArray(elements);
			String fileDir = backupDir + elements;
			if(!fileDir.endsWith("/")) {
				fileDir += "/";
			}
			new File(fileDir).getParentFile().mkdirs();
			
			for(Object file : files) {
				String sourcePath = sourceDir + file.toString();
				File sourceFile = new File(sourcePath);
				File destFile = new File(fileDir + file.toString());
				destFile.getParentFile().mkdirs();
				if(!sourceFile.exists()) { continue; }
				if(destFile.exists()) {
					destFile.delete();
				}
				Files.copy(sourceFile.toPath(), destFile.toPath());
			}
		}
	}
}
