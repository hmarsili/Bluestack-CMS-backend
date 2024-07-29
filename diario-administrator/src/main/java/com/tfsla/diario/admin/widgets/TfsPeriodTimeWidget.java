package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsPeriodTimeWidget extends A_TfsWidget implements I_TfsWidget {

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        String inputClass = "input-mini";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputClass = widgetDialog.getConteinerStyleClass();

        result.append("<div class=\"input-append\" >\n");
        result.append("<input class=\""+ inputClass +"\" style=\"vertical-align:top;\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_amount\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_amount\"");
        result.append(" type=\"text\" value=\"");
        result.append(getWidgetAmountValue(cms, widgetDialog, param));        
        result.append("\">\n");

        //result.append("<div class=\"input-append bootstrap-timepicker-component\">\n");

        //if (inputClass.equals("span12"))
        //	inputClass = "span10";
        
        String selected = getWidgetUnitValue(cms, widgetDialog, param);
        result.append("<select class=\"chzn-select\" style=\"vertical-align:top;height:15px\"");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_unit\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_unit\"");
        result.append(">\n");
        
        result.append("<option ");
        if (selected.equals("h"))
        	result.append("selected ");
        result.append("value=\"h\">horas</option>\n");

        result.append("<option ");
        if (selected.equals("d"))
        	result.append("selected ");
        result.append("value=\"d\">dias</option>\n");

        result.append("<option ");
        if (selected.equals("M"))
        	result.append("selected ");
        result.append("value=\"M\">meses</option>\n");

        result.append("<option ");
        if (selected.equals("y"))
        	result.append("selected ");
        result.append("value=\"y\">años</option>\n");

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
        result.append(getWidgetValue(cms, widgetDialog, param));
        result.append("\"");
        result.append(">\n");
        
        
       result.append("<script type=\"text/javascript\">\n");
       result.append("\t$(document).ready(function () { \n");
       
       result.append("\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_amount\").change(function () {\n");       
       result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "\").val($(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_amount\").val() + $(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_unit\").val());\n");
       result.append("\t});\n");

       result.append("\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_unit\").change(function () {\n");
       result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "\").val($(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_amount\").val() + $(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_unit\").val());\n");
       result.append("\t});\n");

       result.append("\t});\n");

       result.append("</script>\n");
      
       result.append("</div>\n");
        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }

    public String getWidgetValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
        String result = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result))
        	return result;
        return "0d";
    
    }
    
    public String getWidgetAmountValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String result = param.getStringValue(cms);
        result = result.replaceAll("[dMyh]", "");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            	Scanner scanner = new Scanner(result);
            	if (scanner.hasNextInt())
            		return "" + scanner.nextInt();
            	else
            		return "0";
            		
        }
        return "0";
    }

    public String getWidgetUnitValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String result = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            	Scanner scanner = new Scanner(result);
            	if (scanner.hasNextInt()) {
            		scanner.nextInt();
            		if (scanner.hasNext("[dMyh]"));
            			return scanner.next("[dMyh]");
            		
            	}
        }
        return "d";
    }

	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		List<String> widgets = new ArrayList<String>();
		widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsPeriodTimeWidget.class.getName());
		return widgets;

	}
    

}
