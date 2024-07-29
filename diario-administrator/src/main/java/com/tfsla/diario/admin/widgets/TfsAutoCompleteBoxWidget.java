package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.CmsXmlBooleanValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsAutoCompleteBoxWidget extends A_TfsWidget implements I_TfsWidget  {

    public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
        Map<String, String> configurations = new HashMap<String, String>();
        String[] valores=getConfiguration().split("\\|");
        for (String item:valores){
        	configurations.put(item.substring(0,item.indexOf(":")), item.substring(item.indexOf(":")+1,item.length()));

        }

        String id = param.getId();

        int rows = 2;
        String baseTerminos="";
        String autocomplete="true";
        String allowNewValues="true";
        String allowSpaces="true";
        try {
            rows = new Integer( configurations.get("rows"));
            baseTerminos=configurations.get("termBase").trim().toLowerCase();
            autocomplete=configurations.get("autocomplete").trim().toLowerCase();
            allowNewValues=configurations.get("allowNewValues").trim().toLowerCase();
            allowSpaces=configurations.get("allowSpaces").trim().toLowerCase();

        } catch (Exception e) {
            // ignore
        }

        StringBuffer result = new StringBuffer(16);
        if (autocomplete.equals("true")){
	       result.append("<script type=\"text/javascript\">\n");
	       result.append("$(function(){\n");
	       result.append("	var locationUrl = window.top.location.href;\n");
	       result.append("	locationUrl = locationUrl.substring(0,locationUrl.indexOf(\"/system\"));\n");
	       result.append(" $('#allowSpacesTags"+widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.")+"').tagit({\n");
	       
	       result.append("singleField: true,\n");
	       result.append("singleFieldDelimiter : \", \",\n");
	       result.append("singleFieldNode: $('#"+widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.")+"'),\n");
	       if(allowSpaces.equals("true")){
	    	   result.append("allowSpaces: true ,\n");
	       }else{
	    	   result.append("allowSpaces: false ,\n");   
	       }
	       result.append("autocomplete: { source: function( request, response ) {\n");
	       result.append("$.ajax({\n");
	       result.append("url: locationUrl + \"/system/modules/com.tfsla.opencmsdev/templates/terminos.jsp\",\n");
	       result.append("dataType: \"jsonp\",\n");
	       result.append("jsonpCallback:'jsonp_callback',\n");
	       result.append(" data: {\n");
	       result.append("typeTerms:\""+baseTerminos+"\",\n");
	       result.append("featureClass: \"P\",\n");
	       result.append("style: \"full\",\n");
	       result.append("maxRows: 12,\n");
	       result.append("name_startsWith: request.term\n");
	       result.append(" },\n");
	       result.append(" success: function( json ) {\n");
	       //result.append("console.log(json) ;\n");
	       result.append("response( $.map( json, function( item ) {\n");
	       result.append("return{ value: item.name,\n");
	       result.append(" label: item.label}\n");
		      
	       result.append("}));\n");
	       result.append("},\n");
	       result.append(" error: function (xhr, textStatus, errorThrown){\n");
	       result.append(" console.log(\"error: \" + errorThrown);\n");
	       result.append("}\n");
	       result.append("});\n");
	       result.append("},\n");
	       result.append(" minLength: 2 \n");
	       result.append("}\n");
	       result.append("});\n");
	       result.append("});\n");
	        result.append("</script>\n");
	       
	        result.append("<ul id=\"allowSpacesTags"+widgetDialog.getIdElement(id)+"\"></ul>");
	        result.append("<textarea id=\""+widgetDialog.getIdElement(id)+"\" content-definition=\"" + param.getName() + "\" content-type=\"" + getTypeName(param) + "\" class=\"item-value\" name=\""+widgetDialog.getIdElement(id)+"\" rows=\""+rows+"\" style=\"visibility:hidden;display:none;\">"+getWidgetStringValue(cms,widgetDialog,param)+" </textarea>");
    }else{
        	result.append("<textarea id=\""+widgetDialog.getIdElement(id)+"\" content-definition=\"" + param.getName() + "\" content-type=\"" + getTypeName(param) + "\" class=\"item-value\" name=\""+widgetDialog.getIdElement(id)+"\"   rows=\""+rows+"\">"+getWidgetStringValue(cms,widgetDialog,param)+" </textarea>");	
        }
       

       

        return result.toString();
    }
    public String getWidgetStringValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String result = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) ) {
        	return result;
        } else {
        	return "";
        }
        
    }
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(com.tfsla.widgets.TfsAutoCompleteBoxWidget.class.getName());
		return widgets;
	}

} 
