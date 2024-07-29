package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsTotalCookingTimeWidget extends A_TfsWidget implements I_TfsWidget {
	
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);
        
        String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:10%\" ";
        
        result.append("<input class=\"input-small item-value\"");
        result.append(" id=\""+id+"Text\"");
        result.append(" content-definition=\"" + param.getName() + "Text\"");
        result.append(" type=\"text\"");
		result.append(" name=\""+id +"Text\""  );
        result.append(" disabled />\n");
        
        result.append("<input class=\"input-small item-value\"");
        result.append(" id=\""+id+"\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" type=\"hidden\"");
		result.append(" name=\""+id +"\""  );
		result.append(" value=\"" + getWidgetValue(cms, widgetDialog, param) + " \"");
        result.append(" disabled />\n");
       
        return result.toString();

	}
	
	 public String getWidgetValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
	        String result = param.getStringValue(cms);
	        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result))
	        	return result;
	        return "";
	    
	 }
	 
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		List<String> widgets = new ArrayList<String>();
		widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsTotalCookingTimeWidget.class.getName());
		return widgets;
	}
	
}
