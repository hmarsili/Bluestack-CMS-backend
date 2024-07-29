package com.tfsla.genericImport.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.admin.widgets.A_TfsWidget;
import com.tfsla.diario.admin.widgets.I_TfsWidget;
import com.tfsla.genericImport.service.ContentTypeService;
import com.tfsla.genericImport.widgets.opencms.PropertySelectorWidget;
import com.tfsla.genericImport.widgets.opencms.XmlContentTypesWidget;

public class TfsPropertySelectorWidget extends A_TfsWidget implements I_TfsWidget  {

	Map<String, String> configurations = new HashMap<String, String>();

    private List<CmsSelectWidgetOption> m_selectOptions;

	@Override
	public String getWidgetHtml(CmsObject cms,
			TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
		
		parseConfiguration(cms,widgetDialog,param);
        String id = param.getId();
        StringBuffer result = new StringBuffer(16);

        String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:98%\" ";

        String selected = getSelectedValue(cms, param);
        
        result.append("<select class=\"chzn-select item-value\" ");
        result.append(inputWidth);
        result.append(" content-definition=\"" + param.getName() + "\" ");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append("name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\" id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\">");

        ContentTypeService ctService = new ContentTypeService();
		List<CmsPropertyDefinition> properties;
		try {
			properties = ctService.getAllProperties(cms);
			for (CmsPropertyDefinition property : properties) {
	    		String strSelected = (selected!=null && selected.equals(property.getName())) ? "selected" : "";
	    		result.append("	<option value=\"" + property.getName() + "\" " + strSelected + " >" + property.getName() + "</option>\n");
			}
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        result.append("</select>");

        return result.toString();
	}

	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		List<String> widgets = new ArrayList<String>();
		widgets.add(PropertySelectorWidget.class.getName());
		return widgets;
	}
	
    protected void parseConfiguration(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param){
    	configurations = new HashMap<String, String>();
    	String configuration = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog.getMessages());
         if (configuration == null) {
             configuration = param.getDefault(cms);
         }
  	   
         String[] configurationsKeysValues = configuration.split("\\|");
         for(int i=0;i<configurationsKeysValues.length;i++){
      	   String[] items = configurationsKeysValues[i].split("=");
      	   if(items.length == 2)
      		   configurations.put(items[0], items[1]);
         }
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


}
