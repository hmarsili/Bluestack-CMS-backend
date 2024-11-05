package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import com.tfsla.cmsMedios.releaseManager.installer.service.SetupProgressService;

public class CreateJobsDeployStep extends DeployStepContext {

	@Override
	public void deploy() throws Exception {
		if (manifest.containsKey("jobs")) {
			Thread.sleep(2000);
			//JobsService s = new JobsService(manifest, cmsObject);
			//s.createJobs();
			SetupProgressService.reportProgress("Jobs will be created after tomcat restarts");
		} else {
			SetupProgressService.reportProgress("There are no new jobs on this release");
		}
	}	

	@Override
	public String getPartialMessage() {
		return "Jobs processed";
	}

	@Override
	public String getStepName() {
		return "Crear Jobs";
	}
}
