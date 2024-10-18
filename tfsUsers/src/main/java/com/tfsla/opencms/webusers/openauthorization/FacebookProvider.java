package com.tfsla.opencms.webusers.openauthorization;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.Encrypter;
import com.tfsla.opencms.webusers.TfsUserHelper;
import com.tfsla.opencms.webusers.openauthorization.common.SocialContact;

import java.net.URLEncoder;

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
import java.io.StringWriter;

public class FacebookProvider extends GenericContactsProvider {
	
	private String fieldsRequest = "";
	private String apiVersion = "v2.3/";
	private final String apiVersionFormat = "v%s/";
	private final String providerName = "facebook";
	private final String urlLogin = "https://graph.facebook.com/%soauth/authorize?client_id=%s&redirect_uri=%s&display=popup";
	private final String urlAccessToken = "https://graph.facebook.com/%soauth/access_token?client_id=%s&redirect_uri=%s&client_secret=%s&code=%s";
	private final String urlProfile = "https://graph.facebook.com/%sme?access_token=";
	private final String urlFriends = "https://graph.facebook.com/%sme/friends?access_token=";
	private final String urlAddPost = "https://graph.facebook.com/%s%s/feed?access_token=%s";
	private final String urlUserPictureFormat = "https://graph.facebook.com/%s/picture?type=large&redirect=false";
	private final String urlUserPicture = "https://graph.facebook.com/%s/picture?type=large";
	
	private String urlCallback;
	private String apiClientId;
	private String apiSecretKey;
	private String apiPermissions;
	
	public String getProviderName() {
		return this.providerName;
	}
	
	@Override
	protected String getModuleName() {
		return "webusers-facebook";
	}
	
