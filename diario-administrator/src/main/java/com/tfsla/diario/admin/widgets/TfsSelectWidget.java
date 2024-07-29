package com.tfsla.diario.admin.widgets;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.auditActions.Messages;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.securityService.TfsUserAuditPermission;
import com.tfsla.widgets.PropertySelectWidget;

public class TfsSelectWidget extends A_TfsWidget implements I_TfsWidget {

	/** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(TfsSelectWidget.class);
    
	/** Configuration parameter to set the 'cmsmediosprop' parameter. 
    modulename,paramgroup,param or modulename,param */
	public static final String CONFIGURATION_CMSMEDIOS = "cmsmediosprop";
  
    private List<CmsSelectWidgetOption> m_selectOptions;

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        List<CmsSelectWidgetOption> options=null;
        
	    if (areOptionsInCmsMedios())
	        options = parseCmsMediosOptions(cms, widgetDialog, param);
        else
        	options = parseSelectOptions(cms, widgetDialog, param);
        
        String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:98%\" ";
        if(isPublicacionSelect(widgetDialog.getIdElement(id))){
        	
        	String[] typeNames = id.split("_");
            String index = typeNames[1];
            int indexVal = param.getIndex() + 1;
            String siteName = widgetDialog.getXmlContent().getStringValue(cms,  "publicaciones["+index+"]/publicacion[" + indexVal + "]", cms.getRequestContext().getLocale());
            
            boolean isSectionAvailable = true;
        	if(!siteName.isEmpty()){     	
	        	TipoEdicionService tService = new TipoEdicionService();
				TipoEdicion tEdicion;
				int idE = 0;
				try {
					String proyecto = OpenCmsBaseService.getCurrentSite(cms);
					tEdicion = tService.obtenerTipoEdicion(siteName,proyecto);
					idE = tEdicion.getId();
				} catch (Exception e) {
					e.printStackTrace();
				}
	        	
	        	TfsUserAuditPermission tfsAudit = new TfsUserAuditPermission();
	        	String user = cms.getRequestContext().currentUser().getName();
	        	isSectionAvailable = tfsAudit.isPublicationAvailable(user, cms, idE);
        	}
        	if(isSectionAvailable){
        		result.append("<select class=\"chzn-select item-value\" ");
        	}else{
        		result.append("<select class=\"chzn-select chzn-publication-select item-value\" ");
        	}
        }else{
        	result.append("<select class=\"chzn-select item-value\" ");
        }
        result.append(inputWidth);
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
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
	    			optionOp =  com.tfsla.utils.StringEncoding.fixEncoding(Messages.get().getBundle(widgetDialog.getUserLocale()).key("GUI_" + option.getValue().toUpperCase()));
	     	       
	        	else
	        		optionOp = com.tfsla.utils.StringEncoding.fixEncoding(option.getOption());
	        	
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        result.append(optionOp);
	        result.append("</option>\n");
        }
        result.append("</select>\n");

        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
	
    private boolean isPublicacionSelect(String pub) {
		if(pub.contains("publicaciones")){	        
			return true;
		}
		return false;
	}

	protected String getSelectedValue(CmsObject cms, I_CmsWidgetParameter param) {

        String paramValue = param.getStringValue(cms);
        if (CmsStringUtil.isEmpty(paramValue)) {
            CmsSelectWidgetOption option = CmsSelectWidgetOption.getDefaultOption(m_selectOptions);
            if (option != null) {
                paramValue = option.getValue();
            }
        }
        return paramValue;
    }
    	
    protected List<CmsSelectWidgetOption> parseSelectOptions(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

       // if (m_selectOptions == null) {
        String configuration = getConfiguration();
        if (configuration == null) {
            // workaround: use the default value to parse the options
            configuration = param.getDefault(cms);
        }
        
        configuration = CmsMacroResolver.resolveMacros(configuration, cms, widgetDialog.getTfsMessages());
        m_selectOptions = CmsSelectWidgetOption.parseOptions(configuration);
        if (m_selectOptions == Collections.EMPTY_LIST) {
            m_selectOptions = new ArrayList<CmsSelectWidgetOption>();
        }
        //}
        return m_selectOptions;
    }

	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsComboWidget.class.getName());
		widgets.add(PropertySelectWidget.class.getName());
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
            
            LOG.debug("TfsSelectWidget -> configuracion en cmsmedios.xml: " + m_cmsmedios );
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

        LOG.debug("TfsSelectWidget -> ubicado en " + site + "/" + publication);
        LOG.debug("TfsSelectWidget -> buscando en modulo '" + modulename + "' - parametro '" + paramMedios + "' " + (paramGroup != null ? "(grupo:" + paramGroup + ")" : ""));
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
            LOG.debug("TfsSelectWidget -> valor encontrado: " + value);
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
	                locale = (Locale)locales.get(0);
	            } else {
	                locale = Locale.ENGLISH;
	            }
	        }
	        return locale;
	    }

    
}
