package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

import net.sf.json.JSONObject;

public class PurgeFirebaseTokens implements I_CmsScheduledJob {
	
	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String site = cms.getRequestContext().getSiteRoot();
		String publication = parameters.get("publication").toString();
		String module = PushServiceConfiguration.getModuleName();
		CPMConfig cfg = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String apiKey = cfg.getParam(site, publication, module, "firebaseApiKey");
		String topic = cfg.getParam(site, publication, module, "firebaseTopic");
		int batchSize = 100;
		if (parameters.containsKey("batchSize")) {
			batchSize = Integer.parseInt(parameters.get("batchSize").toString());
		}
		int sleepTime = 1000;
		if (parameters.containsKey("sleepTime")) {
			sleepTime = Integer.parseInt(parameters.get("sleepTime").toString());
		}
		int subscribed = 0;
		int errors = 0;
		int http503 = 0;
		int not_subscribed = 0;
		int not_found = 0;
		int total = 0;
		int current = 1;
		
		FirebaseConnector connector = new FirebaseConnector(site, publication);
		
		PushClientDAO dao = new PushClientDAO();
		dao.openConnection();
		try {
			List<String> clients = dao.getClients(StringConstants.PLATFORM_WEB, site, publication, !PushConfigurationHelper.isSiteManaged(site, publication));
			total = clients.size();
			LOG.info("Will process " + total + " firebase subscriptions to topic " + topic + "");
			for (String token : clients) {
				try {
					String url = String.format("https://iid.googleapis.com/iid/v1/%s?details=true", token);
					LOG.info(String.format("Processing token %s (%s of %s)", token, current, total));
					URL urlObject = new URL(url);
					HttpURLConnection con = (HttpURLConnection)urlObject.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Authorization", "key="+apiKey);
					con.setRequestProperty("Content-Type", "application/json");
					con.setDoOutput(true);
					DataOutputStream wr = new DataOutputStream(con.getOutputStream());
					wr.flush();
					wr.close();
					
					if (con.getResponseCode() == 404) {
						not_found++;
						LOG.info("404: Not found, removing "+token);
						dao.unregisterClient(token, StringConstants.PLATFORM_WEB);
					} else if (con.getResponseCode() == 503) {
						LOG.info("HTTP 503 while processing "+token);
						http503++;
					} else if (con.getResponseCode() != 200) {
						StringWriter writer = new StringWriter();
						IOUtils.copy(con.getErrorStream(), writer, "UTF-8");
						String errorResponse = writer.toString();
						LOG.error("Error obteniendo datos de token, code: " + con.getResponseCode() + " - description: " + errorResponse);
						dao.unregisterClient(token, StringConstants.PLATFORM_WEB);
						errors++;
					} else {
						BufferedReader in = new BufferedReader(
				            new InputStreamReader(con.getInputStream())
				        );
					
						String decodedString;
						String result = "";
				
						while ((decodedString = in.readLine()) != null) {
							result += decodedString;
						}
						in.close();
						LOG.debug(result);
						JSONObject json = JSONObject.fromObject(result);
						if (json.containsKey("rel")) {
							JSONObject rel = json.getJSONObject("rel");
							if (rel.containsKey("topics")) {
								JSONObject topics = rel.getJSONObject("topics");
								if (topics.containsKey(topic)) {
									subscribed++;
									LOG.debug("Subscribed: "+subscribed);
									continue;
								}
							}
						}
						not_subscribed++;
						connector.addPushSubscriber(token, StringConstants.PLATFORM_WEB, topic, false);
						LOG.info(String.format("Adding subscription for token %s to topic %s", token, topic));
					}
				} catch (Exception e) {
					LOG.error("Error procesando token "+token, e);
				} finally {
					current++;
				}
				
				if (current % batchSize == 0) {
					Thread.sleep(sleepTime);
				}
			}
		} catch (Exception e) {
			LOG.error("Error en proceso de purga", e);
		} finally {
			dao.closeConnection();
		}
		String result = String.format("PROCESS FINISHED, Tokens processed: %s, Subscribed: %s (%s %%), Errors: %s (%s %%), Not Subscribed: %s (%s %%), Not Found: %s (%s %%), HTTP 503: %s (%s %%)", 
				current-1, subscribed, subscribed*100/current, 
				errors, errors*100/current,
				not_subscribed, not_subscribed*100/current,
				not_found, not_found*100/current,
				http503, http503*100/current);
		LOG.info(result);
		return result;
	}

	private Log LOG = CmsLog.getLog(this);
}
