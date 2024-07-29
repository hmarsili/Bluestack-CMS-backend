package com.tfsla.diario.auditActions.service.hangout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import net.sf.json.JSONObject;

public class HangoutNotificationSender {
	
	protected static final Log LOG = CmsLog.getLog(HangoutNotificationSender.class);
	
	public static void main(String[] args) {
		
		new HangoutNotificationSender()
			.sendMessage("Mensaje de prueba.");

	}

	private String moduleConfigName = "dashboardConfiguration";
	private String siteName;
	private int publication;
	private CPMConfig config;
	
	String hangoutWebhookUrl = null;
	
	HangoutNotificationSender() {
		String webhookUrl = "https://chat.googleapis.com/v1/spaces/AAAADkmwkCE/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=5z5WQP5m2D9w0Xd1REmePfWdgWywdqFg-68HA2aArdc%3D";	

		this.hangoutWebhookUrl = webhookUrl;
	}
	
	public HangoutNotificationSender(String siteName, int publication) {
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.siteName = siteName;
		this.publication = publication;
		this.hangoutWebhookUrl = config.getParam(siteName,""+publication, moduleConfigName, "hangoutWebhookUrl",null);

	}

	
	public boolean isEnabled() {	
		
		boolean isEnabled = false;
		
		if(hangoutWebhookUrl!=null){
			isEnabled = config.getBooleanParam(siteName, ""+publication, moduleConfigName, "enableHangoutNotification",false);
		}
			
		return isEnabled;
	}
	
	public void sendMessage(String message) {
		if (!isEnabled())
			return;
		
		URL url;
		try {
			
			LOG.debug("Enviando a Hangout: "  + hangoutWebhookUrl);
			
			url = new URL(hangoutWebhookUrl);
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
							   con.setRequestMethod("POST");
							   con.setRequestProperty("Content-Type", "application/json; utf-8");
							   con.setRequestProperty("Accept", "application/json");
							   con.setDoOutput(true);
							   
			String jsonInputString = "{\"text\": \""+message+"\"}";
			
			try(OutputStream os = con.getOutputStream()) {
			    byte[] input = jsonInputString.getBytes("utf-8");
			    os.write(input, 0, input.length);           
			}

			BufferedReader br =
					new BufferedReader(
							new InputStreamReader(con.getInputStream()));

			String input;
			String jsonOBjsStr = "";

			while ((input = br.readLine()) != null){
				jsonOBjsStr += input;
			}
			br.close();
			JSONObject jsonObj = JSONObject.fromObject((Object)jsonOBjsStr);
			
			if (jsonObj.get("error")!= null && !jsonObj.get("error").equals(""))  {
				LOG.error("Error enviando mensaje a Hangout: "  + jsonObj.get("error").toString());
			}
			
		} catch (MalformedURLException e) {
			LOG.error("Error enviando mensaje a hangout", e);
			
		} catch (IOException e) {
			LOG.error("Error enviando mensaje a hangout", e);
		}
		
    }

}
