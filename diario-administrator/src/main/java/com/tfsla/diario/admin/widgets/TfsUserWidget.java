package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.widgets.CmsUserWidget;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsUserWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String,String> configParams = null;
	

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseParams();
		
        String id = param.getId();

        String actualValue = param.getStringValue(cms);
        
        
        StringBuffer result = new StringBuffer(16);

        boolean hasValue = actualValue!=null && !actualValue.equals("");
        
		String userDescription = "";
		if (hasValue)
			userDescription = getActualDescription(cms, actualValue);
 			
    	result.append("<label id=\"lblPreview_" + widgetDialog.getIdElement(id) + "\"" + (hasValue ? "" : " style=\"display: none;\"") + " >" + userDescription + "</label>\n");
    	
    	
        
        String modalName = "userModal";
        String className = "input-user";
        String buttonClassName = "btn-multiselectUsers";
        
        
     	result.append("<div class=\"input-append\">\n");
     	
     	result.append("<input class=\"input-xlarge focused item-value " + className + "\" type=\"text\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\"");
        result.append(">\n");

        
        // data-toggle=\"modal\" 
        result.append("<a data-target=\"#" + modalName + "\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success "+ buttonClassName + "\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Seleccionar\"");
        result.append("><i class=\"material-icons\">search</i></a>");
        result.append("</div>");
	   	
        return result.toString();
    }
		   
	private void parseParams(){
		
		configParams = new HashMap<String,String>();
		
		String conf = getConfiguration();
		if (conf!=null) {
			String params[] = conf.split(",");
			for (int j=0; j< params.length; j++)
	    	{
				String param[] = params[j].split(":");
				if (param.length==2)	
					configParams.put(param[0].trim(), param[1].trim());
	    		
	    	}
		}
	}
		   
	    private String getActualDescription(CmsObject cms, String actualUserName) {
	        try {
	        	if (!actualUserName.equals("")) {
	        		if (actualUserName.trim().equals("me") || actualUserName.trim().equals("current"))
	        			return "usuario actual";
	        		else {
		        		return cms.readUser(actualUserName).getDescription();	        		
		        	}
	        	}
	        }
	        catch (CmsException e) {
	            return "El usuario seleccionado no es valido";
	        }
			return "";
	        
	        
	    }

		@Override
		public List<String> getOpenCmsWidgetsClassName() {
			List<String> widgets = new ArrayList<String>();
			widgets.add(CmsUserWidget.class.getName());
			return widgets;
		}

}
