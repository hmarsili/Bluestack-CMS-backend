package com.tfsla.diario.model;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import org.opencms.i18n.CmsMessages;

import com.tfsla.messages.Messages;

public class TfsInternationalizationMessages{
	
	private Locale m_locale;
	private String site = null;
	private String publication = null;
	private CmsMessages messages = null;
	
	
	public TfsInternationalizationMessages(String siteName, String publicationID, String locale)
    {
		 site = siteName;
		 publication = publicationID;
		 
		 if(locale!=null && !locale.equals("")){
			m_locale = new Locale(locale);
		 	messages = Messages.get(site,publication).getBundle(this.m_locale);
		 }else
			 messages = Messages.get(site,publication).getBundle();
    }
	
	public Locale GetLocale(){
	    return m_locale;	
	}
	
	public void setLocale(Locale locale){
		this.m_locale = locale;
	}
	
   public String key(String key) {
	   
		try {
			
			String msg = com.tfsla.utils.StringEncoding.fixEncoding(messages.key(key));
			
			return msg;
			//return new String(messages.key(key).getBytes("iso-8859-1"), "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return messages.key(key);
   }
   
   public String keyDefault(String keyName, String defaultValue) {
	   
	   try {
		   
		   String msg = com.tfsla.utils.StringEncoding.fixEncoding(messages.keyDefault(keyName,defaultValue));
			
		   return msg;
		   //return new String(messages.keyDefault(keyName,defaultValue).getBytes("iso-8859-1"), "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return messages.keyDefault(keyName,defaultValue);
	   
   }
   
   public CmsMessages getMessages(){
	   return messages;
   }
   
   public void setMessages(CmsMessages messages){
	   this.messages = messages;
   }
   
}
