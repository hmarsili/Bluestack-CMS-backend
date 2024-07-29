package com.tfsla.diario.admin.widgets;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.A_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;


public abstract class A_TfsWidget implements I_TfsWidget {

	protected String m_configuration;
	
	public void setConfiguration(String configuration) {
		m_configuration = configuration;
		
	}

	public String getConfiguration() {
		// TODO Auto-generated method stub
		return m_configuration;
	}

	public String getDialogIncludes(CmsObject cms, TfsXmlContentEditor widgetDialog)
	{
		return "";
	}
	
	protected String getTypeName(I_CmsWidgetParameter param)
	{
		return ((A_CmsXmlContentValue)param).getTypeName();	
	}
	
	public void setEditorValue(
			CmsObject cms, 
			Map formParameters,
			TfsXmlContentEditor tfsXmlContentEditor, 
			I_CmsWidgetParameter param) {

	        String[] values = (String[])formParameters.get(param.getId());
	        if ((values != null) && (values.length > 0)) {
	            param.setStringValue(cms, values[0]);
	        }
	    }
}
