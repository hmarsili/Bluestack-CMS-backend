package com.tfsla.diario.newsletters.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tfsla.diario.newsletters.common.INewsletterHtmlRetriever;

public class NewsletterHtmlRetriever implements INewsletterHtmlRetriever {

	@Override
	public String getHtml(String uri) throws Exception {
		URL url = new URL(uri);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
		httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");
        BufferedReader in = new BufferedReader(
    		new InputStreamReader(httpcon.getInputStream())
        );

        String inputLine = "";
        String content = "";
        while ((inputLine = in.readLine()) != null) {
        	content += inputLine;
        }
        in.close();
        
        return content;
	}

}
