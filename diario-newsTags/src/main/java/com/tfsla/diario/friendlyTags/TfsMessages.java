package com.tfsla.diario.friendlyTags;

import java.util.Map;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.model.TfsInternationalizationMessages;

public class TfsMessages extends A_TfsMessagesValueTag{

	private static final long serialVersionUID = 8696633134860786233L;
	
	private String publication=null;
	private String site = null;
	private String lang = null;
	private String key = null;
	
	TfsInternationalizationMessages messages;
	
	Map<String,String> mapMessages=null;
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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
		
		String content = messages.keyDefault(key, key);
		printContent(content);
		
		return SKIP_BODY;
	}

}
