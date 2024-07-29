package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.diario.ediciones.widgets.TipoEdicionesComboWidget;
import com.tfsla.diario.securityService.TfsUserAuditPermission;

public class TfsTipoEdicionWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String, String> configurations = new HashMap<String, String>();

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseConfiguration(cms,widgetDialog,param);

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);
        
        String inputWidth = "";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputWidth = "style=\"width:98%\" ";
        
        result.append("<select class=\"chzn-select item-value\" ");
        result.append(inputWidth);
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\">\n");
        
        
        
        String selected = getSelectedValue(cms,param);

        String allowEmpty = configurations.get("allowEmpty");
        if (allowEmpty!=null && allowEmpty.toLowerCase().equals("true")){
	        result.append("<option ");
	        if (selected.equals(""))
	        	result.append("selected ");
	        
	        String value = configurations.get("value");
	        if (value!=null){
	        	result.append("value=\""+ value +"\">");
	        }else{
	        	result.append("value=\"\">");
	        }	        
	        
	        if (value!=null){
	        	result.append(value);
	        }else{
	        	result.append("Ninguna");
	        }
	        result.append("</option>\n");
        	
        }
        TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
        try {        	
        	TfsUserAuditPermission tfsAudit = new TfsUserAuditPermission();
        	String user = cms.getRequestContext().currentUser().getName();
        	
        	List<TipoEdicion> tipoEdiciones = tDAO.getTipoEdiciones(openCmsService.getCurrentSite(cms));
        	for (TipoEdicion tipoEdicion : tipoEdiciones) {
        		if(selected.equals(tipoEdicion.getNombre())){
        			result.append("<option ");
	    	        if (selected.equals(tipoEdicion.getNombre()))
	    	        	result.append("selected ");
	    	        
	    	        result.append("value=\"");
	    	        result.append(tipoEdicion.getNombre());
	    	        result.append("\">");
	    	        
	    	        result.append(tipoEdicion.getDescripcion());
	    	        result.append("</option>\n");        		
        		}else{
	        		if(tfsAudit.isPublicationAvailable(user, cms, tipoEdicion.getId())){
		    	        result.append("<option ");
		    	        	    	        
		    	        result.append("value=\"");
		    	        result.append(tipoEdicion.getNombre());
		    	        result.append("\">");
		    	        
		    	        result.append(tipoEdicion.getDescripcion());
		    	        result.append("</option>\n");
	        		}
        		}
    		}
        } catch (Exception e) {
			e.printStackTrace();
		}
        result.append("</select>\n");

        

        return result.toString();
    }
	
    protected String getSelectedValue(CmsObject cms, I_CmsWidgetParameter param) {

        return (param.getStringValue(cms)!=null ? param.getStringValue(cms) : "");
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


	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		List<String> widgets = new ArrayList<String>();
		widgets.add(TipoEdicionesComboWidget.class.getName());
		return widgets;
	}   
}
