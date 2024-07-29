package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsTriviaClassificationWidget extends A_TfsWidget implements I_TfsWidget {

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:98%\" ";
        
        result.append("<div class=\"pull-left\"> ");
        result.append("<select class=\"chzn-select item-value\" ");
        result.append(" content-definition=\"" + param.getName() + "\" ");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
     
        result.append(inputWidth);
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id) + "__1");
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id) + "__1");
        result.append("\"");
        result.append(" value=\"");
        result.append(getWidgetAmountValue(cms, widgetDialog, param));
        result.append("\"");
        
        result.append(">\n");
        result.append("\t<option>Select a Value...</option>");
     	result.append("</select>\n");
     	
     	/*result.append("<script>\n");
     	result.append("$(document).ready(function(){\n");
     	result.append("reloadClassification('"+getWidgetAmountValue(cms, widgetDialog, param)+"','"+widgetDialog.getIdElement(id) +"');\n"); 
     	result.append("});\n");
     	result.append("</script>\n");
       */
        result.append("</div>\n");
        

        return result.toString();
    }
	
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		List<String> widgets = new ArrayList<String>();
		widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsTriviaClassificationWidget.class.getName());
		return widgets;

	}
	
	 public String getWidgetAmountValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

	        String result = param.getStringValue(cms);
	        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
	            	return  "" + result;
	        }
	        return "";
	    }
    

}
