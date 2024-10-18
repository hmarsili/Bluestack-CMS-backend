package com.tfsla.diario.ediciones.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.jsp.CmsJspActionElement;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public abstract class A_SearchConsoleService  extends CmsJspActionElement {

	/** Application name. */
    protected static final String APPLICATION_NAME =
        "Google Search Console API";
    
    /** Global instance of the HTTP transport. */
    protected static HttpTransport HTTP_TRANSPORT;
    
    /** Global instance of the JSON factory. */
    protected static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();
    
    
    public A_SearchConsoleService() {
		super();
    }
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
