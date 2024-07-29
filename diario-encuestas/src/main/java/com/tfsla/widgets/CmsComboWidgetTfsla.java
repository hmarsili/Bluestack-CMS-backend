package com.tfsla.widgets;

import java.util.Iterator;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsSelectWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;

public class CmsComboWidgetTfsla extends A_CmsSelectWidget {
	
	public CmsComboWidgetTfsla() {
		// empty constructor is required for class registration
		super ();
	}
	
	public CmsComboWidgetTfsla(List configuration) {
		 super (configuration);
	}
	
	public CmsComboWidgetTfsla(String configuration) {
		super (configuration);
	}
	
	public String getDialogIncludes(CmsObject cms,I_CmsWidgetDialog widgetDialog) {
			
		StringBuffer result = new StringBuffer(16);
		
		result.append(getJSIncludeFile(CmsWorkplace.getSkinUri()+ "components/widgets/combobox.js"));
		return result.toString();
	}
	
	
	public String getDialogInitCall(CmsObject cms,I_CmsWidgetDialog widgetDialog) {
			return "initComboBox();\n";
	}
	
	public String getDialogHtmlEnd(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
			
		    StringBuffer result = new StringBuffer(256);
			
		    int minOcurrence = param.getMinOccurs();
		    int maxOcurrence = param.getMaxOccurs();
		    int cant = maxOcurrence-minOcurrence;
		    
		  for(int j=0; j<=cant; j++ ){
			  
			String comboId=param.getName()+"."+j;
			
		    // get the select box options
			List options = parseSelectOptions(cms, widgetDialog, param);
			
			if (options.size() > 0) {
			     // create combo div
			     result.append("<div class=\"widgetcombo\" id=\"combo");
			     result.append(comboId);
			     result.append("\">\n");
			
			     int count = 0;
				 Iterator i = options.iterator();
				 
				 while (i.hasNext()) {
					 CmsSelectWidgetOption option = (CmsSelectWidgetOption) i.next();
					 String itemId = new StringBuffer(64).append("ci").append(comboId).append('.').append(count).toString();
			
					 // create the link around value
					 result.append("\t<a href=\"javascript:setComboValue(\'");
					 result.append(comboId);
					 result.append("\', \'");
					 result.append(itemId);
				     result.append("\')\" name=\"");
				     result.append(itemId);
				     result.append("\" id=\"");
			         result.append(itemId);
			         result.append("\"");
			         result.append(">");
			         result.append(option.getValue());
			         result.append("</a>\n");  
			         count++;
				 }
				
				 // close combo div
				 result.append("</div>\n");
			}
				 
			}
			
			// return the icon help text from super class
			result.append(super .getDialogHtmlEnd(cms, widgetDialog, param));
			       return result.toString();
	}

	public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		 
		String id = param.getId();
		 StringBuffer result = new StringBuffer(16);
		 result.append("<td class=\"xmlTd\">");
		 result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
		 
		 // medium text input field
		 result.append("<input type=\"text\" class=\"xmlInputMedium");
		 
		 if (param.hasError()) {
			 result.append(" xmlInputError");
		 }
		 
		 result.append("\" name=\"");
		 result.append(id);
		 result.append("\" id=\"");
		 result.append(id);
		 result.append("\"");
		 
		 String selected = getSelectedValue(cms, param);
		 
		 if (selected != null) {
		 
			// append the selection 
		 	result.append(" value=\"");
		 	result.append(CmsEncoder.escapeXml(selected));
		 	result.append("\"");
		 }
		
		 result.append(">");
		 result.append("</td><td>");
		 
		 // button to open combo box
		 result.append("<button name=\"test\" onclick=\"showCombo(\'").append(id).append("\', \'combo").append(id);
		 
		 result.append("\');return false;\" class=\"widgetcombobutton\">");
		 result.append("<img src=\"");
		 result.append(CmsWorkplace.getSkinUri()).append("components/widgets/combo.png");
		 result.append("\" width=\"7\" height=\"12\" alt=\"\" border=\"0\">");
		 result.append("</button></td></tr></table>");
		 
		 result.append("</td>");
		 return result.toString();
	}

	public I_CmsWidget newInstance() {
		return new CmsComboWidget(getConfiguration());
	}

}
