package com.tfsla.opencms.webusers.openauthorization;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.openauthorization.common.SocialContact;

public class LinkedInProvider extends GenericContactsProvider {
	
	private HttpServletRequest request;
	private final String USER_AGENT = "Mozilla/5.0";
	private final String providerName = "linkedin";
	private final String urlProfile = "https://api.linkedin.com/v1/people/~:(id,picture-url,first-name,last-name,public-profile-url,email-address%s)?format=json&oauth2_access_token=";
    private final String urlAccessToken = "https://www.linkedin.com/uas/oauth2/accessToken";
    private final String urlLogin = "https://www.linkedin.com/uas/oauth2/authorization";
    private final String urlMessages = "https://api.linkedin.com/v1/people/~/mailbox?oauth2_access_token=";
    private JSONObject providerData;
    private String urlCallback;
	private String apiKey;
	private String apiSecret;
	private String apiPermissions;
	
	public String getProviderName() {
		return this.providerName;
	}
	
	@Override
	protected String getModuleName() {
		return "webusers-linkedin";
	}
	
	public LinkedInProvider(HttpServletRequest request) throws Exception {
		try {
			setConfiguration(request);
			
	 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
			
	 		this.request = request;
			this.urlCallback = config.getParam(siteName, publication, getModuleName(), "urlCallback");
			this.apiKey = config.getParam(siteName, publication, getModuleName(), "apiKey"); 
			this.apiSecret = config.getParam(siteName, publication, getModuleName(), "apiSecret");
			this.apiPermissions = config.getParam(siteName, publication, getModuleName(), "apiPermissions");
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	@Override
	public Object getProviderData() throws Exception {
		HttpURLConnection requestProvider = null;
		JSONObject json = null;
		try {
			String code = (String)this.request.getParameter("code");
			
			if(code != null) {
				HttpClient client = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(String.format("%s?grant_type=%s&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
					this.urlAccessToken,
					"authorization_code",
					code,
					this.urlCallback,
					this.apiKey,
					this.apiSecret
				));
				
				httppost.setHeader("User-Agent", USER_AGENT);
				if(this.getConfiguration() != null && this.getConfiguration().getLocale() != null && !this.getConfiguration().getLocale().equals("")) {
					httppost.setHeader("Accept-Language", this.getConfiguration().getLocale());
	    		}
				
				HttpResponse response = client.execute(httppost);
				HttpEntity entity = response.getEntity();
				
		        InputStream stream = entity.getContent();
	    		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

	    		String decodedString;
	    		String result = "";

	    		while ((decodedString = in.readLine()) != null) {
	    			result += decodedString;
	    		}
	    		in.close();
	    		
	    		JSONObject jsonToken = (JSONObject) JSONSerializer.toJSON( result );
	    		access_token = jsonToken.getString("access_token");
	    		
	    		String urlString = "";
	    		String extraParams = "";
	    		if(this.getConfiguration() != null && this.getConfiguration().getFieldsAsString() != null && !this.getConfiguration().getFieldsAsString().equals("")) {
	    			extraParams += "," + this.getConfiguration().getFieldsAsString();
	    		}
	    		if(this.getConfiguration() != null && this.getConfiguration().getInviteContacts() && !extraParams.trim().contains(",connections")) {
	    			extraParams += ",connections";
	    		}
	    		urlString = String.format(this.urlProfile, extraParams);
	    		urlString += access_token;
	    		
	    		//get profile data
	    		URL url = new URL(urlString);
	    	    requestProvider = (HttpURLConnection)url.openConnection();
	
	            requestProvider.connect();
	
	            in = new BufferedReader(
	    	            new InputStreamReader(
	            	    requestProvider.getInputStream()));
	    			
	    		result = "";
	    		while ((decodedString = in.readLine()) != null) {
	    			result += decodedString;
	    		}
	    		in.close();
	    		
	    		json = (JSONObject) JSONSerializer.toJSON( result );
	    		CmsLog.getLog(this).debug("LinkedInProvider UserData: "+ result);
			}
		} catch(Exception ex) {
			throw ex;
		} finally {
    		try {
	    		if(requestProvider != null) {
	    			requestProvider.disconnect(); 
	    		}
    		} catch(Exception ex_) {
    			ex_.printStackTrace();
    		}
    	}
		providerData = json;
		return json;
	}
	
	public UserProfileData GetUserProfileData() throws Exception {
		JSONObject json = (JSONObject) this.getProviderData();

    	UserProfileData userData = new UserProfileData();
    	userData.setKey(json.getString("id"));
    	userData.setFirstName(json.getString("firstName"));
    	userData.setLastName(json.getString("lastName"));
    	userData.setNickName(json.getString("firstName") + "_" + json.getString("lastName"));
    	userData.setEmail(json.getString("emailAddress"));
    	userData.setAccessToken(access_token);
    	userData.setUserUrl(json.getString("publicProfileUrl"));
    	userData.setProviderResponse(json);
    	
    	try {
	    	if(json.has("pictureUrl") && json.containsKey("pictureUrl")) {
		    	String userPicture = json.getString("pictureUrl");
		    	if(!userPicture.equals("")) {
		    		userData.setPicture(userPicture);
		    	}
	    	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return userData;
	}
	
	public String GetLoginUrl() throws Exception {
		try {
			this.request.getSession().setAttribute("providerName", this.providerName);
			String ret = String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&state=%s", 
				this.urlLogin, this.apiKey, this.urlCallback, this.apiSecret
			);
			if(this.apiPermissions != null && !this.apiPermissions.equals("")){
				ret += "&scope=" + this.apiPermissions;
			}
			CmsLog.getLog(this).debug("LinkedInProvider LoginUrl: " + ret);
			return ret;
		}
		catch(Exception ex) {
			throw ex;
		}
	}

	@Override
	public void inviteContact(CmsUser user, String contactId) {
		SocialContact contact = this.getContact(contactId);
		if(contact == null) return;
		try {
			if(contact.getId().equals(contactId)) {
				String message = String.format(this.getConfiguration().getInviteMessage(), 
						contact.getName(), user.getFullName());
				String xmlBody = String.format(this.getMessageBodyFormat(), 
						contactId, this.getConfiguration().getInviteSubject(), message);
				
				HttpClient client = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(this.urlMessages + this.access_token);
				
				httppost.setHeader("User-Agent", USER_AGENT);
				if(this.getConfiguration() != null && this.getConfiguration().getLocale() != null && !this.getConfiguration().getLocale().equals("")) {
					httppost.setHeader("Accept-Language", this.getConfiguration().getLocale());
	    		}
				StringEntity entity = new StringEntity(xmlBody, "UTF-8");
				entity.setContentType("text/xml");
				httppost.setEntity(entity);
				
				HttpResponse response = client.execute(httppost);
				HttpEntity responseEntity = response.getEntity();
				InputStream stream = responseEntity.getContent();
	    		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

	    		String decodedString;
	    		String result = "";

	    		while ((decodedString = in.readLine()) != null) {
	    			result += decodedString;
	    		}
	    		in.close();
	    		if(result.contains("error")) {
	    			throw new Exception("Hubo un error al enviar la invitación: " + result);
	    		}
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error("Error intentando enviar invite", e);
		}
	}

	private String getMessageBodyFormat() {
		return "" +
		"<?xml version='1.0' encoding='UTF-8'?>" +
		"<mailbox-item>" +
			"<recipients>" +
				"<recipient>" +
					"<person path='/people/%s'/>" +
				"</recipient>" +
			"</recipients>" +
			"<subject>%s</subject>" +
			"<body>%s</body>" +
		"</mailbox-item>";
	}
	
	@Override
	protected List<SocialContact> retrieveContacts() {
		ArrayList<SocialContact> ret = new ArrayList<SocialContact>();
		JSONObject connections = this.providerData.getJSONObject("connections");
		JSONArray connectionsData = connections.getJSONArray("values");
		SocialContact contact = null;
		int length = connectionsData.size();
		
		for(int i = 0; i < length; i++) {
			try {
				JSONObject jsonContact = connectionsData.getJSONObject(i);
				contact = new SocialContact();
				contact.setDescription(jsonContact.getString("headline"));
				contact.setId(jsonContact.getString("id"));
				contact.setName(String.format("%s %s", jsonContact.getString("firstName"), jsonContact.getString("lastName")));
				contact.setPictureUrl(jsonContact.getString("pictureUrl"));
				ret.add(contact);
			} catch(Exception e) {
				continue;
			}
		}
		return ret;
	}
}