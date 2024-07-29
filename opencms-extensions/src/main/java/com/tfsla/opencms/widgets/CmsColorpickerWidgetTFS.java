package com.tfsla.opencms.widgets;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsColorpickerWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

/**
 * Esta clase modifica el metodo setEditorValue del objeto CmsColorpickerWidget (evita un error de casteo del
 * metodo y quita el "#")
 * 
 * @author Victor Podberezski (vpod@tfsla.com)
 */
public class CmsColorpickerWidgetTFS extends CmsColorpickerWidget {

	@Override
	public void setEditorValue(CmsObject cms, Map formParameters, I_CmsWidgetDialog widgetDialog,
			I_CmsWidgetParameter param) {

		String[] values = (String[]) formParameters.get(param.getId());
		if ((values != null) && (values.length > 0)) {
			String castColorValue = param.getStringValue(cms).replace("#", "");
			String colorValue = values[0].trim().replace("#", "");
			if (CmsStringUtil.isNotEmpty(colorValue)) {
				castColorValue = colorValue;
			}
			param.setStringValue(cms, String.valueOf(castColorValue));
		}

	}
}
