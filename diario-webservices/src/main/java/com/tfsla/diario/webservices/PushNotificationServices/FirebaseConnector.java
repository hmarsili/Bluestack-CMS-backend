package com.tfsla.diario.webservices.PushNotificationServices;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.IncomingHttpResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.TopicManagementResponse;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;


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
	
	public void pushMessage(Message message) throws Exception {
		
		
		FirebaseApp app = getFirebaseApp();
		String msgId;
		
		try {
			msgId = FirebaseMessaging.getInstance(app).send(message);
			LOG.info("Push message sent, msgId: " + msgId);
		} catch (FirebaseMessagingException ex){
			  IncomingHttpResponse response = ex.getHttpResponse();
			  if (response != null) {
				  
				  LOG.error("FCM service responded with HTTP " + response.getStatusCode());

			    Map<String, Object> headers = response.getHeaders();
			    for (Map.Entry<String, Object> entry : headers.entrySet()) {
			    	LOG.error(">>> " + entry.getKey() + ": " + entry.getValue());
			    }

			    LOG.error(">>>");
			    LOG.error(">>> " + response.getContent());
			  }
		}
		
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
	
	
	protected FirebaseApp getFirebaseApp() throws FileNotFoundException, IOException {
		FirebaseApp app=null;
		
		try {
			app = FirebaseApp.getInstance(this.firebaseKeysPath);
		}
		catch ( java.lang.IllegalStateException ex) {
			List<String> scopes = new ArrayList<String>();
			scopes.add("https://www.googleapis.com/auth/firebase.messaging");
			
			GoogleCredentials googleCredentials = GoogleCredentials
				      .fromStream(new FileInputStream(this.firebaseKeysPath))
				      .createScoped(scopes);
			
			FirebaseOptions options = FirebaseOptions.builder()
				    .setCredentials(googleCredentials)
				    .build();
				
			app = FirebaseApp.initializeApp(options,this.firebaseKeysPath);
		}
		
		return app;
	}
	
	public void addPushSubscriber(String token, String platform, String topic, Boolean saveInDB) throws Exception {
		if (token == null || token.equals("")) {
			return;
		}
		
		
		FirebaseApp app = getFirebaseApp();
			
		
	    // These registration tokens come from the client FCM SDKs.
		List<String> registrationTokens = Arrays.asList(
	    		token
	    );
			/*
		// [START subscribe]
	    // Subscribe the devices corresponding to the registration tokens to the
	    // topic.
		*/
		TopicManagementResponse response = FirebaseMessaging.getInstance(app).subscribeToTopicAsync(
	        registrationTokens, topic).get();
	    // See the TopicManagementResponse reference documentation
	    // for the contents of response.
	    
	    // [END subscribe]
	
		if (response==null || response.getSuccessCount() == 0) {
			LOG.debug("Token: " + String.join(",", registrationTokens));
			LOG.debug("topic: " + topic);
			for (com.google.firebase.messaging.TopicManagementResponse.Error err : response.getErrors()) {
				LOG.error("Error while trying add push a subscriber : " + err.getReason());
			}
			throw new Exception("Error while trying add push a subscriber - message: " + response.getErrors().get(0).getReason());
		}
		
		LOG.debug(response.getSuccessCount() + " tokens were subscribed successfully");
	    
		
		String[] replacements = {"https://android.googleapis.com/gcm/send/", "https://fcm.googleapis.com/fcm/send/"};
		String t = token;
		for (String r : replacements) {
			t = t.replace(r, "");
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
		

		FirebaseApp app = getFirebaseApp();
		
		// These registration tokens come from the client FCM SDKs.
	    List<String> registrationTokens = Arrays.asList(
	    		token
	    );

	    // Unsubscribe the devices corresponding to the registration tokens from
	    // the topic.
	    TopicManagementResponse response = FirebaseMessaging.getInstance(app).unsubscribeFromTopicAsync(
	        registrationTokens, topic).get();
	    
	    
	    
	    // See the TopicManagementResponse reference documentation
	    // for the contents of response.
	    System.out.println(response.getSuccessCount() + " tokens were unsubscribed successfully");
	    // [END unsubscribe]
	    
		
		if (response.getSuccessCount() == 0) {
			throw new Exception("Error while trying add remove a subscriber from topic " + topic + " - message: " + response.getErrors().get(0).getReason());
		}
		
		LOG.info("unsubscribe from notification " + topic + " sent");
		
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

		
		List<String> topics = getTopicsOfClient(token, StringConstants.PLATFORM_WEB);
		
		if (topics!=null && topics.size()>0) {
			
			FirebaseApp app = getFirebaseApp();
			
			// These registration tokens come from the client FCM SDKs.
			List<String> registrationTokens = Arrays.asList(
		    		token
		    );
			
			for (String topic : topics) {
			    
			    // Unsubscribe the devices corresponding to the registration tokens from
			    // the topic.
			    TopicManagementResponse response = FirebaseMessaging.getInstance(app).unsubscribeFromTopicAsync(
			        registrationTokens, topic).get();
			    
			    
			    // See the TopicManagementResponse reference documentation
			    // for the contents of response.
			    System.out.println(response.getSuccessCount() + " tokens were unsubscribed successfully to topic " + topic);
			    // [END unsubscribe]
			    
			}
			
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
	}
	
	public String getTopic() {
		return this.topic;
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
