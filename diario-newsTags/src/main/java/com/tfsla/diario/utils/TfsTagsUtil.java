package com.tfsla.diario.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencms.file.CmsObject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.history.CmsHistoryProject;

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

public class TfsTagsUtil {
	
	 public static String getDescriptiveDate(String dateNews, String format, String defaultFormat) {
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
	 
	public static Date getPublishResourceDate(String path, CmsObject cms) {
		 
		 Date publishDate = null;
		 
		 try {
			 if(cms.readAllAvailableVersions(path).size()>0){
				 I_CmsHistoryResource histRes = (I_CmsHistoryResource) cms.readAllAvailableVersions(path).get(0);
		         int publishTag = histRes.getPublishTag();
		         CmsHistoryProject project = cms.readHistoryProject(publishTag); 
		         
		         Long dateLong = project.getPublishingDate();
		         publishDate = new Date(dateLong);
			 }
	         
		 } catch (CmsException e) {
				CmsLog.getLog(cms).error("Error al obtener fecha de publicación del recurso"+path+" ERROR:"+e.getMessage());
		 }
		   
		 return publishDate;
 		
	 }
	
	//entrega el long del tiempo 
	public static long  getTransformationTime (String time, Integer quantity) {
			if (time.toLowerCase().equals("segundos") || time.toLowerCase().equals("seconds") )
				return 1000*quantity;
			else if (time.toLowerCase().equals("minutos") || time.toLowerCase().equals("minutes"))
				return 60*1000*quantity;
			else if (time.toLowerCase().equals("horas") || time.toLowerCase().equals("hours"))
				return 60*60*1000*quantity;
			return 0;
	}
	

}
