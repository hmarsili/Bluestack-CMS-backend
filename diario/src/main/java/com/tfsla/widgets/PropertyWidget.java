package com.tfsla.widgets;

import org.apache.commons.lang.StringEscapeUtils;
import org.opencms.file.CmsObject;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.I_CmsXmlContentValue;


public class PropertyWidget extends A_CmsWidget{
	
	public PropertyWidget() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PropertyWidget(String configuration) {
		super(configuration);
	}

	public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		String value = PropertyWidgetHelper.getProperty(cms, (I_CmsXmlContentValue) param, this.getConfiguration());
        StringBuffer result = new StringBuffer(); 
        result.append("<td class=\"xmlTd\">");
        result.append("<label for=\"");
        result.append(param.getId());
        result.append("\">");
        if(value != null) {
        result.append(StringEscapeUtils.escapeHtml(value));
        }
        result.append("</label>");
        result.append("</td>");
        return result.toString();
	}


	public I_CmsWidget newInstance() {
		return new PropertyWidget(getConfiguration());
	}

}
