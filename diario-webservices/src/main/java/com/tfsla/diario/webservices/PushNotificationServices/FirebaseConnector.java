package com.tfsla.diario.webservices.PushNotificationServices;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;

import net.sf.json.JSONObject;

public class FirebaseConnector {
	
	public FirebaseConnector(String site, String publication) {
		this.module = PushServiceConfiguration.getModuleName();
		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.topic = this.config.getParam(site, publication, module, "firebaseTopic");
		this.firebaseApiKey = this.config.getParam(site, publication, module, "firebaseApiKey");
		this.firebaseProjectName = this.config.getParam(site, publication, module, "firebaseProjectName");
		this.firebaseKeysPath = this.config.getParam(site, publication, module, "firebaseKeysPath");
		this.site = site;
		this.publication = publication;
		
		this.LOG = CmsLog.getLog(this);
	}
	
	public void pushMessage(JSONObject message) throws Exception {
		String url = "https://fcm.googleapis.com/v1/projects/"+this.firebaseProjectName+"/messages:send";
		LOG.debug("Firebase push URL: "+url);
		HttpURLConnection con = this.getConnection(url);

		String msg = new String(message.toString().getBytes(), "ISO-8859-1");
		LOG.info("Push message to be sent: " + msg);
		
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(msg);
		wr.flush();
		wr.close();
		
		if (con.getResponseCode() != 200) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(con.getErrorStream(), writer, "UTF-8");
			String errorResponse = writer.toString();
			throw new Exception("Error while trying to send a push message, code: " + con.getResponseCode() + " - message: " + errorResponse);
		}
		
