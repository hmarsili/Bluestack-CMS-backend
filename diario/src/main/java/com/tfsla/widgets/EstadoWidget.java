package com.tfsla.widgets;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.utils.CmsResourceUtils;

public class EstadoWidget extends CmsSelectWidget {

	public EstadoWidget() {
		super();
	}

	public EstadoWidget(String configuration) {
		super(configuration);
	}

	
	public I_CmsWidget newInstance() {
		return new EstadoWidget(getConfiguration());
	}


	@Override
    protected List parseSelectOptions(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		CmsResource resource = ((I_CmsXmlContentValue) param).getDocument().getFile();
		
		boolean isPost = false;
		try {
		 	CmsProperty prop = cms.readPropertyObject(resource, "newsType",false);
		 	isPost = "post".equals(prop.getValue(""));
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<CmsSelectWidgetOption> list = CollectionFactory.createList();
		String actualState = this.getProperty(cms, resource);
		if(PlanillaFormConstants.PUBLICADA_VALUE.equals(actualState)) {
			list.add(newComboValue(PlanillaFormConstants.PUBLICADA_VALUE, "Publicada", "El contenido est&aacute; disponible desde el online", actualState));
			
			if (isPost)
				list.add(newComboValue(PlanillaFormConstants.RECHAZADA_VALUE, "Rechazada", "El contenido est&aacute; bloqueada en el online", actualState));
			
			}
		else {

			if (isPost) {
				list.add(newComboValue(PlanillaFormConstants.PUBLICADA_VALUE, "Publicada", "El contenido est&aacute; disponible en el online", actualState));
				list.add(newComboValue(PlanillaFormConstants.RECHAZADA_VALUE, "Rechazada", "El contenido est&aacute; bloqueada en el online", actualState));
				list.add(newComboValue(PlanillaFormConstants.PENDIENTE_MODERACION_VALUE, "Pendiente de moderaci&oacute;n", "El contenido est&aacute; pendiente de moderaci&oacute;n", actualState));

			}
			else {
			list.add(newComboValue(PlanillaFormConstants.REDACCION_VALUE, "En redacci&oacute;n", "El contenido est&aacute; siendo redactada", actualState));
			list.add(newComboValue(PlanillaFormConstants.PENDIENTE_PUBLICACION_VALUE, "Pendiente de publicaci&oacute;n", "El contenido est&aacute; esperando ser publicado", actualState));
			list.add(newComboValue(PlanillaFormConstants.PARRILLA_VALUE, "En parrilla", "El contenido est&aacute; esperando el momento de ser publicado", actualState));
			
			}
		}
		return list;
    }

	

	private CmsSelectWidgetOption newComboValue(String value, String optionValue, String help, String actualState) {
		return new CmsSelectWidgetOption(value, value.equals(actualState), optionValue, help);
	}


	private String getProperty(CmsObject cms, CmsResource resource) {
		try {
			return cms
				.readPropertyObject(CmsResourceUtils.getLink(resource), TfsConstants.STATE_PROPERTY, false)
				.getValue();
		}
		catch (Exception ex) {
			// no existe el resource todavia
			return PlanillaFormConstants.REDACCION_VALUE;
		}
	}

}
