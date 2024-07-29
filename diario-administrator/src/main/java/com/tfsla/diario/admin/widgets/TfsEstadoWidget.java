package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.widgets.EstadoWidget;

public class TfsEstadoWidget extends A_TfsWidget implements I_TfsWidget {

    private List<CmsSelectWidgetOption> m_selectOptions;

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        List<CmsSelectWidgetOption> options = parseSelectOptions(cms, widgetDialog, param);

        
        String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:98%\" ";
        
        result.append("<select class=\"chzn-select item-value\" ");
        result.append(inputWidth);
        result.append(" content-definition=\"" + param.getName() + "\" ");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\">\n");
        
        String selected = getSelectedValue(cms,param);
        
        for (CmsSelectWidgetOption option : options) {
	        result.append("<option ");
	        if (selected!=null && selected.equals(option.getValue()))
	        	result.append("selected ");
	        
	        result.append("value=\"");
	        result.append(option.getValue());
	        result.append("\">");
	        
	        result.append(option.getOption());
	        result.append("</option>\n");
        }
        result.append("</select>\n");

        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
	
    protected String getSelectedValue(CmsObject cms, I_CmsWidgetParameter param) {

        String paramValue = param.getStringValue(cms);
        if (CmsStringUtil.isEmpty(paramValue)) {
            CmsSelectWidgetOption option = CmsSelectWidgetOption.getDefaultOption(m_selectOptions);
            if (option != null) {
                paramValue = option.getValue();
            }
        }
        return paramValue;
    }
    
    protected List parseSelectOptions(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
		CmsResource resource = ((I_CmsXmlContentValue) param).getDocument().getFile();
		
		boolean isPost = false;
		try {
		 	CmsProperty prop = cms.readPropertyObject(resource, "newsType",false);
		 	isPost = "post".equals(prop.getValue(""));
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_selectOptions = CollectionFactory.createList();
		String actualState = this.getProperty(cms, resource);
		if(PlanillaFormConstants.PUBLICADA_VALUE.equals(actualState)) {
			m_selectOptions.add(newComboValue(PlanillaFormConstants.PUBLICADA_VALUE, "Publicada", "El contenido est&aacute; disponible desde el online", actualState));
			
			if (isPost)
				m_selectOptions.add(newComboValue(PlanillaFormConstants.RECHAZADA_VALUE, "Rechazada", "El contenido est&aacute; bloqueada en el online", actualState));
			
			}
		else {

			if (isPost) {
				m_selectOptions.add(newComboValue(PlanillaFormConstants.PUBLICADA_VALUE, "Publicada", "El contenido est&aacute; disponible en el online", actualState));
				m_selectOptions.add(newComboValue(PlanillaFormConstants.RECHAZADA_VALUE, "Rechazada", "El contenido est&aacute; bloqueada en el online", actualState));
				m_selectOptions.add(newComboValue(PlanillaFormConstants.PENDIENTE_MODERACION_VALUE, "Pendiente de moderaci&oacute;n", "El contenido est&aacute; pendiente de moderaci&oacute;n", actualState));

			}
			else {
				m_selectOptions.add(newComboValue(PlanillaFormConstants.REDACCION_VALUE, "En redacci&oacute;n", "El contenido est&aacute; siendo redactada", actualState));
				m_selectOptions.add(newComboValue(PlanillaFormConstants.PENDIENTE_PUBLICACION_VALUE, "Pendiente de publicaci&oacute;n", "El contenido est&aacute; esperando ser publicado", actualState));
				m_selectOptions.add(newComboValue(PlanillaFormConstants.PARRILLA_VALUE, "En parrilla", "El contenido est&aacute; esperando el momento de ser publicado", actualState));
			
			}
		}
		return m_selectOptions;
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
	
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(EstadoWidget.class.getName());
		return widgets;
	}
	


}
