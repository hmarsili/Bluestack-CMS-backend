package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.jsp.JspException;

import com.tfsla.opencmsdev.encuestas.Encuesta;

public class TfsEncuestaFechaPublicacionTag extends A_TfsEncuestaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9046781134527902475L;

	private final String defaultFormat = " dd/MM/yyyy hh:mm";
	private String format=null;
	private String type = null;
	
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

	@Override
    public int doStartTag() throws JspException {		
    	try {
    		String content = null;
    		I_TfsEncuesta encuesta = getCurrentEncuesta();
    		
    		if (encuesta!=null)
    		{
    			Encuesta poll = encuesta.getEncuesta();
    			if (poll!=null)
    			{
    				if(type==null) type = "date";
    		        
    		        if(type.equals("descriptive")){
    		           Date uModif = new Date(Long.parseLong(poll.getFechaPublicacion()));	
    		           String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(uModif);
    		           content = getDescriptiveDate(date);
    		           
    		        }else{	 
	    		        Date uModif = new Date(Long.parseLong(poll.getFechaPublicacion()));
	    		        SimpleDateFormat sdf = null;
	
	    		        if (format!=null && !format.equals(""))
	    		        	sdf = new SimpleDateFormat(format);
	    		        else 
	    		        	sdf = new SimpleDateFormat(defaultFormat);
	    		        
	    		        content = sdf.format(uModif);    		        
    		        }
    		        pageContext.getOut().print(content);
    			}
    		}
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }
	
	protected String getDescriptiveDate(String dateNews) 
    {
		@SuppressWarnings("unused")
		SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yy");
		SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm");
		
		String DateDescriptive = "";
		
		try {  
		
			Date TimeNews = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(dateNews); 
			Date currentTime = new Date();
			
			
			long lrefer = TimeNews.getTime(); 
			long lnow = currentTime.getTime(); 
			
			long minutes = (lnow - lrefer)/(1000*60); 
			long seconds = minutes*60;
			long hours = minutes/60; 
			long days = hours/24; 
			
			if (seconds<60){
				DateDescriptive = "Hace "+seconds+" segundos";
			}
			
			if(seconds >59 &&  minutes<60){
			  if(minutes ==1){
				  DateDescriptive = "Hace "+minutes+" minuto";
			  }else{
				  DateDescriptive = "Hace "+minutes+" minutos";
			  }
			}
			
			if(minutes >59 &&  hours <=24){
			  if(hours ==1){
				  DateDescriptive = "Hace "+hours+" hora";
			  }else{
				  DateDescriptive = "Hace "+hours+" horas";
			  }
			}
			
			if(hours >24 &&  hours<48){
			   String FormatoHoras = formatHour.format(TimeNews);
			   DateDescriptive = "Ayer a las "+FormatoHoras+" hs";
			}
			
			if(hours>=48 && hours< 168 ){
				DateDescriptive = "Hace "+ days +" días";
			}
			
			if(hours>=168){
				if (format!=null && !format.equals(""))
					DateDescriptive = new SimpleDateFormat(format).format(TimeNews);
		        else
		        	DateDescriptive = new SimpleDateFormat(defaultFormat).format(TimeNews);
			}
			
		}catch (Exception e) { 
		  //System.out.println("Unable to parse date stamp");
		}
		
		return DateDescriptive;
	}	

}
