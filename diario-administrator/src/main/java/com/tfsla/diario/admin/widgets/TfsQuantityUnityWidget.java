package com.tfsla.diario.admin.widgets;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.auditActions.Messages;


public class TfsQuantityUnityWidget extends A_TfsWidget implements I_TfsWidget {
	
		/** The log object for this class. */
	    private static final Log LOG = CmsLog.getLog(TfsQuantityUnityWidget.class);
	    
		/** Configuration parameter to set the 'cmsmediosprop' parameter. 
	    modulename,paramgroup,param or modulename,param */
		public static final String CONFIGURATION_CMSMEDIOS = "cmsmediosprop";
		public static final String CONFIGURATION_QUANTITY_PATTERN = "pattern";
		public static final String CONFIGURATION_QUANTITY_TYPE = "type";
		
	    private List<CmsSelectWidgetOption> m_selectOptions;
	    private Locale m_locale;
	    
		public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

	        String id = param.getId();

	        StringBuffer result = new StringBuffer(16);

	        List<CmsSelectWidgetOption> options=null;
	        String pattern = "";
	        String type="text";
	        
		    if (areOptionsInCmsMedios()) {
		        options = parseCmsMediosOptions(cms, widgetDialog, param);
		        pattern = getRefereneValue (cms, widgetDialog, param, CONFIGURATION_QUANTITY_PATTERN );
		        type =getRefereneValue (cms, widgetDialog, param,CONFIGURATION_QUANTITY_TYPE);
		    } else
	        	options = parseSelectOptions(cms, widgetDialog, param);
	        
		    
	        String inputWidth = "";
	        if (!widgetDialog.getConteinerStyleClass().equals("default"))
	        	inputWidth = "style=\"width:27%\" ";
	       
	        result.append("<input class=\"input-small \" ");
	        
	        result.append("id=\"q_");
	        result.append(widgetDialog.getIdElement(id));
	        result.append("\"");
	        result.append(" content-definition=\"" + param.getName() + "\"");
	        result.append(" pattern=\"" + pattern + "\"");
		    result.append(" content-type=\"" + getTypeName(param) + "\" ");
		    result.append(" type=\""+type+"\"");
		    result.append (" onChange='updateQuantityUnity(this)'");
		       
	        result.append(" name=\"q_");
	        result.append(widgetDialog.getIdElement(id) +"\""  );
	        result.append(" value=\""+ getWidgetAmountValue(cms, widgetDialog, param, type)+ "\"");        
	        
	        result.append("\">\n");
	        
	        result.append("<select class=\"chzn-select \" ");
	        
	        result.append(inputWidth);
	        result.append("id=\"chz_");
	        result.append(widgetDialog.getIdElement(id));
	        result.append("\"");
	        result.append (" onChange='updateQuantityUnity(this)'");
			 
	        result.append(" content-definition=\"" + param.getName() + "\"");
	        result.append(" content-type=\"" + getTypeName(param) + "\" ");
	        
	        result.append(" name=\"chz_");
	        result.append(widgetDialog.getIdElement(id));
	        result.append("\">\n");
	        
	        String selected = getSelectedValue(cms,param);
	        
	        for (CmsSelectWidgetOption option : options) {
		        result.append("<option ");
		        if (selected.equals(option.getValue()))
		        	result.append("selected ");
		        
		        result.append("value=\"");
		        result.append(option.getValue());
		        result.append("\">");
		        
		        String optionOp = option.getOption();
		        
		        try {
		        	if (Messages.get().getBundle(widgetDialog.getUserLocale())!=null 
		        			&& !Messages.get().getBundle(widgetDialog.getUserLocale()).key("GUI_" + option.getValue().toUpperCase()).contains("?"))
		    			optionOp =  new String(Messages.get().getBundle(widgetDialog.getUserLocale()).key("GUI_" + option.getValue().toUpperCase()).getBytes("iso-8859-1"), "UTF-8");
		        	else
		        		optionOp = new String(option.getOption().getBytes("iso-8859-1"), "UTF-8");
		        } catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		        result.append(optionOp);
		        result.append("</option>\n");
	        }
	          
	        result.append(" </select>\n");
	        
	        result.append("<input type=\"hidden\" class=\"item-value\" ");
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
	        result.append(">\n");
	        
	        
	        result.append ("<script> \n");
	        result.append ("function updateQuantityUnity (element){ \n");
	        result.append ("	var id = $(element).attr(\"id\").replace('q_','').replace('chz_','');\n");
	        //result.append ("	alert(id);\n");
		    result.append (" $('input[id^=\"'+id+'\"]').val($('input[id^=\"q_'+id+'\"]').val() + '-' + $('select[id^=\"chz_'+id+'\"]').val());\n");
		    result.append (" calculateTotalCookingTime();");
	        result.append (" } ");
	        result.append ("</script>");

