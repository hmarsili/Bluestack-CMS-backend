package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;

import com.tfsla.cmsMedios.releaseManager.installer.service.SetupProgressService;

import net.sf.json.JSONObject;

public class ConfigFilesDeployStep extends DeployStepContext {

	@Override
	public void deploy() throws Exception {
		//COPY CONFIG FILES
		SetupProgressService.reportProgress("Copying config files...");
		if (manifest.containsKey("config")) {
			Thread.sleep(2000);
			for (Object item : manifest.getJSONArray("config")) {
				if (item.toString().equals("readme.txt")) continue;
				String destPath = deployRequest.getConfig().getConfigDir() + item.toString();
				destPath = destPath.replace("config/", "");
				File dest = new File(destPath);
				File source = new File(releasePath + "config/" + item.toString());
				
				if (dest.exists()) {
					dest.delete();
					SetupProgressService.reportProgress("Updating " + dest.getAbsolutePath());
				} else {
					SetupProgressService.reportProgress("Writing " + dest.getAbsolutePath());
				}
				
				Files.copy(source.toPath(), dest.toPath());
			}
		} else {
			SetupProgressService.reportProgress("There are no config files to be updated");
		}
		
		//REMOVE CONFIG FILES
		if (manifest.containsKey("config-removed")) {
			Thread.sleep(2000);
			for (Object item : manifest.getJSONArray("config-removed")) {
				File dest = new File(deployRequest.getConfig().getConfigDir() + item.toString());
				if (dest.exists()) {
					SetupProgressService.reportProgress("Removing " + dest.getAbsolutePath());
					dest.delete();
				}
			}
		}
		
		//DEPLOY XML CONFIG FILES
		if (manifest.containsKey("cmsMedios")) {
			Thread.sleep(2000);
			mustConfig = true;
			ArrayList<String> modules = new ArrayList<String>();
			SetupProgressService.reportProgress("Deploying configuration modules...");
			for (Object item : manifest.getJSONArray("cmsMedios")) {
				File source = new File(releasePath + "cmsMedios/" + item.toString());
				File dest = new File(deployRequest.getConfig().getModulesAvailableDir() + item.toString());
				modules.add(item.toString());
				
				if (dest.exists()) {
					dest.delete();
					SetupProgressService.reportProgress("Updating " + dest.getAbsolutePath());
				} else {
					SetupProgressService.reportProgress("Writing " + dest.getAbsolutePath());
				}
				
				Files.copy(source.toPath(), dest.toPath());
			}
			
			File modulesFile = new File(deployRequest.getConfig().getModulesAvailableDir() + "modules.txt");
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(modulesFile));
				for (String module : modules) {
					writer.write(module);
					writer.newLine();
				}
				SetupProgressService.reportProgress("Created modules.txt, modules to be configured: " + modules.size());
			} catch (Exception e) {
				SetupProgressService.warning("Cannot stat modules.txt");
				SetupProgressService.error(e);
			} finally {
				try {
	                writer.close();
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
			}
		}
		
		//DEPLOY GENERIC CONFIG FILES
		if (manifest.containsKey("filesystem")) {
			Thread.sleep(2000);
			SetupProgressService.reportProgress("Deploying file system files...");
			for (Object item : manifest.getJSONArray("filesystem")) {
				String fileName = ((JSONObject)item).getString("name");
				String destination = ((JSONObject)item).getString("destination");
				File source = new File(releasePath + "filesystem/" + fileName);
				File dest = new File(destination);
				
				if (dest.exists()) {
					dest.delete();
					SetupProgressService.reportProgress("Updating " + dest.getAbsolutePath());
				} else {
					SetupProgressService.reportProgress("Writing " + dest.getAbsolutePath());
				}
				
				Files.copy(source.toPath(), dest.toPath());
			}
		}
	}

	@Override
	public String getPartialMessage() {
		return "Config files update finished";
	}

	@Override
	public String getStepName() {
		return "Actualizar archivos y configuración";
	}

}
