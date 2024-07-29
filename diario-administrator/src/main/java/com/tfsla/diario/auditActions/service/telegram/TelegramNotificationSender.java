package com.tfsla.diario.auditActions.service.telegram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;


import net.sf.json.JSONObject;



public class TelegramNotificationSender {


	protected static final Log LOG = CmsLog.getLog(TelegramNotificationSender.class);
	
	public static void main(String[] args) {
		
		String botId = "972147670:AAEKVeOVXzqE5jEkwq8tlUmfKXFARoEk_SM";		
		String chatId = "@cmsmediosDev";

		new TelegramNotificationSender(botId,chatId)
			.sendMessage("Enviando un mensaje de prueba");

	}

	private String moduleConfigName = "dashboardConfiguration";
	private String siteName;
	private int publication;
	private CPMConfig config;
	

	String botId = null;		
	String chatId = null;

	TelegramNotificationSender(String botId,String chatId) {
		this.botId = botId;
		this.chatId = chatId;
		
	}
	
	public TelegramNotificationSender(String siteName, int publication) {
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.siteName = siteName;
		this.publication = publication;
	}

	public String getChatId() {
		return 
				chatId==null ?
						config.getParam(siteName, ""+publication, moduleConfigName, "chatIdTelegram","")
							:
							chatId;
	}
	
	public String getBotId() {
		return 
				botId==null ?
						config.getParam(siteName, ""+publication, moduleConfigName, "botIdTelegram","")
							:
							botId;
	}
	
	public boolean isEnabled() {		
		return 
				botId!=null ? 
						true :
						config.getBooleanParam(siteName, ""+publication, moduleConfigName, "enableTelegramNotification",false);
	}
	
	
	public void sendMessage(String message) {
		
		if (!isEnabled())
			return;
		

		String httpsURL;
		URL url;
		try {
			httpsURL = "https://api.telegram.org/bot" +
					getBotId() +
					"/sendMessage" +
					"?chat_id=" + URLEncoder.encode(getChatId(), "UTF-8") + 
					"&text=" + URLEncoder.encode(message, "UTF-8");


			LOG.debug("Enviando a telegram: "  + httpsURL);
			url = new URL(httpsURL);
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

			BufferedReader br =
					new BufferedReader(
							new InputStreamReader(con.getInputStream()));

			String input;
			String jsonOBjsStr = "";

			while ((input = br.readLine()) != null){
				jsonOBjsStr += input;
				//System.out.println(input);
			}
			br.close();
			JSONObject jsonObj = JSONObject.fromObject((Object)jsonOBjsStr);
			
			if (!(Boolean)jsonObj.get("ok")) {
				LOG.error("Error enviando mensaje a telegram: "  + jsonOBjsStr);
			}
			
			
		} catch (MalformedURLException e) {
			LOG.error("Error enviando mensaje a telegram", e);
			
		} catch (IOException e) {
			LOG.error("Error enviando mensaje a telegram", e);
		}


	}

}
