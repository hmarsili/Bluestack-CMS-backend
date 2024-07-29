package com.tfsla.diario.admin.widgets;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.ediciones.data.PageDAO;
import com.tfsla.diario.ediciones.data.ZoneDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.Zona;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.widgets.ZonasComboWidget;
import com.tfsla.diario.securityService.TfsUserAuditPermission;

public class TfsZonasWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String, String> configurations = new HashMap<String, String>();

    private List<CmsSelectWidgetOption> m_selectOptions;

  
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseConfiguration(cms,widgetDialog,param);
        String id = param.getId();
        StringBuffer result = new StringBuffer(16);
        
        String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:98%\" ";
        
        result.append("<select class=\"chzn-select item-value\" ");
        result.append(inputWidth);
        result.append(" content-definition=\"" + param.getName() + "\" ");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append("name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\" id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\">");
        
        int pagina = getPagina();
        String selected = getSelectedValue(cms, param);

        try {
        	
        	if (configurations.get("depends") == null){
        		
	            String strSelected = (selected!=null && selected.equals("no_mostrar")) ? "selected" : "";
	            result.append("	<option value=\"no_mostrar\" " + strSelected + " >No Mostrar</option>\n");

        		ZoneDAO zDAO = new ZoneDAO();
	        	List<Zona> zonas = zDAO.getZonas(getTipoEdicionId(cms,param),pagina);
	        	for (Iterator iter = zonas.iterator(); iter.hasNext();) {
	        		Zona zona = (Zona) iter.next();
	
	        		strSelected = (selected!=null && selected.equals("" + zona.getName())) ? "selected" : "";
	        		result.append("	<option value=\"" + zona.getName() + "\" " + strSelected + " >" + zona.getDescription() + "</option>\n");
	
	    		}
        	}
        } catch (Exception e) {
			e.printStackTrace();
		}

        result.append("</select>");
        
        if(configurations.get("depends") != null){
        	boolean isZonaAvailable = true;
	        	
        	try{
	        	TfsUserAuditPermission tfsAudit = new TfsUserAuditPermission();
	        	String user = cms.getRequestContext().currentUser().getName();
	        	isZonaAvailable = tfsAudit.isPublicationAvailable(user, cms, getTipoEdicionId(cms,param));
    
	        	
        	} catch (Exception e) {
        		e.printStackTrace();
       		}
        		
        	int idx = configurations.get("depends").lastIndexOf("/")+1;
        	
        	String elementNameDependecy = configurations.get("depends").substring(idx);
        	result.append("\n<script language=\"javascript\">\n");
        	result.append("$(document).ready(function() { \n");
        	
        	if(isZonaAvailable){
        		result.append("\t fillPublicacionComboZona('" + widgetDialog.getIdElement(id) + "','" + elementNameDependecy + "', '" + pagina + "','" + selected + "' );\n");
        	}else{
        		result.append("\t fillPublicacionComboZonaRestricted('" + widgetDialog.getIdElement(id) + "','" + elementNameDependecy + "', '" + pagina + "','" + selected + "', false );\n");
        	}
        	
        	result.append("});\n");
        	result.append("</script>\n");
        }  

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
    
    protected void parseConfiguration(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param){
    	configurations = new HashMap<String, String>();
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
    
    
    private int getTipoEdicionId(CmsObject cms, I_CmsWidgetParameter value) throws Exception
    {
    	String path = cms.getSitePath(((I_CmsXmlContentValue)value).getDocument().getFile());
		
		TipoEdicionService tEService = new TipoEdicionService();

		TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cms, path);
		
		return tEdicion.getId();
    }
    
    protected int getPagina() {
 	   try {
            String pagina = configurations.get("page");
            PageDAO pDAO = new PageDAO();

            return pDAO.getPage(pagina).getIdPage();
 	   }
 	   catch(Exception e){return 0;}
    }
    
    @Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(ZonasComboWidget.class.getName());
		return widgets;
	}
    
}
