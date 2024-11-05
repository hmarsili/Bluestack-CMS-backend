package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import com.tfsla.diario.admin.jsp.TfsMessages;

import net.sf.json.JSONObject;

public class ReleaseConfigurationInfoStep extends ReleaseConfigurationStep {
	
	public ReleaseConfigurationInfoStep(JSONObject manifest, TfsMessages messages) {
		super(manifest, messages);
	}

	@Override
	public int getOrder() {
		return 1;
	}
	
	@Override
	public String getHtmlAction() {
		htmlAction = "<div class='release-config-step info-step'>";
		htmlAction += "<p>";
		htmlAction += manifest.getString("description");
		htmlAction += "</p>";
		htmlAction += "</div>";
		return htmlAction;
	}
}
