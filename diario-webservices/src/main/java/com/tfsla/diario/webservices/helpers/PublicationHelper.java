package com.tfsla.diario.webservices.helpers;

import jakarta.servlet.http.HttpSession;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;

public class PublicationHelper {
	
	public static synchronized String getCurrentPublication(String site) {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion;
		String publication = "1";
		try {
			tEdicion = tService.obtenerEdicionOnlineRoot(site);
			publication = String.valueOf(tEdicion.getId());
		} catch (Exception e) {
			return publication;
		}
		
		return publication;
	}
	
	public static synchronized String getCurrentPublication(CmsObject cms) {
		
		try {
			return getCurrentPublicationWithoutSettings(cms);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		TipoEdicionService tService = new TipoEdicionService();
		String defaultPublication = (String)cms.getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingspublication");
		
		TipoEdicion currentPublication = null;
		if (defaultPublication != null && !defaultPublication.equals("") && !defaultPublication.trim().toLowerCase().equals("null")) {
			currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(defaultPublication) );
		} else {
			String proyecto = openCmsService.getCurrentSite(cms);
			try {
				currentPublication = tService.obtenerEdicionOnlineRoot(proyecto);
			} catch(Exception e) {
				return "1";
			}
		}
		return String.valueOf(currentPublication.getId());
	}
	
	public static String getCurrentPublicationWithoutSettings(CmsObject cmsObject) {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = null;
		
		//Si no tiene publicacion seteada o la misma no se encuentra en el sitio 
		try {
			currentPublication = tService.obtenerTipoEdicion(cmsObject,cmsObject.getRequestContext().getUri());
		} catch (Exception e1) {
			e1.printStackTrace();
		}			
		
		if (currentPublication==null) {
			String siteName = openCmsService.getSiteName(cmsObject.getRequestContext().getSiteRoot());
			try {
				currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return String.valueOf(currentPublication.getId());
	}
	
	public static String getFromResource(CmsObject cms, CmsResource resource) throws Exception {
		TipoEdicionService tService = new TipoEdicionService();
        TipoEdicion tEdicion = null;
           
		try {
			tEdicion = tService.obtenerTipoEdicion(cms, cms.getSitePath(resource));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return String.valueOf(tEdicion.getId());
	}
	
	public static String getFromSession(CmsObject cmsObject, HttpSession session) throws Exception {
		TipoEdicion currentPublication = (TipoEdicion) session.getAttribute("currentPublication");
		TipoEdicionService tService = new TipoEdicionService();
		
		if (currentPublication==null) {
			String proyecto = openCmsService.getCurrentSite(cmsObject);
			currentPublication = tService.obtenerEdicionOnlineRoot(proyecto);
			//session.setAttribute("currentPublication",currentPublication);
		}
		
		return String.valueOf(currentPublication.getId());
	}
}
