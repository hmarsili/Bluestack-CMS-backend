package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.util.ArrayList;
import java.util.List;

import com.tfsla.cmsMedios.releaseManager.installer.common.AmazonConfiguration;
import com.tfsla.cmsMedios.releaseManager.installer.common.ReleaseManagerConfiguration;
import com.tfsla.cmsMedios.releaseManager.installer.jsp.ReleaseConfigurationConfirmStep;
import com.tfsla.cmsMedios.releaseManager.installer.jsp.ReleaseConfigurationInfoStep;
import com.tfsla.cmsMedios.releaseManager.installer.jsp.ReleaseConfigurationPreUpgradeStep;
import com.tfsla.cmsMedios.releaseManager.installer.jsp.ReleaseConfigurationSQLStep;
import com.tfsla.cmsMedios.releaseManager.installer.jsp.ReleaseConfigurationStep;
import com.tfsla.diario.admin.jsp.TfsMessages;

import net.sf.json.JSONObject;

public class ReleaseConfigurationManager {
	
	public static final List<ReleaseConfigurationStep> getReleaseSteps(JSONObject manifest, TfsMessages messages) {
		List<ReleaseConfigurationStep> steps = new ArrayList<ReleaseConfigurationStep>();
		ReleaseConfigurationStep informationStep = new ReleaseConfigurationInfoStep(manifest, messages);
		informationStep.setTitle(messages.key("RM_INFO_STEP_TITLE"));
		informationStep.setDescription(messages.key("RM_INFO_STEP_DESCRIPTION"));
		steps.add(informationStep);
		
		ReleaseConfigurationStep sqlStep = new ReleaseConfigurationSQLStep(manifest, messages);
		sqlStep.setTitle(messages.key("RM_SQL_STEP_TITLE"));
		sqlStep.setDescription(messages.key("RM_SQL_STEP_DESCRIPTION"));
		steps.add(sqlStep);
		
		ReleaseConfigurationStep finalStep = new ReleaseConfigurationConfirmStep(manifest, messages);
		finalStep.setTitle(messages.key("RM_CONFIRM_STEP_TITLE"));
		finalStep.setDescription(messages.key("RM_CONFIRM_STEP_DESCRIPTION"));
		steps.add(finalStep);
		
		return steps;
	}
	
	public static ReleaseConfigurationStep getPreReleaseStep(AmazonConfiguration amzConfig,ReleaseManagerConfiguration rfsConfig, String rm,JSONObject manifest, TfsMessages messages){
		
		ReleaseConfigurationStep step =  new ReleaseConfigurationPreUpgradeStep(amzConfig,rfsConfig, rm,manifest, messages);
		//step.setTitle(messages.key("RM_PRE_UPGRADE_STEP_TITLE"));
		//step.setDescription(messages.key("RM_PRE_UPGRADE_STEP_DESCRIPTION"));
		
		return step;
		
	}
}
