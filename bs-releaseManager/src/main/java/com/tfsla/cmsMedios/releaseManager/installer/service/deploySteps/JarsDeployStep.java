package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import java.io.File;
import java.nio.file.Files;

import com.tfsla.cmsMedios.releaseManager.installer.service.SetupProgressService;

public class JarsDeployStep extends DeployStepContext {

	@Override
	public void deploy() throws Exception {
		//COPY JARS
		SetupProgressService.reportProgress("Updating jar files...");
		Thread.sleep(2000);
		if(manifest.containsKey("jars")) {
			for(Object item : manifest.getJSONArray("jars")) {
				File source = new File(releasePath + "jars/" + item.toString());
				File dest = new File(deployRequest.getConfig().getJarsDir() + item.toString());
				
				if(dest.exists()) {
					SetupProgressService.reportProgress("Updating " + dest.getAbsolutePath());
					dest.delete();
				} else {
					SetupProgressService.reportProgress("Writing " + dest.getAbsolutePath());
				}
				Files.copy(source.toPath(), dest.toPath());
			}
			mustReload = true;
		} else {
			SetupProgressService.reportProgress("There are no jar files to be updated");
		}
		
		//REMOVE JARS
		if(manifest.containsKey("jars-removed")) {
			for(Object item : manifest.getJSONArray("jars-removed")) {
				File dest = new File(deployRequest.getConfig().getJarsDir() + item.toString());
				SetupProgressService.reportProgress("Removing file " + item.toString());
				if(dest.exists()) {
					dest.delete();
				} else {
					SetupProgressService.reportProgress("File " + item.toString() + " already removed");
				}
			}
			mustReload = true;
		}
		
		Thread.sleep(2000);
	}

	@Override
	public String getPartialMessage() {
		return "Jar files update finished";
	}

	@Override
	public String getStepName() {
		return "Actualizar JARs";
	}

}
