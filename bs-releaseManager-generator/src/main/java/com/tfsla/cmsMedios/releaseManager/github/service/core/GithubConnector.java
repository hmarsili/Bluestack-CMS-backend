package com.tfsla.cmsMedios.releaseManager.github.service.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tfsla.cmsMedios.releaseManager.common.ConnectorConfiguration;
import com.tfsla.cmsMedios.releaseManager.github.common.Strings;

public class GithubConnector extends GithubComponent {
	
	public GithubConnector(ConnectorConfiguration config) {
		super(config);
	}
	
	public String get(String urlString) throws IOException {
		this.log.debug(String.format(Strings.REQUEST_FOR_SERVICE, urlString));
		StringBuilder result = new StringBuilder();
		if(urlString.startsWith("/")) {
			urlString = urlString.substring(1);
		}
		
		URL url = new URL(String.format("%srepos/%s/%s/%s", API_URL, config.getOwner(), config.getRepo(), urlString));
		this.log.debug(String.format(Strings.CALLING_URL, url.toString()));
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.addRequestProperty("Authorization", "token " + config.getToken());
		conn.addRequestProperty("Accept", String.format("application/vnd.github.%s+json", config.getVersion()));
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		this.log.debug(String.format(Strings.SERVICE_RESPONSE, url.toString(), result.toString()));
		return result.toString();
	}
	
	public InputStream getBinary(URL url) throws IOException {
		this.log.debug(String.format(Strings.REQUEST_FOR_BINARY_FILE, url.toString()));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.addRequestProperty("Authorization", "token " + config.getToken());
		conn.addRequestProperty("Accept", String.format("application/vnd.github.%s.raw", config.getVersion()));
		
		try {
			InputStream stream = conn.getInputStream();
			this.log.debug(String.format(Strings.BINARY_FILE_AVAILABLE, url.toString(), stream.available()));
			return stream;
		} catch(IOException exception) {
			//404 / 403, try with a custom Github raw URL
			String newUrl = url.toString().replace(GIT_URI, RAW_URI).replace("raw/", "");
			System.out.println("Trying with "+newUrl);
			URL newUri = new URL(newUrl);
			HttpURLConnection newConn = (HttpURLConnection) newUri.openConnection();
			newConn.setRequestMethod("GET");
			newConn.addRequestProperty("Authorization", "token " + config.getToken());
			newConn.addRequestProperty("Accept", String.format("application/vnd.github.%s.raw", config.getVersion()));
			
			InputStream stream = newConn.getInputStream();
			this.log.debug(String.format(Strings.BINARY_FILE_AVAILABLE, url.toString(), stream.available()));
			
			return stream;
		}
	}
	
	public static final String API_URL = "https://api.github.com/";
	public static final String RAW_URI = "raw.githubusercontent.com";
	public static final String GIT_URI = "github.com";
}