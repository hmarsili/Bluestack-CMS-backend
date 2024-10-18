package com.tfsla.diario.friendlyTags;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPrecioFechaValidaTag extends A_TfsNoticiaCollectionValue {
	
	private final String defaultFormat = " dd/MM/yyyy hh:mm";
	private String format=null;
	
	
	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.price.validDate"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String dateLong = collection.getCollectionValue(getCollectionPathName()); 
		 SimpleDateFormat sdf = null;
		 Date uModif = new Date(Long.parseLong(dateLong));
	     String content = null;
		try { 
		  if (format!=null && !format.equals(""))
	        	sdf = new SimpleDateFormat(format);
	        else
	        	sdf = new SimpleDateFormat(defaultFormat);
	        
	       content = sdf.format(uModif);
     
	     printContent(content);
		} catch (Exception ex) {
	    	LOG.error("error de formato al obtener la fecha desde: ", ex);
	    	printContent("");
	    }

		 return SKIP_BODY;
	 }
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	

}
