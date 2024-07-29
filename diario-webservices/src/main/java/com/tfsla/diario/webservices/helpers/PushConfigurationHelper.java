package com.tfsla.diario.webservices.helpers;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

import com.tfsla.diario.webservices.common.strings.StringConstants;

public class PushConfigurationHelper {

	public synchronized static Boolean isSiteManaged(String site, String publication) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String pushScope = config.getParam(
			site, publication, StringConstants.WEBSERVICES_MODULE_NAME,
			"pushSiteScope"
		);
		if(pushScope == null || pushScope.equals("")) return false;
		return Boolean.parseBoolean(pushScope);
	}
	
	public synchronized static int getPushSize(CmsObject cms, String publication) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		int pushSize = Integer.parseInt(config.getParam(
			cms.getRequestContext().getSiteRoot(),
			publication, StringConstants.WEBSERVICES_MODULE_NAME,
			"pushSize"
		));
		return pushSize;
	}
}
