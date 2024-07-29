package com.tfsla.diario.webservices.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

public class FacebookLoginHelper {
	
	/**
	 * Calls the Facebook api to get information about user tokens
	 * @param userToken the user token provided to get the information to
	 * @param appToken server instance FB app token
	 * @return JSON response from FB service call
	 * @throws Exception
	 */
	public static synchronized JSONObject getTokenInformation(String userToken, String appToken) throws Exception {
		URL url = new URL(String.format("%s?input_token=%s&access_token=%s",
			URL_DEBUG_TOKEN,
			userToken,
			appToken
		));
		HttpURLConnection requestProvider = (HttpURLConnection)url.openConnection();
		requestProvider.connect();
		
		if (requestProvider.getResponseCode() != 200) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(requestProvider.getErrorStream(), writer, "UTF-8");
			String errorResponse = writer.toString();
			throw new Exception("Error while trying to retrieve token information, code: " + requestProvider.getResponseCode() + " - message: " + errorResponse);
		}
		
		BufferedReader in = new BufferedReader(
            new InputStreamReader(requestProvider.getInputStream())
        );
			
		String decodedString;
		String result = "";

		while ((decodedString = in.readLine()) != null) {
			result += decodedString;
		}
		in.close();
		
		return (JSONObject)JSONSerializer.toJSON(result);
	}
	
	/**
	 * Retrieves the application token to be used to call facebook services
	 * @param siteName the site the configuration will be retrieved for
	 * @param publication the publication the configuration will be retrieved for
	 * @return a String containing the app token
	 * @throws Exception
	 */
	public static synchronized String getAppToken(String siteName, String publication) throws Exception {
		String apiClientId = config.getParam(siteName, publication, "webusers-facebook", "apiClientid"); 
		String apiSecretKey = config.getParam(siteName, publication, "webusers-facebook", "apiSecretkey");
		return apiClientId + "|" + apiSecretKey;
	}

	public static synchronized JSONObject getProviderData(String access_token, String siteName, String publication) throws Exception {
		HttpURLConnection requestProvider = null;
		JSONObject json = null;
		
		try {
			String fieldsRequest = "";
			try {
				String fields = config.getParam(siteName, publication, "webusers-facebook", "fieldsToRetrieve");
				if(fields != null && !fields.equals(""))
					fieldsRequest = "&fields=" + fields;
			} catch(Exception e) {
				e.printStackTrace();
			}
					
    		String urlString = String.format(urlProfile, getApiVersion(siteName, publication)) + access_token + fieldsRequest;
    		URL url = new URL(urlString);
    	    requestProvider = (HttpURLConnection)url.openConnection();
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
    		
    		json = (JSONObject) JSONSerializer.toJSON( result );
    		CmsLog.getLog(FacebookLoginHelper.class).debug("FacebookLoginHelper UserData: " + result);
		} catch(Exception ex) {
			ex.printStackTrace();
			CmsLog.getLog(FacebookLoginHelper.class).error("Error obtaining provider data", ex);
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

	private static String getApiVersion(String siteName, String publication) {
		try {
			String version = config.getParam(siteName, publication, "webusers-facebook", "apiVersion");
			if(version != null && !version.equals("")) {
				return "v" + version;
			}
			return "v2.2";
		} catch(Exception e) {
			e.printStackTrace();
			return "v2.2";
		}
	}
	
	private static CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private static final String URL_DEBUG_TOKEN = "https://graph.facebook.com/debug_token";
	private static final String urlProfile = "https://graph.facebook.com/%s/me?access_token=";
}
