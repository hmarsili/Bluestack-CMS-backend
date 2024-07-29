package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CPMModuleConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsPreviewConfWidget  extends A_TfsWidget implements I_TfsWidget {

	Map<String, String> configurations = new HashMap<String, String>();

    private List<CmsSelectWidgetOption> m_selectOptions;

   public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

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
        
        String selected = getSelectedValue(cms, param);

        try {
     	
        	TipoEdicion tEdicion = getTipoEdicion(cms,param);
        	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    		CPMConfig cmpConfig = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    		
    		CPMModuleConfig moduleConfig = cmpConfig.getModule(siteName, String.valueOf(tEdicion.getId()) , "previewFormats");

    		String strSelected = (selected!=null && selected.equals("")) ? "selected" : "";
            result.append("	<option value=\"General\" " + strSelected + " >General</option>\n");
            if (moduleConfig != null){
    			String[] paramsGroupNames = moduleConfig.getParamsGroupNames();
    			for (String previewName : paramsGroupNames) {
    				
    				String previewNameDesc = widgetDialog.getMessages().key("GUI_PREVIEW_"+previewName);
    				
            		strSelected = (selected!=null && selected.equals("" + previewName)) ? "selected" : "";
            		result.append("	<option value=\"" + previewName + "\" " + strSelected + " >" + previewNameDesc + "</option>\n");    				
    			}
            }
        } catch (Exception e) {
			e.printStackTrace();
		}

        result.append("</select>");
        

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
    
    
    
    private TipoEdicion getTipoEdicion(CmsObject cms, I_CmsWidgetParameter value) throws Exception
    {
    	String path = cms.getSitePath(((I_CmsXmlContentValue)value).getDocument().getFile());
		
		TipoEdicionService tEService = new TipoEdicionService();

		TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cms, path);
		
		return tEdicion;
    }
        
    @Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsPreviewConfWidget.class.getName());
		return widgets;
	}
    

}
