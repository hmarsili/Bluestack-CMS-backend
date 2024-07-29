package com.tfsla.widgets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.TfsContext;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.utils.CmsResourceUtils;

public class PropertyWidgetHelper {

	
	public static String getSelectedValue(CmsObject cms, I_CmsWidgetParameter param, List selectedOptions) {
		I_CmsXmlContentValue params = (I_CmsXmlContentValue) param;
		String value = getProperty(cms, params, getPropertyName(params));
		return !CmsStringUtil.isEmpty(value) ? value : getDefaultOption(selectedOptions);
	}

	public static String getPropertyName(I_CmsXmlContentValue params) {
		try {
			return ((CmsDefaultXmlContentHandler)params.getContentDefinition().getContentHandler()).getMappings(params.getName() + "[1]")[0].split(":")[1];
		}
		catch(NullPointerException e) {
			throw new RuntimeException("Debe declarar el mapeo de la property para el elemento " + params.getName());
		}
		catch(IndexOutOfBoundsException e) {
			throw new RuntimeException("Debe declarar el mapeo de la property para el elemento " + params.getName());
		}
	}

	public static String getDefaultOption(List selectedOptions) {
		CmsSelectWidgetOption option = CmsSelectWidgetOption.getDefaultOption(selectedOptions);
		return option != null ? option.getValue() : "";
	}
	
	public static String getProperty(CmsObject cms, I_CmsXmlContentValue param, String property) {
		try {
			return cms.readPropertyObject(CmsResourceUtils.getLink(param.getDocument().getFile()), property, false).getValue();
		}
		catch (Exception ex) {
			//Si no se pudo leer la property, es que no existe
			CmsLog.getLog(PropertyWidgetHelper.class).fatal(ex);
			return null;
		}
	}
	
	public static void beforeGetDialogWidget(CmsObject cms, I_CmsWidgetParameter param, List selectedOptions) {
		
		String param2 = "FLAG_" + param.getName();
		
		HttpServletRequest request = TfsContext.getInstance().getRequest();
		
		if(request != null)
		{
			if(TfsContext.getInstance().getRequest().getAttribute(param2) == null) {
				param.setStringValue(cms, PropertyWidgetHelper.getSelectedValue(cms, param, selectedOptions));
			}
		}
	}

	public static void beforeSetEditorValue(I_CmsWidgetParameter param) {
		TfsContext.getInstance().getRequest().setAttribute(param.getName(), "true");
	}



}
