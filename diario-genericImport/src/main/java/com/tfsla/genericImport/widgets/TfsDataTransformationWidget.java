package com.tfsla.genericImport.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.admin.widgets.A_TfsWidget;
import com.tfsla.diario.admin.widgets.I_TfsWidget;
import com.tfsla.genericImport.transformation.I_dataTransformation;
import com.tfsla.genericImport.transformation.TransformationManager;
import com.tfsla.genericImport.widgets.opencms.DataTransformationWidget;

public class TfsDataTransformationWidget  extends A_TfsWidget implements I_TfsWidget {
	
	Map<String, String> configurations = new HashMap<String, String>();

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
		
        String id = param.getId();

		parseConfiguration(cms,widgetDialog,param);

		
        String selectedValue = param.getStringValue(cms);
        if (CmsStringUtil.isEmpty(selectedValue))             
        	selectedValue = "";
	     
		StringBuffer result = new StringBuffer(16);
		
		result.append("<div class=\"input-append\">\n");

     	result.append("<input type=\"hidden\" class=\" item-value\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" value=\"");
        result.append(CmsEncoder.escapeXml(selectedValue));
        result.append("\"");
        result.append(">\n");
        
        result.append("<span class=\"input-xlarge item-value focused uneditable-input\"");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_label\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_label\"");
        result.append(">\n");
        
        TransformationManager tManager = new TransformationManager();
        I_dataTransformation transformation = tManager.getTransformation(selectedValue);
        if (transformation!=null)
        	result.append(transformation.getTransformationDescription(selectedValue) );
        result.append("</span>");
        
        result.append("<a data-target=\"#TransformationModal\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success btn-transformation\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Seleccionar\"");
        result.append("><i class=\"material-icons\">search</i></a>");
        result.append("</div>");
	   	
//        if (param.hasError()) {
//            result.append(" xmlInputError");O
//        }

        return result.toString();
	}

	   protected void parseConfiguration(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param){
	    	configurations = new HashMap<String, String>();
	    	String configuration = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog.getMessages());
	         if (configuration == null) {
	             configuration = param.getDefault(cms);
	         }
	  	   
	         if (configuration==null)
	        	 return;
	         
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
		widgets.add(DataTransformationWidget.class.getName());
		return widgets;
	}
	
}