package com.tfsla.diario.webservices.PushNotificationServices;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

import com.tfsla.diario.webservices.common.interfaces.*;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.helpers.PublicationHelper;

public class RegisterPushClientService implements IRegisterPushClientService {

	public RegisterPushClientService(CmsObject cms) {
		this.cms = cms;
	}
	
	@Override
	public void register(String token, String email, String platform, String topic, String additionalInfo, HttpServletRequest request) throws Exception {
		String defaultSite = request.getParameter(StringConstants.SITE);
		if(defaultSite == null || defaultSite.equals("")) {
			defaultSite = _CONFIG.getParam(
				this.cms.getRequestContext().getSiteRoot(),
				PublicationHelper.getCurrentPublication(cms),
				StringConstants.WEBSERVICES_MODULE_NAME,
				"defaultSite"
			);
		}
		int defaultPublication = 0;
		String publication = request.getParameter(StringConstants.PUBLICATION);
		if(publication == null || publication.equals("")) {
			publication = _CONFIG.getParam(
					this.cms.getRequestContext().getSiteRoot(),
					PublicationHelper.getCurrentPublication(cms),
					StringConstants.WEBSERVICES_MODULE_NAME,
					"defaultPublication"
			);
		}
		defaultPublication = Integer.parseInt(publication);
		this.register(token, email, platform, topic, additionalInfo, defaultSite, defaultPublication);
	}

	@Override
	public void register(String token, String email, String platform, String topic, String additionalInfo, String site, int publication) throws Exception {
		this.assertStringParam(token, ExceptionMessages.ERROR_MISSING_TOKEN);
		this.assertStringParam(site, ExceptionMessages.ERROR_MISSING_SITE);
		if(publication == 0) throw new Exception(ExceptionMessages.ERROR_MISSING_PUBLICATION);
		
		try {
			FirebaseConnector connector = new FirebaseConnector(site, String.valueOf(publication));
			connector.addPushSubscriber(token, platform, topic, true);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	private void assertStringParam(String param, String exceptionMessage) throws Exception {
		if(param == null || param.trim().equals("")) {
			throw new Exception(exceptionMessage);
		}
	}
	
	private CmsObject cms;
	private static final CPMConfig _CONFIG = CmsMedios.getInstance().getCmsParaMediosConfiguration();
}
