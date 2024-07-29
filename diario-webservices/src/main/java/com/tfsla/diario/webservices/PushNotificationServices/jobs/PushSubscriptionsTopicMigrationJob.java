package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.webservices.PushNotificationServices.AmazonSNSConnector;
import com.tfsla.diario.webservices.PushNotificationServices.PushServiceConfiguration;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;
import com.tfsla.diario.webservices.helpers.PushConfigurationHelper;

public class PushSubscriptionsTopicMigrationJob implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String publication = parameters.get("publication").toString();
		String site = cms.getRequestContext().getSiteRoot();
		String module = PushServiceConfiguration.getModuleName();
		CPMConfig cpmConfig = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String amzPushTopicArn = cpmConfig.getParam(site, publication, module, "amzPushTopicArn", "");

		LOG.info("Starting job on site <b>" + site + "</b>, publication <b>" + publication + "</b><br/><hr/>");

		PushClientDAO dao = new PushClientDAO();
		dao.openConnection();
		int subscribed = 0;
		int errors = 0;
		try {
			AmazonSNSConnector snsConnector = new AmazonSNSConnector(site, publication);
			List<String> clients = dao.getClients(StringConstants.PLATFORM_WEB, site, publication, !PushConfigurationHelper.isSiteManaged(site, publication));
			LOG.info("Sending over <b>" + clients.size() + " subscribers</b> to Amazon, topic ARN: <b>" + amzPushTopicArn + "</b><br/>");
			for(String client : clients) {
				try {
					snsConnector.addPushSubscriber(client, StringConstants.PLATFORM_WEB);
					subscribed++;
				} catch (Exception e) {
					LOG.info("Error subscribing client " + client + ": " + e.getMessage() + "<br/>");
					errors++;
				}
			}
		} catch (Exception e) {
			LOG.info("Unhandled exception: " + e.getMessage() + "<br/>");
		} finally {
			dao.closeConnection();
		}

		LOG.info("<br/><hr/><br/><b>Job finished, " + subscribed + " subscriptions and " + errors + " errors</b><br/>");
		
		return null;
	}

	private Log LOG = CmsLog.getLog(this);
}
