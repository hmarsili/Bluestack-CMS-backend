package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.CmsXmlBooleanValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsCheckBoxWidget extends A_TfsWidget implements I_TfsWidget  {

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        result.append("<input class=\"switchCheck item-value\" ");
        result.append(" data-size=\"small\"");
        result.append(" data-on-color=\"success\"");
        result.append(" data-off-color=\"default\"");
        result.append(" data-handle-width=\"33\"");
        result.append(" id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\" ");
        boolean booleanValue = CmsXmlBooleanValue.getBooleanValue(cms, param);
        if (booleanValue) {
            result.append(" checked=\"checked\"");
        }
        result.append(" type=\"checkbox\" value=\"");
        result.append(booleanValue);        
        result.append("\">\n");
        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
	
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsCheckboxWidget.class.getName());
		return widgets;
	}
	
	
}
