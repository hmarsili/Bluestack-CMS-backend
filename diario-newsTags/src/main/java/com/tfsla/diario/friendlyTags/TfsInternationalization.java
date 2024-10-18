package com.tfsla.diario.friendlyTags;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.model.TfsInternationalizationMessages;

public class TfsInternationalization extends BaseTag implements I_TfsMessages{

	private static final long serialVersionUID = 8696633134860786233L;
	
	private String publication=null;
	private String site = null;
	private String lang = null;
	
	TfsInternationalizationMessages messages;
	
	Map<String,String> mapMessages=null;
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}
	
	public String getPublication(){
		return publication;
	}
	
	public void setPublication(String publication){
		this.publication = publication;
	}
	
	@Override
	public int doStartTag() throws JspException {
		
		messages = new TfsInternationalizationMessages(site,publication,lang);
		
		exposeMessages();
		
		return EVAL_BODY_INCLUDE;
	}
	
	private void exposeMessages(){
		
		ResourceBundle results = messages.getMessages().getResourceBundle();
		
		Enumeration<String> keys = results.getKeys();
		
		if (mapMessages==null)
			   mapMessages = new HashMap<String, String>();
		
		while (keys.hasMoreElements()){
			String k = keys.nextElement();
			String content = "";
			
			try {
					
				content = com.tfsla.utils.StringEncoding.fixEncoding(messages.getMessages().keyDefault(k, k));
				
				//content = new String(messages.getMessages().keyDefault(k, k).getBytes("iso-8859-1"), "UTF-8");
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			mapMessages.put(k, content);
		} 
		
		pageContext.getRequest().setAttribute("message", mapMessages );
	}

	@Override
	public TfsInternationalizationMessages getMessages() {

		return messages;
	}

}
