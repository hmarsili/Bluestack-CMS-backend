package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsPatternedFieldWidget extends A_TfsWidget implements I_TfsWidget {
	
	/** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(TfsQuantityUnityWidget.class);
    
	/** Configuration parameter to set the 'cmsmediosprop' parameter. 
    modulename,paramgroup,param or modulename,param */
	public static final String CONFIGURATION_CMSMEDIOS = "cmsmediosprop";
	public static final String CONFIGURATION_PATTERN = "pattern";
	public static final String CONFIGURATION_TYPE = "type";
	public static final String CONFIGURATION_MIN = "min";
	public static final String CONFIGURATION_MAX = "max";

	
    private List<CmsSelectWidgetOption> m_selectOptions;
    private Locale m_locale;
    
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        List<CmsSelectWidgetOption> options=null;
        String pattern = "";
        String type="text";
        
	    pattern = getReferenceValue (cms, widgetDialog, param, CONFIGURATION_PATTERN );
	    type =getReferenceValue (cms, widgetDialog, param,CONFIGURATION_TYPE);
	    String min=getReferenceValue (cms, widgetDialog, param,CONFIGURATION_MIN);
	    String max=getReferenceValue (cms, widgetDialog, param,CONFIGURATION_MAX);
	    
	    String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:27%\" ";
       
        result.append("<input class=\"input-small item-value \" ");
        
        result.append("id=\"q_");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" pattern=\"" + pattern + "\"");
	    result.append(" content-type=\"" + getTypeName(param) + "\" ");
	    result.append(" type=\""+type+"\"");
	    if  (!min.equals(""))  
	    	 result.append(" min=\""+min+"\"");
	    if  (!max.equals(""))  
	    	 result.append(" max=\""+max+"\"");
	  
        result.append(" name=\"q_");
        result.append(widgetDialog.getIdElement(id) +"\""  );
        result.append(" value=\""+ getWidgetAmountValue(cms, widgetDialog, param)+ "\"");        
        
        result.append("\">\n");
        
      
        return result.toString();
    }
	
    

	private String getReferenceValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param, String refValue) {
		String configuration = getConfiguration();
		String pattern = "";
		int cmsmediosIndex = configuration.indexOf(refValue + ",");
        if (cmsmediosIndex!= -1 ) {
        	String[] patternString = configuration.split(refValue + ",");
        	if (patternString.length > 1) {
        		pattern = patternString[1];
        		pattern = pattern.substring(0, pattern.indexOf('|')!=-1?pattern.indexOf('|'):pattern.length());
        	}
        }
        return pattern;
	}

	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		List<String> widgets = new ArrayList<String>();
		widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsPatternedFieldWidget.class.getName());
		return widgets;
	}
   
    protected String getPropValue(CmsObject cms, String path, String propertyName) {
    	String value = "/";
        try {
            value = cms.readPropertyObject(path, propertyName, true).getValue("/");
        } catch (CmsException ex) {
            // should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
        return value;
    }
    
	protected CmsResource getResource(CmsObject cms, I_CmsWidgetParameter param) {

        I_CmsXmlContentValue value = (I_CmsXmlContentValue)param;
        CmsFile file = value.getDocument().getFile();
        String resourceName = cms.getSitePath(file);
        if (CmsWorkplace.isTemporaryFile(file)) {
            StringBuffer result = new StringBuffer(resourceName.length() + 2);
            result.append(CmsResource.getFolderPath(resourceName));
            result.append(CmsResource.getName(resourceName).substring(1));
            resourceName = result.toString();
        }
        try {
            List listsib = cms.readSiblings(resourceName, CmsResourceFilter.ALL);
            for (int i = 0; i < listsib.size(); i++) {
                CmsResource resource = (CmsResource)listsib.get(i);
                // get the default locale of the resource
                Locale locale = getDefaultLocale(cms, cms.getSitePath(resource));
                if (locale.equals(value.getLocale())) {
                    // get the property for the right locale
                    return resource;
                }
            }
        } catch (CmsException ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
        return file;
    }
	
	 protected Locale getDefaultLocale(CmsObject cms, String resource) {

	        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, resource);
	        if (locale == null) {
	            List locales = OpenCms.getLocaleManager().getAvailableLocales();
	            if (locales.size() > 0) {
	                this.m_locale = (Locale)locales.get(0);
	            } else {
	            	this.m_locale = Locale.ENGLISH;
	            }
	        }
	        return locale;
	    }
	 
	 public String getWidgetValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
	        String result = param.getStringValue(cms);
	        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result))
	        	return result;
	        return "";
	    
	    }

    
	 public String getWidgetAmountValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

	        String result = param.getStringValue(cms);
	        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
	            	Scanner scanner = new Scanner(result);
	            	if (scanner.hasNextInt())
	            		return "" + scanner.nextInt();
	            	else
	            		return "";
	            		
	        }
	        return "";
	    }
}