	public FacebookProvider(HttpServletRequest request) throws Exception {
		try {
			setConfiguration(request);
			
	 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	 		this.request = request;
			this.urlCallback = config.getParam(siteName, publication, getModuleName(), "urlCallback");
			this.apiClientId = config.getParam(siteName, publication, getModuleName(), "apiClientid"); 
			this.apiSecretKey = config.getParam(siteName, publication, getModuleName(), "apiSecretkey");
			this.apiPermissions = config.getParam(siteName, publication, getModuleName(), "apiPermissions");
			
			try {
				String version = config.getParam(siteName, publication, getModuleName(), "apiVersion");
				if(version != null && !version.equals("")) {
					this.apiVersion = String.format(this.apiVersionFormat, version);
				}
				String fields = config.getParam(siteName, publication, getModuleName(), "fieldsToRetrieve");
				if(fields != null && !fields.equals("")) {
					this.fieldsRequest = "&fields=" + fields;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		} catch(Exception ex) {
			CmsLog.getLog(this).error(ex);
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
				
				//Retrieve access token
	    		URL url = new URL(
    				String.format(this.urlAccessToken, this.apiVersion, this.apiClientId, this.urlCallback, this.apiSecretKey, code)
				);
	    		requestProvider = (HttpURLConnection)url.openConnection();
	    		requestProvider.connect();
	            
	    		//Agregado para ver el error 400 que se esta registrando en los logs. faltaria armar los correspondientes manejos de error si hacen falta.
	    		if (requestProvider.getResponseCode() != 200) {
	    			StringWriter writer = new StringWriter();
	    			IOUtils.copy(requestProvider.getErrorStream(), writer, "UTF-8");
	    			String errorResponse = writer.toString();
	    			throw new Exception("FacebookProvider Error code " + requestProvider.getResponseCode() + " - response: " + errorResponse);
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
	    		CmsLog.getLog(this).debug("FacebookProvider AccessToken response: " + accessTokenResponse);
	    		JSONObject jsonResponse = JSONObject.fromObject(accessTokenResponse);
	    		access_token = jsonResponse.getString("access_token");
	    		
	    		String urlString = String.format(this.urlProfile, this.apiVersion) + access_token + fieldsRequest;
	    		if(this.getConfiguration() != null && this.getConfiguration().getLocale() != null && !this.getConfiguration().getLocale().equals("")) {
	    			urlString += "&locale=" + this.getConfiguration().getLocale();
	    		}
	    		
	    		CmsLog.getLog(this).debug("FacebookProvider DataUrl: " + urlString);
	    		//get profile data
	    		url = new URL(urlString);
	    	    requestProvider = (HttpURLConnection)url.openConnection();
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
	    		CmsLog.getLog(this).debug("FacebookProvider UserData: "+ result);
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
		return json;
	}
	
	public UserProfileData GetUserProfileData() throws Exception {
		JSONObject json = (JSONObject) this.getProviderData();

    	UserProfileData userData = new UserProfileData();
    	if(json.containsKey("id"))
    		userData.setKey(json.getString("id"));
    	if(json.containsKey("first_name"))
    		userData.setFirstName(json.getString("first_name"));
    	if(json.containsKey("last_name"))
    		userData.setLastName(json.getString("last_name"));
    	if(json.containsKey("first_name") && json.containsKey("last_name"))
    		userData.setNickName(json.getString("first_name") + "_" + json.getString("last_name"));
    	if(json.containsKey("email"))
    		userData.setEmail(json.getString("email"));
    	if(json.containsKey("link"))
    		userData.setUserUrl(json.getString("link"));
    	
    	userData.setAccessToken(access_token);
    	userData.setProviderResponse(json);
    	
    	try {
	    	String userPicture = getUserPicture(json.getString("id"));
	    	if(!userPicture.equals("")) {
	    		userData.setPicture(userPicture);
	    	}
    	} catch(java.io.IOException ex) {
    		CmsLog.getLog(this).error(ex);
    	} catch(Exception ex) {
    		CmsLog.getLog(this).error(ex);
    		for(StackTraceElement st : ex.getStackTrace()) {
    			CmsLog.getLog(this).debug(st.getLineNumber() + " " + st.toString());
    		}
    	}
    	
    	return userData;
	}
	
	public String GetLoginUrl() throws Exception {
		try {
			this.request.getSession().setAttribute("providerName", this.providerName);
			String ret = String.format(this.urlLogin, this.apiVersion, this.apiClientId, this.urlCallback);
			if(this.apiPermissions != null && !this.apiPermissions.equals("")){
				ret += "&scope=" + this.apiPermissions;
			}
			CmsLog.getLog(this).debug("FacebookProvider LoginUrl: " + ret);
			return ret;
		}
		catch(Exception ex) {
			throw ex;
		}
	}
	
	public String submitPost(TfsUserHelper user,  String comment, String url) throws Exception {
		HttpURLConnection requestProvider = null;
		
		try {
			comment = URLEncoder.encode(comment, "UTF-8");
			url = URLEncoder.encode(url, "ISO-8859-1");
		
			String accessToken = user.getValorAdicional("USER_OPENAUTHORIZATION_FACEBOOK_ACCESS_TOKEN");
			accessToken = Encrypter.decrypt(accessToken);
			
			String userIdProvider = user.getValorAdicional("USER_OPENAUTHORIZATION_PROVIDER_FACEBOOK_KEY");
			userIdProvider = Encrypter.decrypt(userIdProvider);
			
			String urlString = String.format(this.urlAddPost, this.apiVersion, userIdProvider, accessToken);
			URL urlSubmit = new URL(urlString);
			String params = String.format("access_token=%s&link=%s&message=%s", accessToken, url, comment);
			byte[] postData = params.getBytes("UTF-8");
			requestProvider = (HttpURLConnection)urlSubmit.openConnection();
			requestProvider.setRequestMethod("POST");  
			requestProvider.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			requestProvider.setRequestProperty("Content-Length", String.valueOf(postData.length));
			requestProvider.setDoOutput(true);
			requestProvider.getOutputStream().write(postData);
	        
	        String result = "";
	        
	        if(requestProvider.getResponseCode() != 200) {
	        	result += requestProvider.getResponseCode() + " - " + requestProvider.getResponseMessage();
	        } else {
		        InputStream inputStream = requestProvider.getInputStream();
		        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
				String decodedString;
				while ((decodedString = in.readLine()) != null) {
					result += decodedString;
				}
				in.close();
	        }
			
			return result;
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
	}
	
	private String getUserPicture(String userId) throws Exception {
		URL url = new URL(String.format(urlUserPictureFormat, userId));
		HttpURLConnection requestProvider = (HttpURLConnection)url.openConnection();
        requestProvider.connect();

        BufferedReader in = new BufferedReader(
            new InputStreamReader(requestProvider.getInputStream())
        );
			
		String decodedString;
		String result = "";

		while ((decodedString = in.readLine()) != null) {
			result += decodedString;
		}
		in.close();

		JSONObject json = (JSONObject) JSONSerializer.toJSON(result);
		if(json.containsKey("error")) return "";
		
    	JSONObject imageData = json.getJSONObject("data");
    	if(!imageData.getString("is_silhouette").equals("false")) return "";
    	
    	return imageData.getString("url");
	}

	@Override
	public void inviteContact(CmsUser user, String contactId) {
		// La API de Facebook no soporta envío de mensajes internos
	}

	@Override
	protected List<SocialContact> retrieveContacts() {
		ArrayList<SocialContact> ret = new ArrayList<SocialContact>();
		try {
			String urlString = String.format(this.urlFriends, this.apiVersion) + access_token;
			if(this.getConfiguration() != null && this.getConfiguration().getLocale() != null && !this.getConfiguration().getLocale().equals("")) {
				urlString += "&locale=" + this.getConfiguration().getLocale();
			}
			URL url = new URL(urlString);
			HttpURLConnection requestProvider = (HttpURLConnection)url.openConnection();

            requestProvider.connect();

            BufferedReader in = new BufferedReader(
	            new InputStreamReader(requestProvider.getInputStream())
            );
    			
    		String decodedString;
    		String result = "";

    		while ((decodedString = in.readLine()) != null) {
    			result += decodedString;
    		}
    		in.close();
    		
    		JSONObject connections = (JSONObject) JSONSerializer.toJSON( result );
    		CmsLog.getLog(this).debug("FacebookProvider Contacts: "+ result);
    		
    		JSONArray connectionsData = connections.getJSONArray("data");
    		SocialContact contact = null;
    		for(int i = 0; i < connectionsData.size(); i++) {
    			try {
    				JSONObject jsonContact = connectionsData.getJSONObject(i);
    				contact = new SocialContact();
    				//contact.setDescription("");
    				contact.setId(jsonContact.getString("id"));
    				contact.setName(jsonContact.getString("name"));
    				contact.setPictureUrl(String.format(urlUserPicture, jsonContact.getString("id")));
    				ret.add(contact);
    			} catch(Exception e) {
    				continue;
    			}
    		}
		} catch(Exception e) {
			CmsLog.getLog(this).error("FacebookProvider Error retrieving contacts: " + e.getMessage(), e);
		}
		
		return ret;
	}
}