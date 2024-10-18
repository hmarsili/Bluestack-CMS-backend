package com.tfsla.diario.toolbar.preview;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CPMModuleConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.toolbar.PrincipalButton;

public class PreviewButton extends PrincipalButton {

    private static final Log LOG = CmsLog.getLog(PreviewButton.class);
	
	public PreviewButton() {
		super();
		this.name = "preview";
	}

	@Override
	public void getButtonStructure(HttpServletRequest request, HttpServletResponse response, PageContext pageContext) {
		CmsJspActionElement cmsJspAction = new CmsJspActionElement(pageContext,request,response);
		TipoEdicionService tService = new TipoEdicionService();
		//Busco el nombre del sitio
		String siteName = OpenCms.getSiteManager().getCurrentSite(cmsJspAction.getCmsObject()).getSiteRoot();
		//Busco la publicacion
		
		TipoEdicion currentPublication = null;
		try {
			currentPublication = (TipoEdicion) tService.obtenerEdicionOnlineRoot(siteName.substring(siteName.lastIndexOf("/")+1));
		} catch (Exception e) {
			LOG.error(e);
		}

		HttpSession m_session = request.getSession();
        CmsWorkplaceSettings m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
		CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(m_settings.getUserSettings().getLocale());
        
		CPMConfig cmpConfig = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		CPMModuleConfig moduleConfig = cmpConfig.getModule(siteName, String.valueOf(currentPublication.getId()) , "previewFormats");

		if (moduleConfig != null){
			String[] paramsGroupNames = moduleConfig.getParamsGroupNames();
			String width = "";
			String height = "";
			String querystring = "";
			
			if (paramsGroupNames.length >0) {
				width = moduleConfig.getParamItemGroup(paramsGroupNames[0], "width"); 
				height = moduleConfig.getParamItemGroup(paramsGroupNames[0], "height"); 
				querystring = moduleConfig.getParamItemGroup(paramsGroupNames[0], "queryString"); 

				for (String groupName :paramsGroupNames ) {
					width = moduleConfig.getParamItemGroup(groupName, "width"); 
					height = moduleConfig.getParamItemGroup(groupName, "height"); 
					querystring = moduleConfig.getParamItemGroup(groupName, "queryString");
					if (querystring == null){
						querystring = "";
					}
					String previewName = messages.key("GUI_PREVIEW_"+groupName);
					PreviewSubButton subButton = new PreviewSubButton(previewName, width, height, querystring);
					this.addButton(subButton);
				}
			}
		} else {
			String previewName = messages.key("GUI_NEWS_LABEL_PREVISUALIZAR");
			PreviewSubButton subButton = new PreviewSubButton(previewName, "", "", "");
			this.addButton(subButton);
		}
	}

	@Override
	public String getValues() {
		return "";
	}

}
