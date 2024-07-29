package com.tfsla.diario.webservices.PushNotificationServices;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.notnoop.apns.*;
import com.tfsla.diario.webservices.common.strings.LogMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.PushNotificationService;
import com.tfsla.diario.webservices.data.PushClientDAO;

/**
 * Manages Apple push notifications, see https://github.com/notnoop/java-apns
 */
public class ApplePushNotificationService extends PushNotificationService {

	@Override
	protected String getPlatform() {
		return StringConstants.PLATFORM_APPLE;
	}

	@Override
	public void startMessageService() {
		String certPath = this.config.getParam(site, publication, module, "appleCertPath");
		if(certPath.toLowerCase().contains("dev.p12")) {
			this.service = APNS.newService()
			    .withCert(
		    		certPath, 
		    		this.config.getParam(site, publication, module, "applePassword")
	    		)
			    .withSandboxDestination()
			    .withDelegate(new AppleNotificationsEvents(this.LOG))
			    .build();
		} else {
			this.service = APNS.newService()
			    .withCert(
			    		certPath, 
		    		this.config.getParam(site, publication, module, "applePassword")
	    		)
			    .withProductionDestination()
			    .withDelegate(new AppleNotificationsEvents(this.LOG))
			    .build();
		}
	}

	public void removeInvalidClients() {
		Map<String,Date> devices = this.service.getInactiveDevices();
		if(devices != null && devices.keySet().size() > 0) {

			PushClientDAO dao = new PushClientDAO();
			dao.openConnection();
			try {
				for(String token : devices.keySet()) {
					LOG.debug(String.format(LogMessages.REMOVING_APPLE_CLIENT, token));
					try {
						dao.unregisterClient(token, this.getPlatform());
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				dao.closeConnection();
			}
		}
	}
	
	@Override
	public void stopMessageService() {
		try {
			this.service.stop();
		}catch(Exception e) {
			e.printStackTrace();
		}
		this.service = null;
	}

	@Override
	protected void sendMessage(String client, Hashtable<String, String> message) throws Exception {
		String payload = this.getPayload(message);
		service.push(client, payload);
	}
	
	@Override
	protected void sendMessage(List<String> clients, Hashtable<String, String> message) throws Exception {
		String payload = this.getPayload(message);
		service.push(clients, payload);
	}
	
	private String getPayload(Hashtable<String, String> message) {
		PayloadBuilder payloadBuilder = APNS.newPayload().alertBody(message.get("title"));
		for(String key : message.keySet()) {
			if(key.equals("title")) continue;
			payloadBuilder.customField(key, message.get(key));
		}
		return payloadBuilder.build();
	}
	
	private ApnsService service;
}
