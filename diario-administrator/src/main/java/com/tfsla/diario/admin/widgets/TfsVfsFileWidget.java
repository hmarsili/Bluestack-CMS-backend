package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsMacroResolver;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsVfsFileWidget extends A_TfsWidget implements I_TfsWidget {
	
	Map<String, String> configurations = new HashMap<String, String>();

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
		
        String id = param.getId();

		parseConfiguration(cms,widgetDialog,param);

		StringBuffer result = new StringBuffer(16);
		
        String hideOnEmpty =configurations.get("hideOnEmpty");
        
        String inputClass="";
        if (hideOnEmpty!=null && (hideOnEmpty.toLowerCase().trim().equals("true") || hideOnEmpty.toLowerCase().trim().equals("yes")) && param.getStringValue(cms).length()==0)
        	inputClass = " input-hideOnEmpty";

		result.append("<div class=\"input-append\">\n");
     	
     	result.append("<input class=\"input-xlarge item-value focused input-vfsFile " + inputClass + " \" type=\"text\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        
        if(configurations.get("excludefiles") != null){
        	result.append(" showfiles=\"false\"");
        }
        if(configurations.get("includefiles") != null){
        	result.append(" showfiles=\"true\"");
        	
        }
        if(configurations.get("includefolders") != null){
        	result.append(" showfolders=\"true\"");
        	
        }
        if(configurations.get("excludefolders") != null){
        	result.append(" showfolders=\"false\"");
        	
        }
         
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\"");
        result.append(">\n");
        
        result.append("<a data-target=\"#vfsFileModal\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success btn-multiselectVfsFile\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Seleccionar\"");
        result.append("><i class=\"material-icons\">search</i></a>");
        result.append("</div>");
	   	
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
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
	      	   else
	      		   configurations.put(items[0], "true");
	         }
	     }   
	 
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsVfsFileWidget.class.getName());
		widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsVfsFileWidget.class.getName());
		return widgets;
	}
	
}
