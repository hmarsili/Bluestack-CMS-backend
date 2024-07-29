package com.tfsla.widgets;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

public class TfsAutoCompleteBoxWidget extends CmsTextareaWidget {

	//CustomSourceConfiguration m_config;
	public TfsAutoCompleteBoxWidget() {
		super();
	}

	public TfsAutoCompleteBoxWidget(String configuracion) {
		super(configuracion);
	}

	@Override
	public I_CmsWidget newInstance() {
		return new TfsAutoCompleteBoxWidget(getConfiguration());
	}

	/*@Override
	public void setEditorValue(CmsObject cms, Map formParameters, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		PropertyWidgetHelper.beforeSetEditorValue(param);
		super.setEditorValue(cms, formParameters, widgetDialog, param);
	}*/

	 @Override
	public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		//PropertyWidgetHelper.beforeGetDialogWidget(cms, param, this.getSelectOptions());
		 return super.getDialogWidget(cms, widgetDialog, param);
	 }
}
