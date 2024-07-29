package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.webservices.PushNotificationServices.FirebaseConnector;
import com.tfsla.diario.webservices.PushNotificationServices.PushServiceConfiguration;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;
import com.tfsla.diario.webservices.helpers.PushConfigurationHelper;

public class PushSubscriptionsFirebaseMigrationJob implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String publication = parameters.get("publication").toString();
		String site = cms.getRequestContext().getSiteRoot();
		String module = PushServiceConfiguration.getModuleName();
		CPMConfig cpmConfig = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String topic = cpmConfig.getParam(site, publication, module, "firebaseTopic", "");

		LOG.info("Starting job on site " + site + ", publication " + publication);

		PushClientDAO dao = new PushClientDAO();
		dao.openConnection();
		int subscribed = 0;
		int errors = 0;
		try {
			FirebaseConnector snsConnector = new FirebaseConnector(site, publication);
			List<String> clients = dao.getClients(StringConstants.PLATFORM_WEB, site, publication, !PushConfigurationHelper.isSiteManaged(site, publication));
			LOG.info("Sending over " + clients.size() + " subscribers to Firebase, topic: " + topic + "");
			for(String client : clients) {
				try {
					snsConnector.addPushSubscriber(client, StringConstants.PLATFORM_WEB, topic, false);
					subscribed++;
					LOG.info(String.format("Subscribed client %s from %s", subscribed, clients.size()));
				} catch (Exception e) {
					LOG.info("Error subscribing client no. " + subscribed + ", token: " + client + ": " + e.getMessage());
					errors++;
				}
			}
		} catch (Exception e) {
			LOG.info("Unhandled exception: " + e.getMessage() + "<br/>");
		} finally {
			dao.closeConnection();
		}

		LOG.info("Job finished, " + subscribed + " subscriptions and " + errors + " errors");
		
		return null;
	}

	private Log LOG = CmsLog.getLog(this);
}
