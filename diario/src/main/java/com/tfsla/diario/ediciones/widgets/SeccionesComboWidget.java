package com.tfsla.diario.ediciones.widgets;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.widgets.A_CmsSelectWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.data.SeccionDAO;
import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.openCmsService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SeccionesComboWidget extends A_CmsSelectWidget {

	Map<String, String> configurations = new HashMap<String, String>();

    /**
     * Creates a new select widget.<p>
     */
    public SeccionesComboWidget() {

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
    public SeccionesComboWidget(List configuration) {

        super(configuration);
    }

    /**
     * Creates a select widget with the specified select options.<p>
     *
     * @param configuration the configuration (possible options) for the select box
     */
    public SeccionesComboWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

    	parseConfiguration(cms,widgetDialog,param);
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
        
        String selected = getSelectedValue(cms, param);

        try {
        	
        	if (configurations.get("depends") == null){
	        	SeccionDAO sDAO = new SeccionDAO();
	        	List<Seccion> secciones = sDAO.getSeccionesByTipoEdicionId(getTipoEdicionId(cms,param));
	        	for (Iterator iter = secciones.iterator(); iter.hasNext();) {
	        		Seccion seccion = (Seccion) iter.next();
	
	        		String strSelected = (selected!=null && selected.equals("" + seccion.getName())) ? "selected" : "";
	        		result.append("	<option value=\"" + seccion.getName() + "\" " + strSelected + " >" + seccion.getDescription() + "</option>\n");
	    		}
        	}
        } catch (Exception e) {
			e.printStackTrace();
		}

        result.append("</select>");
        
        if(configurations.get("depends") != null){
        	String dependsControl = "OpenCmsString\\\\." + configurations.get("depends").replace("[n]","_" + getIndexWidget(param.getId()) + "_").replace("/","\\\\.") + "_1_\\\\.0";
        	String seccionControl = param.getId().replace(".","\\\\.");
        	result.append("<script language=\"javascript\">fillComboSeccion($(\"#" + seccionControl  + "\"), $(\"#" + dependsControl + "\").val(), '" + selected + "'); $(\"#" + dependsControl + "\").change(function(){fillComboSeccion($(\"#" + seccionControl + "\"), $(this).val(), '');});</script>");
        }        
        
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new SeccionesComboWidget(getConfiguration());
    }
    
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n<script language=\"javascript\">\n");
        stringBuffer.append("function fillComboSeccion(control, publicacion, selected){\n");
        stringBuffer.append("	var locationUrl = window.top.location.href;\n");
        stringBuffer.append("	locationUrl = locationUrl.substring(0,locationUrl.indexOf(\"/system\"));\n");
        stringBuffer.append("	$.ajax({\n");
        stringBuffer.append("		type: \"GET\",\n");
        stringBuffer.append("		url: locationUrl + \"/system/modules/com.tfsla.opencmsdev/templates/Secciones.xml?tipoEdicionNombre=\" + publicacion,\n");
        stringBuffer.append("		dataType: \"xml\",\n");
        stringBuffer.append("		success: function(xml) {\n");
        stringBuffer.append("			control.children().remove();\n");
        stringBuffer.append("			control.append($(\"<option />\").val('').text('Ninguna'));\n");       
        stringBuffer.append("			$(xml).find('seccion').each(function(){\n");
        stringBuffer.append("				control.append($(\"<option />\").val($(this).find('nombre').text()).text($(this).find('descripcion').text()));\n");
        stringBuffer.append("				if(selected == $(this).find('nombre').text())\n");
        stringBuffer.append("					control.val($(this).find('nombre').text());\n");
        stringBuffer.append("			});\n");
        stringBuffer.append("		}\n");  
        stringBuffer.append("});\n");
        
        stringBuffer.append("}\n");

        stringBuffer.append("</script>\n\n");
        return stringBuffer.toString();
    }     
    
    protected void parseConfiguration(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param){
 	   String configuration = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog.getMessages());
        if (configuration == null) {
            configuration = param.getDefault(cms);
        }
 	   
        String[] configurationsKeysValues = configuration.split("\\|");
        for(int i=0;i<configurationsKeysValues.length;i++){
     	   String[] items = configurationsKeysValues[i].split("=");
     	   if(items.length == 2)
     		   configurations.put(items[0], items[1]);
        }
    }    
    
    private int getIndexWidget(String name){
 	   //Parseo el nombre del control. Obtengo el caracter inmediato a "_"
 	   //Ej: OpenCmsString.publicaciones_2_.publicacion_1_.0
 	   //En este caso obtengo "2"
 	   String[] text = name.split("_"); 
 	   if(text.length >= 2)
 		   return Integer.parseInt(text[1]);
 	   else
 		   return 0;
    }    

    private int getTipoEdicionId(CmsObject cms, I_CmsWidgetParameter value) throws Exception
    {
    	String publicationName = "contenidos";
    	String siteName = openCmsService.getSiteName(cms.getRequestContext().getSiteRoot());
    	
    	try {
	    	if (((I_CmsXmlContentValue)value).getDocument().getFile()!=null)
	    	{
	    		siteName = ((I_CmsXmlContentValue)value).getDocument().getFile().getRootPath();

	    		siteName = siteName.replaceAll("/sites/", "");
	    		publicationName = siteName;

	    		siteName = siteName.substring(0, siteName.indexOf("/"));

	    		publicationName = publicationName.replace(siteName + "/", "");
	    		publicationName = publicationName.substring(0, publicationName.indexOf("/"));
	    	}
    	}
    	catch (Exception e) {
    		publicationName = "contenidos";
    	}

    	TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
    	TipoEdicion tEdicion = null;

    	// fijarse si el diario es el online.
    	if (publicationName.equals("contenidos"))
    		tEdicion = tDAO.getTipoEdicionOnlineRoot(siteName);
    	else {
    		tEdicion = tDAO.getTipoEdicion(siteName, publicationName);
    		if (tEdicion==null)
    			tEdicion = tDAO.getTipoEdicionOnlineRoot(siteName);
    	}
    	return tEdicion.getId();

    }

}