		String result = this.getHttpResponse(con);
		LOG.info("Push message sent, response: " + result);
	}
	
	
	public List<String> getTopicsOfClient(String token, String platform) {
		List<String> topics =null;
		
		PushClientDAO dao = new PushClientDAO();
		String t = token;
		String[] replacements = {"https://android.googleapis.com/gcm/send/", "https://fcm.googleapis.com/fcm/send/"};
		
		for (String r : replacements) {
			t = t.replace(r, "");
		}
		try {
			dao.openConnection();
			
			topics =dao.getTopicFromClient(token, platform, site, publication,true);
		} catch (Exception e) {
			LOG.error("Error getting topic from subscriber", e);
			e.printStackTrace();
		} finally {
			dao.closeConnection();
		}
		return topics;
	}
	
	public void addPushSubscriber(String token, String platform, String topic, Boolean saveInDB) throws Exception {
		if (token == null || token.equals("")) {
			return;
		}
		String[] replacements = {"https://android.googleapis.com/gcm/send/", "https://fcm.googleapis.com/fcm/send/"};
		String t = token;
		for (String r : replacements) {
			t = t.replace(r, "");
		}
		String url = String.format("https://iid.googleapis.com/iid/v1/%s/rel/topics/%s", t, topic);
		HttpURLConnection con = this.getConnection(url, true);
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.flush();
		wr.close();
		
		if (con.getResponseCode() != 200) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(con.getErrorStream(), writer, "UTF-8");
			String errorResponse = writer.toString();
			throw new Exception("Error while trying add push a subscriber, code: " + con.getResponseCode() + " - message: " + errorResponse);
		}
		
		if (saveInDB) {
			PushClientDAO dao = new PushClientDAO();
			try {
				dao.openConnection();
				
				if (!dao.userExists(t, platform, topic, site, Integer.valueOf(publication))) {
					dao.registerEndpoint(t, platform, "", topic, site, publication);
				} else {
					dao.updateEndpoint(t, platform, "", topic, site, publication);
				}
				LOG.debug("Push subscriber registered with token " + t);
			} catch (Exception e) {
				LOG.error("Error adding push subscriber", e);
				e.printStackTrace();
			} finally {
				dao.closeConnection();
			}
		}
	}
	
	public void removeTopicPushSubscriber(String token, String topic)  throws Exception {
		

		String url = "https://iid.googleapis.com/iid/v1:batchRemove";
		HttpURLConnection con = this.getConnection(url, true);
		
		String postData = "{\n"
				+ "\"to\": \"/topics/" + topic + "\",\n"  
				+ "\"registration_tokens\": [\"" + token + "\"],\n" + 
				"}";
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		
		
		
		con.getOutputStream().write(postDataBytes);
		
		wr.flush();
		wr.close();
		
		if (con.getResponseCode() != 200) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(con.getErrorStream(), writer, "UTF-8");
			String errorResponse = writer.toString();
			throw new Exception("Error while trying add remove a subscriber from topic " + topic + ", code: " + con.getResponseCode() + " - message: " + errorResponse
					+ "   || Json: " + postData
					);
		}
		
		String result = this.getHttpResponse(con);
		LOG.info("unsubscribe from notification " + topic + " sent, response: " + result);
		
		PushClientDAO dao = new PushClientDAO();
		try {
			dao.openConnection();
			dao.unregisterClient(token, StringConstants.PLATFORM_WEB,topic);
			LOG.debug("Push subscriber removed with token " + token + " from topic " + topic);
		} catch (Exception e) {
			LOG.error("Error removing push subscriber from topic " + topic, e);
			e.printStackTrace();
		} finally {
			dao.closeConnection();
		}
	}
	
	public void removePushSubscriber(String token) throws Exception {
		String url = String.format("https://iid.googleapis.com/v1/web/iid/%s", token);
		HttpURLConnection con = this.getConnection(url, "DELETE", true);
		
		con.setDoOutput(true);
		
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	
		wr.flush();
		wr.close();
		
		PushClientDAO dao = new PushClientDAO();
		try {
			dao.openConnection();
			dao.unregisterClient(token, StringConstants.PLATFORM_WEB);
			LOG.debug("Push subscriber removed with token " + token);
		} catch (Exception e) {
			LOG.error("Error adding push subscriber", e);
			e.printStackTrace();
		} finally {
			dao.closeConnection();
		}
	}
	
	public String getTopic() {
		return this.topic;
	}
	
	protected HttpURLConnection getConnection(String url, Boolean useServerApi) throws IOException {
		return this.getConnection(url, "POST", useServerApi);
	}
	
	protected HttpURLConnection getConnection(String url) throws IOException {
		return this.getConnection(url, "POST", false);
	}
	
	protected HttpURLConnection getConnection(String url, String method, Boolean useServerApi) throws IOException {
		URL urlObject = new URL(url);
		HttpURLConnection con = (HttpURLConnection)urlObject.openConnection();
		con.setRequestMethod("POST");
		if (useServerApi) {
			con.setRequestProperty("Authorization", "key="+this.firebaseApiKey);
		} else {
			String token = this.getAccessToken();
			LOG.debug("Firebase token: "+token);
			con.setRequestProperty("Authorization", "Bearer "+token);
		}
		con.setRequestProperty("Content-Type", "application/json");
		return con;
	}
	
	public String getAccessToken() throws IOException {
		List<String> scopes = new ArrayList<String>();
		scopes.add("https://www.googleapis.com/auth/firebase.messaging");
		
		GoogleCredential googleCredential = GoogleCredential
	      .fromStream(new FileInputStream(this.firebaseKeysPath))
	      .createScoped(scopes);
		googleCredential.refreshToken();
		return googleCredential.getAccessToken();
	}
	
	protected String getHttpResponse(HttpURLConnection con) throws IOException {
		BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream())
        );

		String decodedString;
		String result = "";

		while ((decodedString = in.readLine()) != null) {
			result += decodedString;
		}
		in.close();
		return result;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	
	protected String firebaseApiKey;
	protected String firebaseProjectName;
	protected String firebaseKeysPath;
	protected String topic;
	protected String site;
	protected String publication;
	protected String module;
	protected CPMConfig config;
	protected Log LOG;
}