	        return result.toString();
	    }
		
	    

		private String getRefereneValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param, String refValue) {
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



		protected String getSelectedValue(CmsObject cms, I_CmsWidgetParameter param) {

	        String paramValue = param.getStringValue(cms);
	        if (CmsStringUtil.isEmpty(paramValue)) {
	            CmsSelectWidgetOption option = CmsSelectWidgetOption.getDefaultOption(m_selectOptions);
	            if (option != null) {
	                paramValue = option.getValue();
	            }
	        }
	        return paramValue.split("-").length==2 ? paramValue.split("-")[1]:"";
	    }
	    	
	    protected List<CmsSelectWidgetOption> parseSelectOptions(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

	        String configuration = getConfiguration();
	        if (configuration == null) {
	            configuration = param.getDefault(cms);
	        }
	        
	        configuration = CmsMacroResolver.resolveMacros(configuration, cms, widgetDialog.getTfsMessages());
	        m_selectOptions = CmsSelectWidgetOption.parseOptions(configuration);
	        if (m_selectOptions == Collections.EMPTY_LIST) {
	            m_selectOptions = new ArrayList<CmsSelectWidgetOption>();
	        }
	        return m_selectOptions;
	    }

		@Override
		public List<String> getOpenCmsWidgetsClassName() {
			List<String> widgets = new ArrayList<String>();
			widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsQuantityUnityWidget.class.getName());
			return widgets;
		}
		
		protected boolean areOptionsInCmsMedios() {
			String configuration = getConfiguration();
			int cmsmediosIndex = configuration.indexOf(CONFIGURATION_CMSMEDIOS + "=");
	        return (cmsmediosIndex != -1);
		}
		
		
		protected List<CmsSelectWidgetOption> parseCmsMediosOptions(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
			
			List<CmsSelectWidgetOption> m_selectOptions = new ArrayList<CmsSelectWidgetOption>();
			
			String configuration = getConfiguration();
			String m_cmsmedios = "";
			
			int cmsmediosIndex = configuration.indexOf(CONFIGURATION_CMSMEDIOS + "=");
	        if (cmsmediosIndex != -1) {
	            // cmsmedios.xml path is given
	            String cmsmedios = configuration.substring(cmsmediosIndex + CONFIGURATION_CMSMEDIOS.length() + 1);
	            if (cmsmedios.indexOf('|') != -1) {
	                // cut eventual following configuration values
	            	cmsmedios = cmsmedios.substring(0, cmsmedios.indexOf('|'));
	            }
	            m_cmsmedios = cmsmedios;
	            
	            LOG.debug("TfsQuantityUnityWidget -> configuracion en cmsmedios.xml: " + m_cmsmedios );
	            try {
					return getListFromCmsMedios(cms, widgetDialog, param, m_cmsmedios);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        
	        return m_selectOptions;
		}

		
	    private List<CmsSelectWidgetOption> getListFromCmsMedios(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param, String m_cmsmedios) throws Exception {

	    	List<CmsSelectWidgetOption> m_selectOptions = new ArrayList<CmsSelectWidgetOption>();
	    	
	    	CmsResource resource = getResource(cms, param);
	    	 
	    	String path = cms.getSitePath(resource);
	    	TipoEdicionService tEService = new TipoEdicionService();
			TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cms, path);

			String publication = "" + tEdicion.getId();
			
	    	String site = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

	    	String cmsMediosWithVars = replaceVariables(m_cmsmedios, resource, cms);
	    	
	    	String parts[] = cmsMediosWithVars.split(",");
	    	
		    String modulename = parts[0];
		    String paramGroup = null;
		    String paramMedios = null;
		    
	    	if (parts.length>2) {
	    		paramGroup = parts[1];
	    		paramMedios = parts[2];
	    	}
	    	else
	    		paramMedios = parts[1];

	        LOG.debug("TfsQuantityUnityWidget -> ubicado en " + site + "/" + publication);
	        LOG.debug("TfsQuantityUnityWidget -> buscando en modulo '" + modulename + "' - parametro '" + paramMedios + "' " + (paramGroup != null ? "(grupo:" + paramGroup + ")" : ""));
	    	List<String> values=null;
	    	if (parts.length>2) {
	    		values=CmsMedios.getInstance().getCmsParaMediosConfiguration().getListItempGroupParam(site, publication, modulename, paramGroup, paramMedios);
	    	}
	    	else {
	    		values=CmsMedios.getInstance().getCmsParaMediosConfiguration().getParamList(site, publication, modulename, paramMedios);
	    	}
	    	
	    	String option = null;
	        String help = null;
	        boolean isDefault = false;
	    	for (String value : values) {
	            LOG.debug("TfsQuantityUnityWidget -> valor encontrado: " + value);
	    		m_selectOptions.add(new CmsSelectWidgetOption(value, isDefault, option, help));
	    	}
	    	
	    	return m_selectOptions;
		}

	    private String replaceVariables(String cmsmedios, CmsResource resource, CmsObject cmsObject) {
			Pattern pattern = Pattern.compile("\\{prop\\..*\\}");
			Matcher matcher = pattern.matcher(cmsmedios);
			List<String> presentVariables = new ArrayList<String>();
			while (matcher.find())
				if (!presentVariables.contains(matcher.group()))
					presentVariables.add(matcher.group());
			for (String var : presentVariables)
			{
				String propName = var.replace("{prop.", "").replace("}", "");
				String value=null;
				try {
					value = cmsObject.readPropertyObject(resource, propName, false).getValue();
				} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (value==null)
					value = "";
				
				cmsmedios = cmsmedios.replaceAll(Pattern.quote(var), value);
			}
			
			return cmsmedios;
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
		        return getWidgetAmountValue(cms,widgetDialog,param,"number");
		 }
	    
		 public String getWidgetAmountValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param, String type) {

		        String result = param.getStringValue(cms);
		        String[] values = result.split("-");
		        
		        String amountValue = "";
		        
		        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(values[0])) {
		            	Scanner scanner = new Scanner(values[0]);
		            	
		            	if(type.equals("number")){
			            	if (scanner.hasNextInt())
			            		amountValue = "" + scanner.nextInt();
		            	}else{
		            		amountValue = "" + scanner.next();
		            	}
		            	
		            	scanner.close();
		        }
		        return amountValue;
		 }
	}

