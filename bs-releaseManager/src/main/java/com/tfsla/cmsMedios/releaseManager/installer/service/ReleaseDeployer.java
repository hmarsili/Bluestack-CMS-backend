package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.tfsla.cmsMedios.releaseManager.installer.common.ClusterNode;
import com.tfsla.cmsMedios.releaseManager.installer.common.DeployReleaseRequest;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupResult;
import com.tfsla.cmsMedios.releaseManager.installer.common.exceptions.ReleaseNameNotFoundException;
import com.tfsla.cmsMedios.releaseManager.installer.common.exceptions.ReleaseValidationException;
import com.tfsla.cmsMedios.releaseManager.installer.common.listeners.StringBuilderProgressListener;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.ConfigFilesDeployStep;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.CreateJobsDeployStep;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.DeployStepContext;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.DownloadReleaseFileDeployStep;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.GenerateBackupDeployStep;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.JarsDeployStep;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.SQLScriptsDeployStep;
import com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps.VFSFilesDeployStep;

public class ReleaseDeployer {
	
	public static synchronized List<String> getDeploySteps(HttpServletRequest request) throws Exception {
		List<DeployStepContext> steps = generateDeploySteps(request);
		List<String> stepNames = steps.stream()
			.map(DeployStepContext::getStepName)
			.collect(Collectors.toList()
		);
		
		return stepNames;
	}
	
	public static synchronized List<DeployStepContext> generateDeploySteps(HttpServletRequest request) throws Exception {
		NodeReleaseService service = new NodeReleaseService();
		ClusterNode node = service.getNode(request);
		
		List<DeployStepContext> steps = new ArrayList<DeployStepContext>();
		steps.add(new DownloadReleaseFileDeployStep());
		steps.add(new GenerateBackupDeployStep());
		if(node.getIsWP()) {
			steps.add(new VFSFilesDeployStep());
			steps.add(new SQLScriptsDeployStep());
			steps.add(new CreateJobsDeployStep());
		}
		steps.add(new ConfigFilesDeployStep());
		steps.add(new JarsDeployStep());
		return steps;
	}
	
	public static synchronized void deploy(DeployReleaseRequest deployRequest, CmsObject cmsObject, HttpServletRequest request) throws IOException, ReleaseValidationException, CmsException, ReleaseNameNotFoundException {
		SetupProgressService.reportProgress("Resolving cluster information...");
		NodeReleaseService nodeService = new NodeReleaseService();
		ClusterNode node = nodeService.getNode(request);
		JSONObject manifest = null;
		try {
			cmsObject.getRequestContext().setSiteRoot("/");
			DeployStepContext.reset();
			
			/* Iterate over deployment steps and execute them */
			List<DeployStepContext> steps = generateDeploySteps(request);
			for(DeployStepContext deployStep : steps) {
				deployStep.init(deployRequest, cmsObject, request, node);
				deployStep.deploy();
				SetupProgressService.completePartial(deployStep.getPartialMessage());
				Thread.sleep(2000);
			}
			
			manifest = DeployStepContext.getManifest();
			
			node.setNeedsConfiguration(DeployStepContext.getMustConfig());
			node.setRM(deployRequest.getReleaseName());
			node.setManifest(manifest.toString());
			node.setReadme(FileUtils.readFileToString(new File(DeployStepContext.getReleasePath() + "config/readme.txt")));
			node.setMessages(DeployStepContext.getDeployMessages());
			nodeService.updateNode(node);
			nodeService.addNodeHistory(node, StringBuilderProgressListener.getLog(), deployRequest.getReleaseName(), SetupResult.OK, manifest.toString());

			// Cleanup
			String path = deployRequest.getConfig().getReleasesDirectory() + deployRequest.getReleaseName();
			FileUtils.deleteDirectory(new File(path));
			FileUtils.deleteDirectory(new File(DeployStepContext.getBackupDirectory()));
			
			Boolean mustReload = DeployStepContext.getMustReload();
			if(mustReload) {
				TomcatManagerService.reloadAsync(deployRequest.getConfig().getTomcatManagerConfiguration());
				SetupProgressService.reportProgress("Tomcat App Reload on progress");
				Thread.sleep(500);
			}
			
			SetupProgressService.completed("Release deployment completed!", mustReload);
			
			Thread.sleep(500);
		} catch(Exception e) {
			//TODO: restore backup if fails
			e.printStackTrace();
			
			SetupProgressService.error(e);
			
			nodeService.addNodeHistory(node, StringBuilderProgressListener.getLog(), deployRequest.getReleaseName(), SetupResult.ERROR, manifest != null ? manifest.toString() : "");
		}
	}
}
