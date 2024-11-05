package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import net.sf.json.JSONObject;

import com.tfsla.diario.admin.jsp.TfsMessages;

public class ReleaseConfigurationConfirmStep extends ReleaseConfigurationStep {

	public ReleaseConfigurationConfirmStep(JSONObject manifest, TfsMessages messages) {
		super(manifest, messages);
	}

	@Override
	public int getOrder() {
		return 4;
	}
	
	@Override
	public String getHtmlAction() {
		htmlAction = "<div class='release-config-step release-final-step'>";
		htmlAction += "<input type='button' value='" + messages.key("RM_INSTALL") + "'/>";
		htmlAction += "</div>";
		return htmlAction;
	}
}
