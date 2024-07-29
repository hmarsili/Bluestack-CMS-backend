package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsTextAreaWidget extends A_TfsWidget implements I_TfsWidget {

    private static final int DEFAULT_ROWS_NUMBER = 4;
    
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        int rows = DEFAULT_ROWS_NUMBER;
        try {
            rows = new Integer(getConfiguration()).intValue();
        } catch (Exception e) {
            // ignore
        }
        StringBuffer result = new StringBuffer(16);

        String inputClass = "input-xlarge";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputClass = widgetDialog.getConteinerStyleClass();
        
        result.append("<textarea class=\""+ inputClass +" item-value\" ");
        result.append("rows=\"" + rows + "\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\">\n");
        
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        
        result.append("</textarea>");
        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
	
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsTextareaWidget.class.getName());
		return widgets;
	}
	
}
