package com.tfsla.diario.admin.widgets;

import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public interface I_TfsWidget {
	String A_CLASS = "class";
	
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param);

	public void setConfiguration(String configuration);

	public String getConfiguration();

	public String getDialogIncludes(CmsObject cms, TfsXmlContentEditor widgetDialog);

	public void setEditorValue(CmsObject cmsObject, Map parameterMap,
			TfsXmlContentEditor tfsXmlContentEditor, I_CmsWidgetParameter value);

	public List<String> getOpenCmsWidgetsClassName();
	
}
