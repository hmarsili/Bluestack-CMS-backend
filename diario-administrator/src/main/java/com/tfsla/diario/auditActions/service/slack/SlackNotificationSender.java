package com.tfsla.diario.auditActions.service.slack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.workflow.QueryBuilder;

public class SlackNotificationSender {
	
	protected static final Log LOG = CmsLog.getLog(SlackNotificationSender.class);

	
	public static void main(String[] args) {
		
		new SlackNotificationSender()
			.sendMessage("Enviando un mensaje de prueba");

	}

	private String moduleConfigName = "dashboardConfiguration";
	private String siteName;
	private int publication;
	private CPMConfig config;
	
	String slackWebhookUrl = null;
	
	SlackNotificationSender() {
		String webhookUrl = "https://hooks.slack.com/services/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";
		this.slackWebhookUrl = webhookUrl;
	}
	
	public SlackNotificationSender(String siteName, int publication) {
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.siteName = siteName;
		this.publication = publication;
		this.slackWebhookUrl = config.getParam(siteName,""+publication, moduleConfigName, "slackWebhookUrl",null);

	}

	
	public boolean isEnabled() {	
		
		boolean isEnabled = false;
		
		if(slackWebhookUrl!=null){
			isEnabled = config.getBooleanParam(siteName, ""+publication, moduleConfigName, "enableSlackNotification",false);
		}
			
		return isEnabled;
				
	}
	
	public void sendMessage(String message) {
		if (!isEnabled())
			return;
		
		URL url;
		try {
			
			LOG.debug("Enviando a Slack: "  + slackWebhookUrl);
			
			url = new URL(slackWebhookUrl);
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
			String responseStr = "";

			while ((input = br.readLine()) != null){
				responseStr += input;
			}
			br.close();
			
			if (!responseStr.equals("ok")) {
				LOG.error("Error enviando mensaje a slack: "  + responseStr);
			}
			
		} catch (MalformedURLException e) {
			LOG.error("Error enviando mensaje a slack", e);
			
		} catch (IOException e) {
			LOG.error("Error enviando mensaje a slack", e);
		}
		
    }
	
	

}
