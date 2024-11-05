package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import java.io.InputStream;

import com.tfsla.cmsMedios.releaseManager.installer.service.ReleaseFileValidator;
import com.tfsla.cmsMedios.releaseManager.installer.service.ReleaseRetriever;
import com.tfsla.cmsMedios.releaseManager.installer.service.SetupProgressService;
import com.tfsla.cmsMedios.releaseManager.installer.service.ZipUtils;

public class DownloadReleaseFileDeployStep extends DeployStepContext {

	@Override
	public void deploy() throws Exception {
		SetupProgressService.reportProgress("Downloading release file...");
		InputStream releaseFile = ReleaseRetriever.getReleaseFile(deployRequest.getConfig().getAmazonConfiguration(), deployRequest.getReleaseName());
		SetupProgressService.reportProgress("Unpacking...");
		String releaseDirectory = deployRequest.getConfig().getReleasesDirectory() + deployRequest.getReleaseName();
		ZipUtils.unpack(releaseFile, releaseDirectory);
		releasePath = deployRequest.getConfig().getReleasesDirectory() + deployRequest.getReleaseName();
		if(!releasePath.endsWith("/")) releasePath += "/";
		
		SetupProgressService.reportProgress("Downloading manifest...");
		manifest = ReleaseRetriever.getReleaseManifest(
			deployRequest.getConfig().getAmazonConfiguration(), 
			deployRequest.getReleaseName()
		);
		
		SetupProgressService.reportProgress("Validating release file...");
		ReleaseFileValidator.validate(releasePath, manifest);
	}

	@Override
	public String getPartialMessage() {
		return "Release contents ready to be installed";
	}

	@Override
	public String getStepName() {
		return "Descargar contenidos del release";
	}

}