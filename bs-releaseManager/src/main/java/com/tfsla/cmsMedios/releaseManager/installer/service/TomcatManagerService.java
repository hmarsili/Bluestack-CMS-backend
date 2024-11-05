package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.tfsla.cmsMedios.releaseManager.installer.common.TomcatManagerConfiguration;

public class TomcatManagerService {
	
	private static Boolean isReloading = false;
	
	public static Boolean isTomcatReloading() {
		return isReloading;
	}
	
	public static String getServerInfo(TomcatManagerConfiguration config) throws IOException {
		return call(config, "text/serverinfo");
	}
	
	public static String getServerStatus(TomcatManagerConfiguration config) throws IOException {
		return call(config, "status/all");
	}
	
	public static String getConnectorInfo(TomcatManagerConfiguration config) throws IOException {
		return call(config, "text/sslConnectorCiphers");
	}
	
	public static String getVMInfo(TomcatManagerConfiguration config) throws IOException {
		return call(config, "text/vminfo");
	}
	
	public static String getApplications(TomcatManagerConfiguration config) throws IOException {
		return call(config, "text/list");
	}
	
	public static void reloadAsync(TomcatManagerConfiguration tomcatManagerConfiguration) {
		isReloading = true;
		class ReloadTomcatThread implements Runnable {
			TomcatManagerConfiguration config;
			ReloadTomcatThread(TomcatManagerConfiguration c) { config = c; }
	        public void run() {
				try {
					SetupProgressService.reportProgress("Reloading Tomcat App...");
		            String userpass = config.getUsername() + ":" + config.getPassword();
		            
					Thread.sleep(2000);
					
					URL url = new URL (config.getAppEndpoint()+"text/reload?path=/");
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		            connection.setRequestMethod("GET");
		            connection.setDoOutput(true);
		            connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(userpass.getBytes())));
		            connection.connect();
		            
		            int responseCode = connection.getResponseCode();
		            SetupProgressService.reportProgress("Tomcat App reloaded, code: " + connection.getResponseCode());
		            StringWriter writer = new StringWriter();
		            InputStream stream = responseCode != 200 ? connection.getErrorStream() : connection.getInputStream();
		            IOUtils.copy(stream, writer, "UTF-8");
	    			SetupProgressService.reportProgress("Response: " +  writer.toString());
	    			stream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	            
	        }
	    }
	    Thread t = new Thread(new ReloadTomcatThread(tomcatManagerConfiguration));
	    t.start();
	}
	
	public static String call(TomcatManagerConfiguration config, String operation) throws IOException {
		URL url = new URL (config.getAppEndpoint()+operation);
		String userpass = config.getUsername() + ":" + config.getPassword();
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(userpass.getBytes())));
        connection.connect();
        int responseCode = connection.getResponseCode();
        StringWriter writer = new StringWriter();
        InputStream stream = responseCode != 200 ? connection.getErrorStream() : connection.getInputStream();
        IOUtils.copy(stream, writer, "UTF-8");
		SetupProgressService.reportProgress("Response: " +  writer.toString());
		stream.close();
		
		return writer.toString();
	}
}
