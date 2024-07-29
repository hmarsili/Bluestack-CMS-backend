package com.tfsla.opencms.webusers.openauthorization;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.tfsla.opencms.webusers.Encrypter;
import com.tfsla.opencms.webusers.TfsUserHelper;

public class TwitterProvider extends GenericProvider implements IOpenProvider {
	
	private HttpServletRequest request;
	private final String providerName = "twitter";
	private final String profileUrl = "https://api.twitter.com/1.1/account/verify_credentials.json";
	private final String urlAddTweet = "https://api.twitter.com/1.1/statuses/update.json";	
    private final String urlRequest = "https://api.twitter.com/oauth/request_token";
    private final String urlAccess = "https://api.twitter.com/oauth/access_token";
    private final String urlAuthorize = "https://api.twitter.com/oauth/authenticate";
    
	private String apiKey;
	private String apiSecret;
	private String urlCallack;
	private OAuthConsumer consumer;
	
	public String getProviderName() {
		return this.providerName;
	}
	
	@Override
	protected String getModuleName() {
		return "webusers-twitter";
	}
	
	public TwitterProvider(HttpServletRequest request) throws Exception {
		try {
			setConfiguration(request);
			
	 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	
	 		this.request = request;
			this.apiKey = config.getParam(siteName, publication, getModuleName(), "apiKey"); 
			this.apiSecret = config.getParam(siteName, publication, getModuleName(), "apiSecret");
			this.urlCallack = config.getParam(siteName, publication, getModuleName(), "urlCallback");
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
	
	@Override
	public Object getProviderData() throws Exception {
		try {
	        consumer = (OAuthConsumer)this.request.getSession().getAttribute("consumer");
	    	OAuthProvider provider = (OAuthProvider)this.request.getSession().getAttribute("provider");	
	    	
	    	CmsLog.getLog(this).debug("Twitter Consumer: "+consumer.getConsumerKey()+" | "+consumer.getConsumerSecret());
	    	
	    	CmsLog.getLog(this).debug("Verifier: "+this.request.getParameter("oauth_verifier"));
	    	
	    	provider.retrieveAccessToken(consumer, this.request.getParameter("oauth_verifier"));
	    	
	    	DefaultHttpClient httpclient = new DefaultHttpClient();
	    	HttpGet request = new HttpGet(profileUrl);
	    	
	    	consumer.sign(request);
	    	HttpResponse response = httpclient.execute(request);	
	    	
	    	CmsLog.getLog(this).debug("Status : "+ response.getStatusLine().getStatusCode() +" - "+response.getStatusLine().getReasonPhrase());
	    	
	    	BufferedReader in = new BufferedReader(
	        	            new InputStreamReader(
	        	            		response.getEntity().getContent()));
	       
    		String decodedString;
    		String result = "";

    		while ((decodedString = in.readLine()) != null) {
    			result += decodedString;
    		}
    		in.close();
    		
    		CmsLog.getLog(this).debug("TwitterProvider UserData: "+result);
    		
    		return JSONSerializer.toJSON(result);	  
		}
		catch(Exception ex) {
			throw ex;
		}
	}	
	
	public UserProfileData GetUserProfileData() throws Exception {
		JSONObject json = (JSONObject) this.getProviderData();	  
		
		CmsLog.getLog(this).debug("TwitterProvider Json: "+json.toString());
        	    	
    	UserProfileData userData = new UserProfileData();
    	userData.setKey(json.getString("id"));
    	userData.setFirstName(json.getString("name"));
    	userData.setNickName(json.getString("screen_name"));
    	userData.setAccessToken(consumer.getToken());
    	userData.setAccessSecret(consumer.getTokenSecret());
    	userData.setPicture(json.getString("profile_image_url"));
    	userData.setUserUrl("http://twitter.com/" + json.getString("screen_name"));
    	userData.setProviderResponse(json);
    	
    	return userData;
	}
		
	public String GetLoginUrl() throws Exception {
		try{
			String secret = null;
	        String token = null;

			OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
					this.apiKey,
	                this.apiSecret);
	        
	        OAuthProvider provider = new CommonsHttpOAuthProvider(
	        		this.urlRequest,
	                this.urlAccess,
	                this.urlAuthorize
	        		);
	       
	        String authUrl = provider.retrieveRequestToken(consumer,this.urlCallack);
	        
	        token = consumer.getToken();
	        secret = consumer.getTokenSecret();
	        
	        consumer.setTokenWithSecret(token, secret);
	        
	        this.request.getSession().setAttribute("consumer", consumer);
	        this.request.getSession().setAttribute("provider", provider);
	        this.request.getSession().setAttribute("providerName", this.providerName);
	        
	        return authUrl;
		}
		catch(Exception ex){
			throw ex;
		}
	}
	
	public String submitTweet(TfsUserHelper user,  String content) throws Exception{
		
		String result ="";
		
		OAuthConsumer consumer = new DefaultOAuthConsumer(this.apiKey, this.apiSecret);

		String accessToken = user.getValorAdicional("USER_OPENAUTHORIZATION_TWITTER_ACCESS_TOKEN");
		       accessToken = Encrypter.decrypt(accessToken);
				
		String accessSecret = user.getValorAdicional("USER_OPENAUTHORIZATION_TWITTER_ACCESS_SECRET");
		       accessSecret = Encrypter.decrypt(accessSecret);
		        
		       consumer.setTokenWithSecret(accessToken, accessSecret);

		String           msg = URLEncoder.encode(content, "UTF-8");
		String urlParameters = "status=" + msg;
		
		URL url = new URL(urlAddTweet + "?" + urlParameters);
			
		HttpURLConnection requestProvider = (HttpURLConnection)url.openConnection();
				          requestProvider.setRequestMethod("POST");
				
		consumer.sign(requestProvider);
		requestProvider.connect();
        
		result = requestProvider.getResponseCode()+" - "+requestProvider.getResponseMessage();
		
		return result;
	}

	
}