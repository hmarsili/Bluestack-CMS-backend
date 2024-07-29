package com.tfsla.diario.webservices.PushNotificationServices;

import javax.servlet.http.HttpServletRequest;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

import com.tfsla.diario.webservices.common.interfaces.IUnsubscribePushClientService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;
import com.tfsla.diario.webservices.helpers.PublicationHelper;

public class UnsubscribePushClientService implements IUnsubscribePushClientService {

	public UnsubscribePushClientService(CmsObject cms) {
		this.cms = cms;
	}
	
	@Override
	public void unsubscribe(String token, String platform) throws Exception {
		if(token == null || token.trim().equals("")) {
			throw new Exception(ExceptionMessages.ERROR_MISSING_TOKEN);
		}
		PushClientDAO dao = new PushClientDAO();
		try {
			dao.openConnection();
			dao.unregisterClient(token, platform);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			dao.closeConnection();
		}
	}
	
	public void unsubscribe(String token, String platform, String topic, String site, int publication) throws Exception {
		
		this.assertStringParam(token, ExceptionMessages.ERROR_MISSING_TOKEN);
		this.assertStringParam(site, ExceptionMessages.ERROR_MISSING_SITE);
		if(publication == 0) throw new Exception(ExceptionMessages.ERROR_MISSING_PUBLICATION);
		
		try {
			FirebaseConnector connector = new FirebaseConnector(site, String.valueOf(publication));
			connector.removeTopicPushSubscriber(token, topic);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		
	}

	public void unsubscribe(String token, String platform, String topic, HttpServletRequest request) throws Exception {
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
		
		unsubscribe(token, platform, topic, defaultSite, defaultPublication);
	}

	private void assertStringParam(String param, String exceptionMessage) throws Exception {
		if(param == null || param.trim().equals("")) {
			throw new Exception(exceptionMessage);
		}
	}
	
	private CmsObject cms;
	
	private static final CPMConfig _CONFIG = CmsMedios.getInstance().getCmsParaMediosConfiguration();

}
