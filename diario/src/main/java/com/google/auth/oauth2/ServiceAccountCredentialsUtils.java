package com.google.auth.oauth2;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpTransportFactory;
import com.google.api.client.http.HttpTransport;

import java.util.List;

public class ServiceAccountCredentialsUtils {
	
	private String client_id;
	private String client_email;
	private String private_key;
    private String private_key_id;
    private HttpTransportFactory transportFactory = OAuth2Utils.HTTP_TRANSPORT_FACTORY;
    //private final URI tokenServerUri = OAuth2Utils.TOKEN_SERVER_URI;
    
    
    
    public ServiceAccountCredentialsUtils() {
    	
    }
    
    public static ServiceAccountCredentialsUtils createInstance() {
    	return new ServiceAccountCredentialsUtils();
    }
    
	public ServiceAccountCredentialsUtils setClientId(String client_id) {
		this.client_id = client_id;
		return this;
	}

	public ServiceAccountCredentialsUtils setPrivateKey(String private_key) {
		this.private_key = private_key;
		return this;
	}

	public ServiceAccountCredentialsUtils setPrivateKeyId(String private_key_id) {
		this.private_key_id = private_key_id;
		return this;
	}

	public ServiceAccountCredentialsUtils setClientEmail(String client_email) {
		this.client_email = client_email;
		return this;
	}

	public GoogleCredentials getServiceAccountCredentials() throws IOException {
		
		Map<String, Object> json = new HashMap<String, Object>();

		 json.put("client_id",client_id);
		 json.put("client_email",client_email);
		 json.put("private_key",private_key);
		 json.put("private_key_id",private_key_id);
		 
		 return ServiceAccountCredentials.fromJson(
			      json, transportFactory);
				 
				 
		 
		//PrivateKey privateKey = ServiceAccountCredentials.privateKeyFromPkcs8(private_key);

		//return new ServiceAccountCredentials(
	    //    	client_id, client_email, privateKey, private_key_id, null, null, null, null
	    //    );

		/*
		return new ServiceAccountCredentials(
		        	client_id, client_email, privateKey, private_key_id, null, transportFactory, tokenServerUri, null
		        );
		 */
	}
	
	public GoogleCredentials getServiceAccountCredentials(List<String> scopes) throws IOException{
		
		Map<String, Object> json = new HashMap<String, Object>();
		
		json.put("client_id", client_id);
		json.put("client_email", client_email);
		json.put("private_key", private_key);
		json.put("private_key_id", private_key_id);
		
		/*List<String> scopes = new ArrayList<String>();
	    scopes.add("https://www.googleapis.com/auth/cloud-platform");
	    scopes.add("https://www.googleapis.com/auth/cloud-platform.read-only");
		*/
		return ServiceAccountCredentials.fromJson(json, transportFactory).createScoped(scopes);
	}
	
}
