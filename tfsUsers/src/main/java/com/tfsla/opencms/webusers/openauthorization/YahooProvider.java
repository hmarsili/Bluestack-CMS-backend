package com.tfsla.opencms.webusers.openauthorization;

import javax.servlet.http.HttpServletRequest;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.*;

import java.util.List;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;

public class YahooProvider extends GenericProvider implements IOpenProvider {
	
	private HttpServletRequest request;
	private final String providerName = "yahoo";
	private final String urlRequest = "http://me.yahoo.com";
	private final String urlAttributeEmail = "http://axschema.org/contact/email";
	private final String urlAttributeFirstName = "http://axschema.org/namePerson/first";
	private final String urlAttributeLastName = "http://axschema.org/namePerson/last";
	private String urlCallback;
	private String urlVerification;

	public String getProviderName(){
		return this.providerName;
	}
	
	@Override
	protected String getModuleName() {
		return "webusers-yahoo";
	}
	
	public YahooProvider(HttpServletRequest request) throws Exception {
		try {
	 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	 		this.request = request;
	 		this.urlCallback = config.getParam(siteName, publication, getModuleName(), "urlCallback");
			this.urlVerification = config.getParam(siteName, publication, getModuleName(), "urlVerification");
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
	
	@Override
	public Object getProviderData() throws Exception {
		try{			
		    ConsumerManager manager = (ConsumerManager)this.request.getSession().getAttribute("consumermanager");

		    // extract the parameters from the authentication response
		    // (which comes in as a HTTP request from the OpenID provider)
		    ParameterList responselist = new ParameterList(this.request.getParameterMap());

		    // retrieve the previously stored discovery information
		    DiscoveryInformation discovered = (DiscoveryInformation) this.request.getSession().getAttribute("openid-disco");
		        
		    // verify the response; ConsumerManager needs to be the same
		    // (static) instance used to place the authentication request
		    VerificationResult verification = manager.verify(this.urlVerification, responselist, discovered);

		    // examine the verification result and extract the verified identifier
		    //Identifier verified = verification.getVerifiedId();
		    
	        AuthSuccess authSuccess = (AuthSuccess)verification.getAuthResponse();
	        
	        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
			    return (FetchResponse)authSuccess.getExtension(AxMessage.OPENID_NS_AX);
	        }
	        else
	        	throw new Exception("Body without extension");
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
	
	public UserProfileData GetUserProfileData() throws Exception {
	    FetchResponse fetchResp = (FetchResponse)this.getProviderData();
	
	    List emails = fetchResp.getAttributeValues("email");
	    String email = (String) emails.get(0);
	    
	    //List firstnames = fetchResp.getAttributeValues("firstname");
	    //String firstName = (String) firstnames.get(0);
	    
	    //List lastnames = fetchResp.getAttributeValues("lastname");
	    //String lastName = (String) lastnames.get(0);
	    
	    UserProfileData userData = new UserProfileData();
	    userData.setKey(email);			    
	    userData.setFirstName(email.substring(0, email.indexOf("@")));
	    userData.setNickName(email.substring(0, email.indexOf("@")));
    	userData.setEmail(email);
    	userData.setProviderResponse(fetchResp);
    	
    	return userData;
	}
	
	public String GetLoginUrl() throws Exception {
		try{
			ConsumerManager manager= new ConsumerManager();
			
		    // perform discovery on the user-supplied identifier
		    List discoveries = manager.discover(this.urlRequest);
		
		    // attempt to associate with an OpenID provider
		    // and retrieve one service endpoint for authentication
		    DiscoveryInformation discovered = manager.associate(discoveries);
				
		    // obtain a AuthRequest message to be sent to the OpenID provider
		    AuthRequest authReq = manager.authenticate(discovered, this.urlCallback);
		
		    // Attribute Exchange example: fetching the 'email' attribute
		    FetchRequest fetch = FetchRequest.createFetchRequest();
		    
			fetch.addAttribute("email", this.urlAttributeEmail, true);
			fetch.addAttribute("firstname", this.urlAttributeFirstName, true);
			fetch.addAttribute("lastname", this.urlAttributeLastName, true);		
					    
		    //fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
		    // attach the extension to the authentication request
		    authReq.addExtension(fetch);
		
		    // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
		    // The only method supported in OpenID 1.x
		    // redirect-URL usually limited ~2048 bytes
		    String url = authReq.getDestinationUrl(true);
			
		    this.request.getSession().setAttribute("consumermanager", manager);
		    this.request.getSession().setAttribute("openid-disco", discovered);		    
	        this.request.getSession().setAttribute("providerName", this.providerName);
	        
	        return url;
		}
		catch(Exception ex)
		{
			throw ex;
		}  
	}
}