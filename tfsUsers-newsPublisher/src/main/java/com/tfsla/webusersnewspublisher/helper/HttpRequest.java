package com.tfsla.webusersnewspublisher.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {
	
	public String url;
	public String method = "POST";
	
	public void setUrl(String url){
		this.url = url;
	}	
	
	public String getUrl(){
		return this.url;
	}	
	
	public void setMethod(String method){
		this.method = method;
	}	
	
	public String getMethod(){
		return this.method;
	}	
	
	public HttpRequest(){
		
	}
	
	public String sendRequest() throws Exception {
	
		try{
			String[] urlparts = url.split("\\?");
			URL urlConnection = new URL(urlparts[0]);
			HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
	
			connection.setDoOutput(true);
		    connection.setDoInput(true);
		        
		    connection.setRequestMethod("POST");
		    //connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		        
		    connection.connect();
		  	        
		    StringBuffer respSb = new StringBuffer();
		    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    String line = in.readLine();
	
		    while (line != null){
		    	respSb.append(line);
		        line = in.readLine();
		    }
	
		    in.close();
		        
		    return respSb.toString();	
		}
    	catch (Exception e) {
    		throw e;
    	}		
	}
}
