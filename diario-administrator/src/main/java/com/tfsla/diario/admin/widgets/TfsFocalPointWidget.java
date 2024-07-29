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
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;


public class TfsFocalPointWidget extends A_TfsWidget implements I_TfsWidget {
	
		/** The log object for this class. */
	    private static final Log LOG = CmsLog.getLog(TfsFocalPointWidget.class);
	    private Locale m_locale;
	    
		public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

	        String id = param.getId();
	        String elementName = widgetDialog.getIdElement(id);
	        StringBuffer result = new StringBuffer(16);

	     
	        String inputWidth = "";
	        if (!widgetDialog.getConteinerStyleClass().equals("default"))
	        	inputWidth = "style=\"width:27%\" ";
	       
	        result.append("<input class=\"input-small item-value\" ");
	        
	        result.append("id=\"");
	        result.append(widgetDialog.getIdElement(id));
	        result.append("\"");
	        result.append(" content-definition=\"" + param.getName() + "\"");
	        result.append(" content-type=\"" + getTypeName(param) + "\" ");
	        result.append(" type=\"text\" ");
			   
	        result.append(" name=\"");
	        result.append(widgetDialog.getIdElement(id) +"\""  );
	        result.append(" value=\""+ getWidgetValue(cms, widgetDialog, param)+ "\"");        
	        
	       
	        result.append("/>\n");
	        result.append("<div id=\""+ param.getName()+"_buttons\" class=\"control-buttons span12 "+ param.getName()+"_buttons\">");
		    result.append("<a class=\"focalPointButton\"");
	        
	        result.append(" id=\"fp_");
	        result.append(widgetDialog.getIdElement(id));
	        result.append("\"");
	        result.append (" onClick='openFocalPointModal(this)'");
			
	        result.append("rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Punto focal\"");
	        result.append(" content-definition=\"" + param.getName() + "\"");
	        result.append(" content-type=\"" + getTypeName(param) + "\" ");
	        
	        result.append(" name=\"fp_");
	        result.append(widgetDialog.getIdElement(id) + "\">");
	        result.append("<i class=\"material-icons \" >filter_center_focus");
	        result.append("</i>\n");
	        result.append("</a>\n");
	        
	        result.append("<a class=\"focalPointClearButton\"");
	        
	        result.append (" onClick='clearFocalPointModal(\""+widgetDialog.getIdElement(id)+"\")'");
			
	        result.append("rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Borrar\"");
	        result.append(" content-definition=\"" + param.getName() + "\"");
	        result.append(" content-type=\"" + getTypeName(param) + "\" ");
	        
	        result.append( "\">");
	        result.append("<i class=\"material-icons \" >delete");
	        result.append("</i>\n");
	        result.append("</a>\n");
	        result.append("</div>\n");
	        
	        /*result.append("<input type=\"hidden\" class=\"item-value\" ");
	        result.append(" content-definition=\"" + param.getName() + "\" ");
	        result.append(" content-type=\"" + getTypeName(param) + "\" ");
	        result.append("id=\"");
	        result.append(widgetDialog.getIdElement(id));
	        result.append("\"");
	        result.append(" name=\"");
	        result.append(widgetDialog.getIdElement(id));
	        result.append("\"");
	        result.append(" value=\"");
	        result.append(getWidgetValue(cms, widgetDialog, param));
	        result.append("\"");
	        result.append(">\n");*/
	        
	        
	        result.append ("<script> \n");
	        result.append ("function openFocalPointModal (element){ \n");
	        result.append ("	var id = $(element).attr(\"id\").replace('fp_','');\n");
	        result.append ("	getFocalPoint(id);\n");
	        result.append (" } ");
	        
	        result.append ("function clearFocalPointModal (element){ \n");
	        result.append ("	var id = element.replace(/\\./g,\"\\\\.\");$(\"#\"+id).val(\"\");\n");
	        result.append (" } ");
	        result.append ("</script>");

	        return result.toString();
	    }
		
	    
		    	
		@Override
		public List<String> getOpenCmsWidgetsClassName() {
			List<String> widgets = new ArrayList<String>();
			widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsFocalPointWidget.class.getName());
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
		        String[] values = result.split("-");
		        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(values[0])) {
		            	Scanner scanner = new Scanner(values[0]);
		            	if (scanner.hasNextInt())
		            		return "" + scanner.nextInt();
		            	else
		            		return "";
		            		
		        }
		        return "";
		    }
	}


