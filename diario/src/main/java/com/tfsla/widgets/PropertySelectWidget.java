package com.tfsla.widgets;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

public class PropertySelectWidget extends CmsSelectWidget {

	public PropertySelectWidget() {
		super();
	}

	public PropertySelectWidget(String configuration) {
		super(configuration);
	}

	@Override
	public I_CmsWidget newInstance() {
		return new PropertySelectWidget(this.getConfiguration());
	}
	
	public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		 PropertyWidgetHelper.beforeGetDialogWidget(cms, param, this.getSelectOptions());
		 return super.getDialogWidget(cms, widgetDialog, param);
	 }


	
	@Override
	public void setEditorValue(CmsObject cms, Map formParameters, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		PropertyWidgetHelper.beforeSetEditorValue(param);
		super.setEditorValue(cms, formParameters, widgetDialog, param);
	}



}
