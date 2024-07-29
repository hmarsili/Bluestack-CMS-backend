package com.tfsla.diario.webservices.PushNotificationServices;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

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
		JSONObject message = getPayloadMessage(jsonObject);
		FirebaseConnector connector = new FirebaseConnector(site, publication);
		connector.setTopic(topic);
		connector.pushMessage(message);
	}
	
	protected JSONObject getPayloadMessage(JSONObject toPush) {
		JSONObject ret = new JSONObject();
		JSONObject message = new JSONObject();
		JSONObject data = new JSONObject();
		if (hasProperty(toPush, "urlredirect")) {
			data.put("click_action", toPush.getString("urlredirect"));
		} else if (hasProperty(toPush, "urlfriendly")) {
			data.put("click_action", toPush.getString("urlfriendly"));
		} else if (hasProperty(toPush, "canonical")) {
			data.put("click_action", toPush.getString("canonical"));
		} else {
			String url = toPush.getString("url").replace(this.site, "");
			data.put("click_action", url);
		}
		if (hasProperty(toPush, "icon")) {
			data.put("icon", toPush.getString("icon"));
		}
		if (hasProperty(toPush, "image")) {
			data.put("image", toPush.getString("image"));
		}

		message.put("data", data);
		
		JSONObject notification = new JSONObject();
		notification.put("title", toPush.getString("title"));
		if (hasProperty(toPush, "subtitle")) {
			notification.put("body", toPush.getString("subtitle"));
		}
		
		message.put("notification", notification);
		
		JSONObject android = new JSONObject();
		android.put("ttl", ttl + "s");
		message.put("android", android);
		
		JSONObject webpush = new JSONObject();
		JSONObject headers = new JSONObject();
		headers.put("Urgency", "high");
		headers.put("TTL", ttl);
		webpush.put("headers", headers);
		
		message.put("webpush", webpush);
		message.put("topic", this.topic);
		
		ret.put("message", message);
		ret.put("validate_only", false);
		
		
		return ret;
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
