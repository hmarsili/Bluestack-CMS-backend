package com.tfsla.diario.friendlyTags;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

import jakarta.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;


public class TfsTriviaStartDateTag extends A_TfsTriviaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7545254296961257415L;

	protected static final Log LOG = CmsLog.getLog(TfsTriviaStartDateTag.class);
	
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

		I_TfsTrivia trivia = getCurrentTrivia();
        

		String dateLong = getPropertyValue(trivia,TfsXmlContentNameProvider.getInstance().getTagName("trivia.startDate"),false);
	    
		if(dateLong==null || (dateLong !=null && (dateLong.equals("") || dateLong.equals("0"))))
		{
			printContent("");
		}
		else{
			try {
		        Date dStart = new Date(Long.parseLong(dateLong));
		        SimpleDateFormat sdf = null;
		        String content = null;
		        
		        if(type==null) type = "date";
		        
		        if(type.equals("descriptive")){
		        	
		           String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dStart);
		           content = getDescriptiveDate(date);
		           
		        }else{
		
			        if (format!=null && !format.equals(""))
			        	sdf = new SimpleDateFormat(format);
			        else
			        	sdf = new SimpleDateFormat(defaultFormat);
			        
			        content = sdf.format(dStart);
		        }
			     
			    printContent(content);
		    }
		    catch (NumberFormatException ex)
		    {
		    	LOG.debug("Error en nt de fecha de inicio de la trivia: "+ex.getMessage());
		    }
		}
	    
	    return SKIP_BODY;
	 }
	
	protected String getDescriptiveDate(String dateNews) 
    {
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
				
			if(hours >24){
				   String FormatoHoras = formatHour.format(TimeNews);
				   
				   Date dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(dateNews);
				   Calendar calendar = Calendar.getInstance();
				            calendar.setTime(dateTime);
				            
				   Calendar yesterday = Calendar.getInstance();
				            yesterday.add(Calendar.DATE, -1);
				            
				   Calendar beforeyesterday = Calendar.getInstance();
				   			beforeyesterday.add(Calendar.DATE, -2);

				    if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
				    	DateDescriptive = "Ayer a las "+FormatoHoras+" hs";
				    } else if (calendar.get(Calendar.YEAR) == beforeyesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == beforeyesterday.get(Calendar.DAY_OF_YEAR)) {
				    	DateDescriptive = "Anteayer a las "+FormatoHoras+" hs";
				    }else {
				    	
				    	if(hours< 168 ){
							DateDescriptive = "Hace "+ days +" días";
						}else{
							if (format!=null && !format.equals(""))
								DateDescriptive = new SimpleDateFormat(format).format(TimeNews);
						    else
						        DateDescriptive = new SimpleDateFormat(defaultFormat).format(TimeNews);
						}
				   }
			}
				
		}catch (Exception e) { 
			  LOG.error("No se pudo calcular la fecha descriptiva. Error: "+e.getMessage());
		}
			
		return DateDescriptive;
    }
	
}
