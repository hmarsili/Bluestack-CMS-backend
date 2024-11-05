package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import com.tfsla.cmsMedios.releaseManager.installer.service.BackupGenerator;
import com.tfsla.cmsMedios.releaseManager.installer.service.SetupProgressService;

public class GenerateBackupDeployStep extends DeployStepContext {

	@Override
	public void deploy() throws Exception {
		SetupProgressService.reportProgress("Creating backup...");
		Thread.sleep(2000);
		backupDir = BackupGenerator.backup(manifest, cmsObject, deployRequest.getConfig());
	}

	@Override
	public String getPartialMessage() {
		return "Backup generated correctly";
	}

	@Override
	public String getStepName() {
		return "Generar backup";
	}

}