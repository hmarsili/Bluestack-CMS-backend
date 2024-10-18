package com.tfsla.diario.friendlyTags;


import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Locale;

import jakarta.servlet.jsp.JspException;

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.utils.TfsTagsUtil;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.diario.utils.Messages;

public class  TfsRecipeTotalTimeTag extends A_TfsNoticiaValue {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String separator = " "; 
	private String format = "";
	private String unityTotalTime = null;
	
	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String tiempoCocciontotal = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.tiempoCoccionTotal")); 
        long total;
        
        String tiempoCoccion = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.tiempoCoccion")); 
        String tiempoPreparacion = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.tiempoPreparacion")); 
      
        String tiempoCoccionUnity = unityValue(tiempoCoccion);
        String tiempoPreparacionUnity = unityValue(tiempoPreparacion);
        
        unityTotalTime = unityTotalTime(tiempoCoccionUnity,tiempoPreparacionUnity, format);
        
       if (tiempoCocciontotal != null && !tiempoCocciontotal.equals("")){
        	total = Long.parseLong(tiempoCocciontotal);
       }else{
	         
	        if (tiempoCoccion== null)
	        	tiempoCoccion = "0";
	        if (tiempoPreparacion == null)
	        	tiempoPreparacion = "0";
	        
	        long coccion = longValue(tiempoCoccion);
	        long preparacion = longValue(tiempoPreparacion);
	        
	        total = coccion + preparacion;
		}
        
		String tiempoTotal = "";
        if (format.equals("long")) {
        	tiempoTotal = String.valueOf(total);
        } else if (!format.equals("")) {
        	tiempoTotal = convertLong(total, separator);
        } else {
        	tiempoTotal = "";
        }
        
        printContent(tiempoTotal);
        return SKIP_BODY;

    }


	private String convertLong(long tiempoTotal, String separator2) {
		Timestamp tiempo = new Timestamp(tiempoTotal);
		float suma = 0;
		if (format.equals("hours"))
			suma = (float)tiempo.getTime()/60/60/1000;
		else if (format.equals("minutes"))
			suma = (float)tiempo.getTime()/60/1000;
		else if (format.equals("seconds"))
			suma = (float)tiempo.getTime()/1000;
		
		String formato="";
		
		if(unityTotalTime==null){
			CmsWorkplaceSettings settings =
					(CmsWorkplaceSettings) pageContext.getSession().getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
			Locale locale = null;	
		
			if (settings!=null && settings.getUserSettings() != null)
	    		 locale = settings.getUserSettings().getLocale();
			else 
				locale = pageContext.getRequest().getLocale();
			
			try {
				formato = new String(Messages.get().getBundle(locale).key("GUI_" + format.toUpperCase()).getBytes("iso-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else
			formato = unityTotalTime;
		
		if (suma %1 == 0)
			return ((int)suma) + separator + formato;
		return String.format("%.2f", suma) + separator + formato;
		
      }


	public long longValue(String content) {
		String[] splitValue = content.split("-");
    	Integer quantity =0;
    	String unity = "";
    	if (splitValue.length == 2) {
    		try {
    			quantity= Integer.valueOf(splitValue[0]);
    		} catch (NumberFormatException ex) {
    			// toma el valor en 0 si no puede parsear los tiempos
    		}
    		unity = splitValue[1];
    	}
		return TfsTagsUtil.getTransformationTime(unity, quantity);
 	}
	
	private String unityValue(String content) {
		String[] splitValue = content.split("-");
		
    	String unity = "";
    	
    	if (splitValue.length == 2) {
    		unity = splitValue[1];
    	}
		return unity;
 	}
	
	private String unityTotalTime(String tiempoCoccionUnity, String tiempoPreparacionUnity, String formatTag) {
		
		String unity = null;
		String unityLang = null;
		
		if (tiempoCoccionUnity!=null && !tiempoCoccionUnity.equals("") && tiempoCoccionUnity.toLowerCase().equals(formatTag)){
				unity = tiempoCoccionUnity;
		}else if (tiempoPreparacionUnity!=null && !tiempoPreparacionUnity.equals("") && tiempoPreparacionUnity.toLowerCase().equals(formatTag)){
				unity = tiempoPreparacionUnity;
		}else if (tiempoCoccionUnity!=null && !tiempoCoccionUnity.equals("") && tiempoCoccionUnity.toLowerCase().equals("segundos") || tiempoCoccionUnity.toLowerCase().equals("minutos") || tiempoCoccionUnity.toLowerCase().equals("horas") ){
			unityLang = "ES";
		}else if (tiempoCoccionUnity!=null && !tiempoCoccionUnity.equals("") && tiempoCoccionUnity.toLowerCase().equals("seconds") || tiempoCoccionUnity.toLowerCase().equals("minutes") || tiempoCoccionUnity.toLowerCase().equals("hours") ){
			unityLang = "EN";
		}else if (tiempoPreparacionUnity!=null && !tiempoPreparacionUnity.equals("") && tiempoPreparacionUnity.toLowerCase().equals("segundos") || tiempoPreparacionUnity.toLowerCase().equals("minutos") || tiempoPreparacionUnity.toLowerCase().equals("horas") ){
			unityLang = "ES";
		}else if (tiempoPreparacionUnity!=null && !tiempoPreparacionUnity.equals("") && tiempoPreparacionUnity.toLowerCase().equals("seconds") || tiempoPreparacionUnity.toLowerCase().equals("minutes") || tiempoPreparacionUnity.toLowerCase().equals("hours") ){
			unityLang = "EN";
		}
		
		if(unity==null && unityLang!=null){
			Locale locale = null;	
			locale = CmsLocaleManager.getLocale(unityLang);
		
			try {
				unity = new String(Messages.get().getBundle(locale).key("GUI_" + format.toUpperCase()).getBytes("iso-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return unity;
 	}
	
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}

