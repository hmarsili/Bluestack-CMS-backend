package com.tfsla.diario.admin.jsp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsCmsMediosConfig {

    private CmsFlexController m_controller;
    private HttpSession m_session;
    
    private String siteName;
    private TipoEdicion currentPublication;
    private String publication;
    private String moduleName;
    private CPMConfig config;

    public CmsObject getCmsObject() {
        return m_controller.getCmsObject();
    }

	public TfsCmsMediosConfig(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception
    {
		m_controller = CmsFlexController.getController(req);
        m_session = req.getSession();
        
    	siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
    	

    	currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	try {
    		publication = "" + currentPublication.getId();
    	} catch (Exception ex) {
    		publication = "";
    	}
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

        
    }

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return this.moduleName;
	}
	
	public String getParam(String name)
	{
		return config.getParam(siteName, publication, moduleName,name);
	}

	public String getParam(String group, String name)
	{
		return config.getItemGroupParam(siteName, publication, moduleName, group, name);		
	}


}
