package com.tfsla.opencms.webusers.openauthorization;

import javax.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfiguration;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfigurationLoader;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;

public abstract class GenericProvider implements IOpenProvider {
	
	protected String siteName="";
	protected String publication="";
	protected String access_token="";
	protected HttpServletRequest request;
	protected ProviderConfiguration configuration;
	
	protected abstract String getModuleName();
	
	public void setConfiguration(HttpServletRequest request) {
		this.request = request;

    	CmsFlexController controller = CmsFlexController.getController(request);
    	CmsObject cms = controller.getCmsObject();
        
    	siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ProviderConfiguration getConfiguration() throws InvalidConfigurationException {
		if(this.configuration == null) {
			ProviderConfigurationLoader configLoader = new ProviderConfigurationLoader();
			configuration = configLoader.getConfiguration(getModuleName(), siteName, publication);
		}
		return this.configuration;
	}
}
