package com.tfsla.diario.ediciones.widgets;

import org.opencms.file.CmsObject;
import org.opencms.widgets.A_CmsSelectWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.openCmsService;

import java.util.Iterator;
import java.util.List;


public class TipoEdicionesComboWidget extends A_CmsSelectWidget {


    /**
     * Creates a new select widget.<p>
     */
    public TipoEdicionesComboWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a select widget with the select options specified in the given configuration List.<p>
     *
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     *
     * @param configuration the configuration (possible options) for the select widget
     *
     * @see CmsSelectWidgetOption
     */
    public TipoEdicionesComboWidget(List configuration) {

        super(configuration);
    }

    /**
     * Creates a select widget with the specified select options.<p>
     *
     * @param configuration the configuration (possible options) for the select box
     */
    public TipoEdicionesComboWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\" style=\"height: 25px;\"><select class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");

        TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
        try {
        	List<TipoEdicion> tipoEdiciones = tDAO.getTipoEdiciones(openCmsService.getCurrentSite(cms));
        	String selected = getSelectedValue(cms, param);
        	for (Iterator iter = tipoEdiciones.iterator(); iter.hasNext();) {
        		TipoEdicion tipoEdicion = (TipoEdicion) iter.next();

        		String strSelected = (selected!=null && selected.equals("" + tipoEdicion.getNombre())) ? "selected" : "";
        		result.append("	<option value=\"" + tipoEdicion.getNombre() + "\" " + strSelected + " >" + tipoEdicion.getDescripcion() + "</option>\n");

    		}
        } catch (Exception e) {
			e.printStackTrace();
		}

        result.append("</select>");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new TipoEdicionesComboWidget(getConfiguration());
    }
}
