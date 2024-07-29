package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsTextWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String,String> configParams = null;

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseParams();

        String id = param.getId();

        //param.getName()
        StringBuffer result = new StringBuffer(16);

        String inputClass = "input-xlarge input-textCounter";

        String size =configParams.get("size");
        
        String hideOnEmpty =configParams.get("hideOnEmpty");
        if (hideOnEmpty!=null && (hideOnEmpty.toLowerCase().trim().equals("true") || hideOnEmpty.toLowerCase().trim().equals("yes")) && param.getStringValue(cms).length()==0)
        	inputClass = size + " input-hideOnEmpty";
        
        if (size!=null)
        	inputClass = size + " input-textCounter";
        
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputClass ="input-block-level"; //widgetDialog.getConteinerStyleClass();
        
        result.append("<input class=\""+ inputClass +" focused item-value\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" type=\"text\" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));        
        result.append("\">\n");
        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
	
	private void parseParams(){
		
		configParams = new HashMap<String,String>();
		
		String conf = getConfiguration();
		if (conf!=null) {
			String params[] = conf.split(",");
			for (int j=0; j< params.length; j++)
	    	{
				String param[] = params[j].split(":");
				if (param.length==2)	
					configParams.put(param[0].trim(), param[1].trim());
	    		
	    	}
		}
	}
	
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsInputWidget.class.getName());
		return widgets;
	}
	

}
