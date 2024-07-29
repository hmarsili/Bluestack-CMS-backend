package com.tfsla.diario.admin.widgets;

import java.util.Map;
import java.util.HashMap;


import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.I_CmsXmlContentValue;


public class TfsWidgetManager {
	    
	private static Map<String,I_TfsWidget> tfsWidgets = new HashMap<String,I_TfsWidget>();

	static {
		
		//tfsWidgets.put(CmsDisplayWidget.class.getName(),new TfsLabelWidget());
		//tfsWidgets.put(CmsInputWidget.class.getName(),new TfsTextWidget());
		//tfsWidgets.put(CmsHtmlWidget.class.getName(),new TfsHTMLWidget());
		//tfsWidgets.put(CmsCheckboxWidget.class.getName(),new TfsCheckBoxWidget());
		//tfsWidgets.put(CmsVfsFileWidget.class.getName(), new TfsVfsFileWidget());
		
		//tfsWidgets.put(CmsLinkGalleryWidget.class.getName(),new TfsLinkGalleryWidget());

		//tfsWidgets.put(PropertySelectWidget.class.getName(),new TfsSelectWidget());
		//tfsWidgets.put(CmsComboWidget.class.getName(),new TfsSelectWidget());
		
		//tfsWidgets.put(EstadoWidget.class.getName(),new TfsEstadoWidget());

		//tfsWidgets.put(SeccionesComboWidget.class.getName(),new TfsSeccionesWidget());
		//tfsWidgets.put(ZonasComboWidget.class.getName(),new TfsZonasWidget());

		//tfsWidgets.put(CmsCategoryWidget.class.getName(),new TfsCategoryWidget());
	
		//tfsWidgets.put(MultipleImageWidget.class.getName(),new TfsContentMultiSelectWidget());
		//tfsWidgets.put(CmsImageGalleryWidget.class.getName(),new TfsContentMultiSelectWidget());
		//tfsWidgets.put(CmsCalendarWidget.class.getName(), new TfsCalendarWidget());
		
		//tfsWidgets.put(CmsTextareaWidget.class.getName(), new TfsTextAreaWidget());
		
		//tfsWidgets.put(EncuestaWidget.class.getName(), new TfsEncuestaWidget());
		
		//tfsWidgets.put(TipoEdicionesComboWidget.class.getName(), new TfsTipoEdicionWidget());

		//tfsWidgets.put(CmsGroupWidget.class.getName(), new TfsGroupsWidget());
		//tfsWidgets.put(CmsUserWidget.class.getName(), new TfsUserWidget());

		//tfsWidgets.put(com.tfsla.diario.admin.widgets.opencms.TfsPeriodTimeWidget.class.getName(), new TfsPeriodTimeWidget());
		//tfsWidgets.put(com.tfsla.widgets.TfsAutoCompleteBoxWidget.class.getName(), new TfsAutoCompleteBoxWidget());
		tfsWidgets.put(null,new TfsTextWidget());

	}

	public static void addWidget(I_TfsWidget widget) {
		for (String className : widget.getOpenCmsWidgetsClassName())
			tfsWidgets.put(className, widget);
	}

	public static I_TfsWidget getWidget(I_CmsWidget cmsWidget) {
		I_TfsWidget widget = tfsWidgets.get(cmsWidget.getClass().getName());
		
		if (widget==null)
			widget = tfsWidgets.get(null);

		widget.setConfiguration(cmsWidget.getConfiguration());
		
		return widget;
	}

	public static I_TfsWidget getWidget(CmsXmlContentDefinition contentDefinition, I_CmsXmlContentValue value) {
		
		I_CmsWidget cmsWidget = null;
		I_TfsWidget widget = null;
		try {
			cmsWidget = contentDefinition.getContentHandler().getWidget(value);
			widget = tfsWidgets.get(cmsWidget.getClass().getName());
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		if (widget==null)
			widget = tfsWidgets.get(null);
		
		widget.setConfiguration(cmsWidget.getConfiguration());
		
		return widget;
	 }

	 
	

}
