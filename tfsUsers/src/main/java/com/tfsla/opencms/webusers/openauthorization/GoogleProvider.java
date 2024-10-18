package com.tfsla.opencms.webusers.openauthorization;

import jakarta.servlet.http.HttpServletRequest;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.UUID;

public class GoogleProvider extends GenericProvider implements IOpenProvider {
	
	protected HttpServletRequest request;
	protected final String providerName = "google";
	protected final String urlOAuth = "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&response_type=code&scope=%s&redirect_uri=%s&nonce=%s";
	protected final String urlAccessToken = "https://www.googleapis.com/oauth2/v4/token";
	protected final String urlProfile = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=";
	protected String urlCallback;
	protected String clientID;
	protected String clientSecret;
	protected String scope;
	
	public String getProviderName(){
		return this.providerName;
	}
	
	@Override
	protected String getModuleName() {
		return "webusers-google";
	}
	
	public GoogleProvider(HttpServletRequest request) throws Exception {
		try {
			setConfiguration(request);
			
			this.request = request;
	 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	 		
			this.urlCallback = config.getParam(siteName, publication, getModuleName(), "urlCallback");
			this.clientID = config.getParam(siteName, publication, getModuleName(), "clientId");
			this.clientSecret = config.getParam(siteName, publication, getModuleName(), "clientSecret");
			this.scope = config.getParam(siteName, publication, getModuleName(), "scope");
			if (this.scope == null || this.scope.equals("")) {
				this.scope = "openid%20email%20profile";
			}
		} catch(Exception ex) {
			throw ex;
		}
	}
	
	@Override
	public Object getProviderData() throws Exception {
		HttpsURLConnection requestProvider = null;
		JSONObject json = null;
		try {
			// Get OAuth response code
			// See https://developers.google.com/identity/protocols/OpenIDConnect
			String code = (String)this.request.getParameter("code");
			if (code != null) {
				URL url = new URL(this.urlAccessToken);
				requestProvider = (HttpsURLConnection) url.openConnection();
				requestProvider.setRequestMethod("POST");
				
				String urlParameters = String.format("code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
						code,
						this.clientID,
						this.clientSecret,
						this.urlCallback
				);
				
				requestProvider.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(requestProvider.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
				
				if (requestProvider.getResponseCode() != 200) {
	    			StringWriter writer = new StringWriter();
	    			IOUtils.copy(requestProvider.getErrorStream(), writer, "UTF-8");
	    			String errorResponse = writer.toString();
	    			throw new Exception("GoogleProvider Error code " + requestProvider.getResponseCode() + " - response: " + errorResponse);
	    		} 
	    		BufferedReader in = new BufferedReader(
            		new InputStreamReader(requestProvider.getInputStream())
        		);
	    		String decodedString;
	    		String accessTokenResponse = "";
	    		while ((decodedString = in.readLine()) != null) {
	    			accessTokenResponse += decodedString;
	    		}
	    		in.close();
	    		CmsLog.getLog(this).debug("GoogleProvider AccessToken response: " + accessTokenResponse);
	    		JSONObject jsonResponse = JSONObject.fromObject(accessTokenResponse);
	    		access_token = jsonResponse.getString("access_token");

	    		url = new URL(this.urlProfile+access_token);
	    		requestProvider = (HttpsURLConnection)url.openConnection();
	            requestProvider.connect();
	
	    		in = new BufferedReader(
    	            new InputStreamReader(requestProvider.getInputStream())
	            );
	    		
	    		String result = "";
	
	    		while ((decodedString = in.readLine()) != null) {
	    			result += decodedString;
	    		}
	    		in.close();
	    		
	    		json = (JSONObject) JSONSerializer.toJSON( result );
	    		CmsLog.getLog(this).debug("GoogleProvider UserData: "+ result);
			} else {
				throw new Exception("Empty code from Google");
			}
		} catch (Exception e) {
			CmsLog.getLog(this).error(e);
			e.printStackTrace();
			throw e;
		} finally {
			try {
	    		if (requestProvider != null) {
	    			requestProvider.disconnect(); 
	    		}
    		} catch(Exception ex_) {
    			ex_.printStackTrace();
    		}
		}
		return json;
	}
	
	public UserProfileData GetUserProfileData() throws Exception {
		JSONObject json = (JSONObject) this.getProviderData();
		UserProfileData userData = new UserProfileData();
		
		if (json.containsKey("email")) {
    		userData.setEmail(json.getString("email"));
		}
		
		if (json.containsKey("name")) {
			userData.setNickName(json.getString("name"));
		}
		
		if (json.containsKey("given_name")) {
			userData.setFirstName(json.getString("given_name"));
		}
		
		if (json.containsKey("family_name")) {
			userData.setLastName(json.getString("family_name"));
		}
	    
    	if (json.containsKey("picture")) {
			userData.setPicture(json.getString("picture"));
    	}
    	
    	if (json.containsKey("link")) {
    		userData.setUserUrl(json.getString("link"));
    	}
		userData.setAccessToken(access_token);
    	userData.setProviderResponse(json);
    	userData.setKey(json.getString("id"));
    	return userData;
	}
		
	public String GetLoginUrl() throws Exception {
		try {
			String nonce = UUID.randomUUID().toString();
			this.request.getSession().setAttribute("providerName", this.getProviderName());
			
			// https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&response_type=code&scope=%s&redirect_uri=%s&nonce=%s
			// ver https://developers.google.com/identity/protocols/OpenIDConnect?authuser=1
			String ret = String.format(this.urlOAuth,
					this.clientID,
					this.scope,
					this.urlCallback,
					nonce
			);
			return ret;
		} catch (Exception e) {
			CmsLog.getLog(this).error(e);
			e.printStackTrace();
			throw e;
		}
	}
}