package com.tfsla.diario.friendlyTags;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.jsp.JspException;


public class TfsFechaModificacionAudioTag  extends A_TfsNoticiaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8588292992494282386L;
	private final String defaultFormat = " dd/MM/yyyy hh:mm";
	private String format=null;
	private String type = null;

	@Override
	 public int doStartTag() throws JspException {


		 I_TfsCollectionListTag collection = getCurrentCollection();
	     setKeyName("lastmodifieddate");

	     String dateLong = collection.getCollectionValue("lastmodifieddate"); 

		    try {
		        Date uModif = new Date(Long.parseLong(dateLong));
		        SimpleDateFormat sdf = null;
		        String content = null;
		        
		        if(type==null) type = "date";
		        
		        if(type.equals("descriptive")){
		        	
		           String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(uModif);
		           content = getDescriptiveDate(date);
		           
		        }else{
		
			        if (format!=null && !format.equals(""))
			        	sdf = new SimpleDateFormat(format);
			        else
			        	sdf = new SimpleDateFormat(defaultFormat);
			        
			        content = sdf.format(uModif);
		        }
		        
			     printContent(content);

		    }
		    catch (NumberFormatException ex)
		    {
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
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	 protected String getDescriptiveDate(String dateNews) 
     {
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
