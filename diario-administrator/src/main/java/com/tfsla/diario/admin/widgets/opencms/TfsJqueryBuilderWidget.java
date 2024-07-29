package com.tfsla.diario.admin.widgets.opencms;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

public class TfsJqueryBuilderWidget extends A_CmsWidget {

	@Override
	public String getDialogWidget(CmsObject cms,
			I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		String id = param.getId();
		StringBuffer result = new StringBuffer(16);
    	
    	result.append("<td class=\"xmlTd\">");
        result.append("<input class=\"xmlInput textInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\"");
        result.append(" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\">");
        result.append("</td>");

        return result.toString();
        
	}

	@Override
	public I_CmsWidget newInstance() {
		// TODO Auto-generated method stub
		return new TfsJqueryBuilderWidget();
	}

}
