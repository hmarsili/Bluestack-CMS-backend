package com.tfsla.diario.friendlyTags;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.utils.TfsTagsUtil;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsFechaEventoHastaTag extends A_TfsNoticiaValue {
	
	private static final long serialVersionUID = -4133862252667742185L;

	private static final Log LOG = CmsLog.getLog(TfsFechaEventoDesdeTag.class);

	private final String defaultFormat = " dd/MM/yyyy hh:mm";
	private String format=null;
	private String type = null;

	@Override
	 public int doStartTag() throws JspException {
			I_TfsNoticia noticia = getCurrentNews();
	        
	        String dateLong = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.event.toDate")); 

	        if (dateLong.equals("0"))
	        	printContent("");
	        else {
		        try {
			        Date uModif = new Date(Long.parseLong(dateLong));
			        SimpleDateFormat sdf = null;
			        String content = null;
			        
			        if(type==null) type = "date";
			        
			        if(type.equals("descriptive")){
			        	
			           String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(uModif);
			           content = TfsTagsUtil.getDescriptiveDate(date, format, defaultFormat);
			           
			        }else{
			
				        if (format!=null && !format.equals(""))
				        	sdf = new SimpleDateFormat(format);
				        else
				        	sdf = new SimpleDateFormat(defaultFormat);
				        
				        content = sdf.format(uModif);
			        }
			        
				     printContent(content);
	
			    } catch (NumberFormatException ex) {
			    	LOG.error("error de formato al obtener la fecha desde: ", ex);
			    	printContent("");
			    }
	       }
		 return SKIP_BODY;
	 }

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	



}
