package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCategoryWidget;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsCategoryWidget extends A_TfsWidget implements I_TfsWidget {

    /** Configuration parameter to set the category to display. */
    public static final String CONFIGURATION_CATEGORY = "category";

    /** Configuration parameter to set the 'only leaf' flag parameter. */
    public static final String CONFIGURATION_ONLYLEAFS = "onlyleafs";

    /** Configuration parameter to set the 'property' parameter. */
    public static final String CONFIGURATION_PROPERTY = "property";

    /** Configuration parameter to set the 'cmsmediosprop' parameter. 
      modulename,paramgroup,param or modulename,param */
    public static final String CONFIGURATION_CMSMEDIOS = "cmsmediosprop";

    /** Configuration parameter to set the 'property' parameter. */
    public static final String CONFIGURATION_EDITORVALUE = "editorvalue";
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCategoryWidget.class);

    /** The cmsmedios param to read the starting category from. */
    private String m_cmsmedios;
    
    /** The displayed category. */
    private String m_category;

    /** The 'only leaf' flag. */
    private String m_onlyLeafs;

    /** The property to read the starting category from. */
    private String m_property;

    private String m_editorValue;

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        String referencePath = null;
        try {
            referencePath = cms.getSitePath(getResource(cms, param));
        } catch (Exception e) {
            // ignore, this can happen if a new resource is edited using direct edit
        }
        String startingCat = getStartingCategory(cms, widgetDialog, referencePath,param);
        String selectedCategory = CmsEncoder.escapeXml(param.getStringValue(cms));
        String categoryPreffix = "";
        int idx = selectedCategory.indexOf("categories");
        if (idx>-1)
        {
        	categoryPreffix = selectedCategory.substring(0,idx+11);
        }
        String category = selectedCategory.replace(categoryPreffix,"");
        
        
        StringBuffer result = new StringBuffer(16);

        result.append("<script>\n");
        result.append("$(document).ready(function(){\n");

        
        
        result.append("\tvar startPath = \"" + startingCat + "\";\n");
     	
     	result.append("\tvar selectedCategory = \"" + category + "\";\n");
     	result.append("\tif (selectedCategory.length > 0)\n");
     	result.append("\t\tselectedCategory = selectedCategory.substring(0,selectedCategory.length -1);\n");
     			
     	result.append("\tfillSubCategory(\"" + widgetDialog.getIdElement(id) + "\",startPath,true,selectedCategory);\n");
        
     	result.append("});\n");
     	result.append("</script>\n");

     	String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:98%\" ";
        
        result.append("<div class=\"pull-left\"> ");
        result.append("<select class=\"chzn-select\" ");
        result.append(inputWidth);
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id) + "__1");
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id) + "__1");
        result.append("\"");
        result.append(">\n");
        result.append("\t<option>Select a Value...</option>");
     	result.append("</select>\n");
     	
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
        result.append(selectedCategory);
        result.append("\"");
        result.append(">\n");
        
        result.append("<input type=\"hidden\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_preffix\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_preffix\"");
        result.append(" value=\"");
        result.append(categoryPreffix);
        result.append("\"");
        result.append(">\n");
        result.append("</div>");
        //result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));        
        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
	
	   @Override
	    public String getDialogIncludes(CmsObject cms, TfsXmlContentEditor widgetDialog) {
		   
		   StringBuffer stringBuffer = new StringBuffer();
	
		   String link = CmsJspTagLink.linkTagAction("/system/modules/com.tfsla.diario.admin/resources/scripts/categoryFiller.js", widgetDialog.getRequest());
		   
		   stringBuffer.append("<script type=\"text/javascript\" src=\"");
		   stringBuffer.append(link);
		   stringBuffer.append("\"></script>\n");
	        return stringBuffer.toString();
		   
	   }
	   @Override
	    public void setConfiguration(String configuration) {

	        // we have to validate later, since we do not have any cms object here
	        m_category = "";
	        m_cmsmedios = "";
	        m_editorValue ="";
	        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
	            int categoryIndex = configuration.indexOf(CONFIGURATION_CATEGORY + "=");
	            if (categoryIndex != -1) {
	                // category is given
	                String category = configuration.substring(CONFIGURATION_CATEGORY.length() + 1);
	                if (category.indexOf('|') != -1) {
	                    // cut eventual following configuration values
	                    category = category.substring(0, category.indexOf('|'));
	                }
	                m_category = category;
	            }
	            int cmsmediosIndex = configuration.indexOf(CONFIGURATION_CMSMEDIOS + "=");
	            if (cmsmediosIndex != -1) {
	                // category is given
	                String cmsmedios = configuration.substring(cmsmediosIndex + CONFIGURATION_CMSMEDIOS.length() + 1);
	                if (cmsmedios.indexOf('|') != -1) {
	                    // cut eventual following configuration values
	                	cmsmedios = cmsmedios.substring(0, cmsmedios.indexOf('|'));
	                }
	                m_cmsmedios = cmsmedios;
	            }	       
	            int onlyLeafsIndex = configuration.indexOf(CONFIGURATION_ONLYLEAFS + "=");
	            if (onlyLeafsIndex != -1) {
	                // only leafs is given
	                String onlyLeafs = configuration.substring(onlyLeafsIndex + CONFIGURATION_ONLYLEAFS.length() + 1);
	                if (onlyLeafs.indexOf('|') != -1) {
	                    // cut eventual following configuration values
	                    onlyLeafs = onlyLeafs.substring(0, onlyLeafs.indexOf('|'));
	                }
	                m_onlyLeafs = onlyLeafs;
	            }
	            
	            int propertyIndex = configuration.indexOf(CONFIGURATION_PROPERTY + "=");
	            if (propertyIndex != -1) {
	                // property is given
	                String property = configuration.substring(propertyIndex + CONFIGURATION_PROPERTY.length() + 1);
	                if (property.indexOf('|') != -1) {
	                    // cut eventual following configuration values
	                    property = property.substring(0, property.indexOf('|'));
	                }
	                m_property = property;
	            }
	            
	            if (m_category.contains(CONFIGURATION_EDITORVALUE + ":"))
	            {
	            	m_editorValue = m_category.replace(CONFIGURATION_EDITORVALUE + ":", "");
	            	m_category = "";
	            }

	        }
	        super.setConfiguration(configuration);
	    }
	   protected String getStartingCategory(CmsObject cms, TfsXmlContentEditor widgetDialog, String referencePath, I_CmsWidgetParameter param) {

		   
	        String ret = "";
	        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_editorValue)) {
	        	ret = widgetDialog.getEditorValues().get(m_editorValue);
	        }
	        else if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_category) && CmsStringUtil.isEmptyOrWhitespaceOnly(m_property) && CmsStringUtil.isEmptyOrWhitespaceOnly(m_cmsmedios)) {
	            ret = "/";
	        } else if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_category)) {
	            ret = m_category;
	        } else if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_cmsmedios)) {
	            try {
					ret = getCategoryPathFromCmsMedios(cms, widgetDialog, param);
				} catch (Exception e) {
					ret = "/";
					e.printStackTrace();
				}
	        } else {
	            // use the given property from the right file
	            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(referencePath)) {
	                try {
	                    ret = cms.readPropertyObject(referencePath, m_property, true).getValue("/");
	                } catch (CmsException ex) {
	                    // should never happen
	                    if (LOG.isErrorEnabled()) {
	                        LOG.error(ex.getLocalizedMessage(), ex);
	                    }
	                }
	            }
	        }
	        if (!ret.endsWith("/")) {
	            ret += "/";
	        }
	        if (ret.startsWith("/")) {
	            ret = ret.substring(1);
	        }
	        
	        LOG.debug("Categoria inicial: " + ret);
	        return ret;
	    }
	   
	    private String getCategoryPathFromCmsMedios(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) throws Exception {

	    	CmsResource resource = getResource(cms, param);
	    	 
	    	String path = cms.getSitePath(resource);
	    	TipoEdicionService tEService = new TipoEdicionService();
			TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cms, path);

			String publication = "" + tEdicion.getId();
			
	    	String site = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

	    	String cmsMediosWithVars = replaceVariables(m_cmsmedios, resource, cms);
	    	
	    	String valueToReturn = "";
	    	String posiblePath[] = cmsMediosWithVars.split(";");
	    	for (String configPath : posiblePath) {
				String parts[] = configPath.split(",");
	    	
	    		String modulename = parts[0];
	    		String paramGroup = null;
	    		String paramMedios = null;
		    
		    	if (parts.length>2) {
		    		paramGroup = parts[1];
		    		paramMedios = parts[2];
		    	}
		    	else
		    		paramMedios = parts[1];
	    	
		    	String value = "";
		    	if (parts.length>2) {
		    		value = CmsMedios.getInstance().getCmsParaMediosConfiguration().getItemGroupParam(site, publication, modulename, paramGroup, paramMedios);
		    	}
		    	else {
		    		value += CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(site, publication, modulename, paramMedios);
		    	}
		    	if (valueToReturn.equals(""))
		    		valueToReturn = value;
		    	else
		    		valueToReturn += "," + value;
	    	}
	    	return valueToReturn;
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
	    
		@Override
		public List<String> getOpenCmsWidgetsClassName() {
			// TODO Auto-generated method stub
			List<String> widgets = new ArrayList<String>();
			widgets.add(CmsCategoryWidget.class.getName());
			return widgets;
		}
	    

}
