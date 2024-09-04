package com.tfsla.diario.webservices.PushNotificationServices;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidConfig.Priority;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;

import net.sf.json.JSONObject;

/**
 * Manages Push Notifications with Firebase, see https://firebase.google.com/docs/cloud-messaging/http-server-ref 
 */
public class FirebasePushNotificationService {

	public FirebasePushNotificationService(String site, String publication) {
		this.module = PushServiceConfiguration.getModuleName();
		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.apiKey = this.config.getParam(site, publication, module, "firebaseApiKey");
		this.topic = this.config.getParam(site, publication, module, "firebaseTopic");
		this.ttl = this.config.getParam(site, publication, module, "pushTTL","3600");
		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.site = site;
		this.publication = publication;
		this.LOG = CmsLog.getLog(this);
	}

	public void pushMessage(JSONObject jsonObject) throws Exception {
		Message message = getPayloadMessage(jsonObject);
		FirebaseConnector connector = new FirebaseConnector(site, publication);
		connector.setTopic(topic);
		connector.pushMessage(message);
	}
	
	protected Message getPayloadMessage(JSONObject toPush) {
		
		Message.Builder messageBuilder = Message.builder();
		Notification.Builder notificationBuilder = Notification.builder();
		
		AndroidConfig.Builder androidConfigBuilder = AndroidConfig.builder();
		WebpushConfig.Builder webpushConfigBuilder = WebpushConfig.builder();
		
		
		AndroidNotification.Builder androidNotificationBuilder = AndroidNotification.builder();
		WebpushNotification.Builder webpushNotificationBuilder = WebpushNotification.builder();
		
		messageBuilder.setTopic(topic);
		
		/*
		Message message = Message.builder()
			    .putData("score", "850")
			    .putData("time", "2:45")
			    .setToken(registrationToken)
			    .build();
		*/

		
		
		notificationBuilder.setTitle(toPush.getString("title"));
		if (hasProperty(toPush, "subtitle")) {
			notificationBuilder.setBody(toPush.getString("subtitle"));
		}

		
		
		androidConfigBuilder.setPriority(Priority.HIGH);
		androidConfigBuilder.setTtl(Long.parseLong(ttl));
		
		
		if (hasProperty(toPush, "icon")) {
			androidNotificationBuilder.setIcon(toPush.getString("icon"));
			messageBuilder.putData("icon",toPush.getString("icon"));
		}
		String url = "";
		if (hasProperty(toPush, "urlredirect")) {
			url = toPush.getString("urlredirect");
		} else if (hasProperty(toPush, "urlfriendly")) {
			url = toPush.getString("urlfriendly");
		} else if (hasProperty(toPush, "canonical")) {
			url = toPush.getString("canonical");
		} else {
			url = toPush.getString("url").replace(this.site, "");
			
		}
		androidNotificationBuilder.setClickAction(url);
		messageBuilder.putData("click_action",url);
		
		if (hasProperty(toPush, "image")) {
			notificationBuilder.setImage(toPush.getString("image"));
			androidNotificationBuilder.setImage(toPush.getString("image"));
			webpushNotificationBuilder.setImage(toPush.getString("image"));
			messageBuilder.putData("image",toPush.getString("image"));
		}
		
		webpushConfigBuilder.setNotification(webpushNotificationBuilder.build());
		androidConfigBuilder.setNotification(androidNotificationBuilder.build());
		
		messageBuilder.setAndroidConfig(androidConfigBuilder.build());
		messageBuilder.setWebpushConfig(webpushConfigBuilder.build());
		messageBuilder.setNotification(notificationBuilder.build());
		
		return messageBuilder.build();
	}
	
	protected Boolean hasProperty(JSONObject jsonObject, String key) {
		return jsonObject.containsKey(key) && jsonObject.getString(key) != null && !jsonObject.getString(key).equals("");
	}
	
	public String getTopic() {
		return topic;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	protected String apiKey;
	protected String topic;
	protected String site;
	protected String ttl;
	protected String publication;
	protected String module;
	protected CPMConfig config;
	protected Log LOG;
}
