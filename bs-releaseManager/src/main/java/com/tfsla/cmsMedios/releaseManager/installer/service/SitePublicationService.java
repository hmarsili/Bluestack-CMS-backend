package com.tfsla.cmsMedios.releaseManager.installer.service;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;

public class SitePublicationService {
	public static int getPublicationId(CmsObject cmsObject) throws Exception {
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		String defaultPublication = (String)cmsObject.getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingspublication");
		
		TipoEdicion currentPublication = null;
		if (defaultPublication != null && !defaultPublication.equals("") && !defaultPublication.trim().toLowerCase().equals("null")) {
			currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(defaultPublication) );
		} else {
			String proyecto = getCurrentSite(cmsObject);
			currentPublication = tService.obtenerEdicionOnlineRoot(proyecto);
		}
		return currentPublication.getId();
	}
	
	private static String getCurrentSite(CmsObject cmsObject) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
		
		if(siteName.substring(siteName.length() - 1, siteName.length()).equals("/"))
			siteName = siteName.substring(0, siteName.length() - 1);
		
		siteName = siteName.replaceAll("/sites/", "");

		return siteName;
	}
}
