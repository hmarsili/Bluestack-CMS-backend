package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.widgets.EncuestaWidget;

public class TfsEncuestaWidget  extends A_TfsWidget implements I_TfsWidget {
	
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        String actualValue = param.getStringValue(cms);
        
        
        StringBuffer result = new StringBuffer(16);

        boolean hasValue = actualValue!=null && !actualValue.equals("");
        

    	String actualTitle = "";
 			if (hasValue)
 				actualTitle = getActualTitle(cms, actualValue);
 			
    		result.append("<label id=\"lblPreview_" + widgetDialog.getIdElement(id) + "\"" + (hasValue ? "" : " style=\"display: none;\"") + " >" + actualTitle + "</label>\n");
    	
        
        String modalName = "pollsModal";
        String className = "input-related-poll";
        String buttonClassName = "btn-multiselectPolls";
        
        
     	result.append("<div class=\"input-append\">\n");
     	
     	result.append("<input class=\"input-xlarge item-value focused " + className + "\" type=\"text\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\"");
        result.append(">\n");

        result.append("<a onclick=\"createPoll()\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success btn-createPoll\" rel=\"tooltip\" data-original-title=\"Crear encuesta\"");
        result.append("><i class=\"material-icons\">add</i></a>");   
        
        result.append("<a data-target=\"#" + modalName + "\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success "+ buttonClassName + "\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Seleccionar\"><i class=\"material-icons\">search</i></a>");
        result.append("</div>");
	   	


        return result.toString();
    }
		   
    private String getActualTitle(CmsObject cms, String actualPath) {
        try {
        	
        	Encuesta encuesta = Encuesta.getEncuestaFromURL(cms, actualPath);
        	
            return encuesta.getPregunta();
        }
        catch (Exception e) {
			// TODO Auto-generated catch block
        	return "El elemento seleccionado no es valido";
		}
    }
    
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(EncuestaWidget.class.getName());
		return widgets;
	}
    

}
