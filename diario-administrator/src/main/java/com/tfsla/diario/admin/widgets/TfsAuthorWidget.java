package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsAuthorWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String,String> configParams = null;
	

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseParams();
		
        String id = param.getId();

        
        String defaultMode = widgetDialog.getEditorValues().get("authorMode");
        
        String anonymousUser = widgetDialog.getEditorValues().get("anonymousUser");
        String currentUser = widgetDialog.getEditorValues().get("currentUser");
        String freeText = widgetDialog.getEditorValues().get("defaultFreeText");
        
        String selectedUser = param.getStringValue(cms);
        /*if (selectedUser==null) {
        	if (defaultMode!=null && defaultMode.equals("signByUser"))
        		selectedUser = currentUser;
        	else
        		selectedUser = anonymousUser;
        }*/
        
        StringBuffer result = new StringBuffer(16);

        boolean hasValue = selectedUser!=null && !selectedUser.equals("");
        
		String userDescription = "";
		if (hasValue)
			userDescription = getActualDescription(cms, selectedUser);
 			
    	result.append("<label id=\"lblPreview_" + widgetDialog.getIdElement(id) + "\"" + (hasValue ? "" : " style=\"display: none;\"") + " >" + userDescription + "</label>\n");
    	
    	
        
        String modalName = "userModal";
        String className = "input-user";
        String buttonClassName = "btn-multiselectUsers";
        
        
     	result.append("<div class=\"input-append\">\n");
     	
     	result.append("<input class=\"input-xlarge focused item-value " + className + " input-author \" type=\"text\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" value=\"");
        result.append(CmsEncoder.escapeXml(selectedUser));
        result.append("\"");
        result.append(">\n");

        
        // data-toggle=\"modal\" 
        result.append("<a data-target=\"#" + modalName + "\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success "+ buttonClassName + "\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Seleccionar\"");
        result.append("><i class=\"material-icons\">search</i></a>");

        if (currentUser!=null) {
	        result.append("<a onclick=\"signBy('" + widgetDialog.getIdElement(id) + "','" + StringEscapeUtils.escapeJavaScript(currentUser) + "','" + StringEscapeUtils.escapeJavaScript(getActualDescription(cms, currentUser)) + "')\" class=\"btn btn-success \" rel=\"tooltip\" data-original-title=\"Firmar\"");
	        result.append("><i class=\"material-icons \">check</i></a>");
        }
        
        if (anonymousUser!=null) {
	        result.append("<a onclick=\"signBy('" + widgetDialog.getIdElement(id) + "','" + StringEscapeUtils.escapeJavaScript(anonymousUser) + "','" + StringEscapeUtils.escapeJavaScript(getActualDescription(cms, anonymousUser)) + "')\" class=\"btn btn-success \" rel=\"tooltip\" data-original-title=\"Usuario anonimo\"");
	        result.append("><i class=\"material-icons \">help_outline</i></a>");
		}
        if (freeText!=null) {
	        result.append("<a onclick=\"freeSignBy('" + widgetDialog.getIdElement(id) + "','" + StringEscapeUtils.escapeJavaScript(freeText) + "')\" class=\"btn btn-success \" rel=\"tooltip\" data-original-title=\"Autor manual\"");
	        result.append("><i class=\"material-icons\">mode_edit</i></a>");        	
        }
        
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
		        		return cms.readUser(actualUserName).getFullName();	        		
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
			widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsAuthorWidget.class.getName());
			return widgets;
		}

}
