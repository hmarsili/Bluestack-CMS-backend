package com.tfsla.diario.ediciones.services;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.diario.ediciones.model.TipoEdicion;

public class PublicationService {
	protected static final Log LOG = CmsLog.getLog(PublicationService.class);
	
	public static int getPublicationId(CmsObject cmsObject) throws Exception {
		TipoEdicionService tService = new TipoEdicionService();
		String defaultPublication = (String)cmsObject.getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingspublication");
		
		TipoEdicion currentPublication = null;
		if (defaultPublication != null && !defaultPublication.equals("") && !defaultPublication.trim().toLowerCase().equals("null")) {
			currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(defaultPublication) );
		} else {
			String proyecto = openCmsService.getCurrentSite(cmsObject);
			currentPublication = tService.obtenerEdicionOnlineRoot(proyecto);
		}
		return currentPublication.getId();
	}
	
	/*
	 * Se agrega para entregar el id de la publicacion actual
	 * */
	public static int getCurrentPublicationId(CmsObject cmsObject) throws Exception {
		TipoEdicionService tService = new TipoEdicionService();
		String defaultPublication = (String)cmsObject.getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingspublication");
		
		TipoEdicion currentPublication = null;
		if (defaultPublication != null && !defaultPublication.equals("") && !defaultPublication.trim().toLowerCase().equals("null")) {
			currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(defaultPublication) );
		} else {
			try {
				currentPublication = tService.obtenerTipoEdicion(cmsObject,cmsObject.getRequestContext().getUri());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			
			if (currentPublication==null) {
				String siteName = openCmsService.getSiteName(cmsObject.getRequestContext().getSiteRoot());
				try {
					currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return currentPublication.getId();
	}
	
	/*
	 * Se agrega para entregar  la publicacion actual si es que la publicacion seteada del usuario no contiene 
	 * */
	public static TipoEdicion getCurrentPublication(CmsObject cmsObject) throws Exception {
		String defaultPublication = (String)cmsObject.getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingspublication");
		TipoEdicionService tService = new TipoEdicionService();

		TipoEdicion currentPublication = null;
		//Si la publicacion esta dentro del sitio retorna la misma.
		if (defaultPublication != null && !defaultPublication.equals("") && !defaultPublication.trim().toLowerCase().equals("null")) {
			currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(defaultPublication) );
			String siteName = cmsObject.getRequestContext().getSiteRoot();
			if (siteName.indexOf(currentPublication.getProyecto()) != -1){
				return currentPublication;
			}
		} 
		//Si no tiene publicacion seteada o la misma no se encuentra en el sitio 
		try {
			currentPublication = tService.obtenerTipoEdicion(cmsObject,cmsObject.getRequestContext().getUri());
		} catch (Exception e1) {
			LOG.error(e1);
		}			
		
		if (currentPublication==null) {
			String siteName = openCmsService.getSiteName(cmsObject.getRequestContext().getSiteRoot());
			try {
				currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
			} catch (Exception e) {
				LOG.error(e);
			}
		}
		return currentPublication;
	}



	public static TipoEdicion getCurrentPublicationWithoutSettings(	CmsObject cmsObject) {
		TipoEdicionService tService = new TipoEdicionService();

		TipoEdicion currentPublication = null;
		
		//Si no tiene publicacion seteada o la misma no se encuentra en el sitio 
		try {
			currentPublication = tService.obtenerTipoEdicion(cmsObject,cmsObject.getRequestContext().getUri());
		} catch (Exception e1) {
			LOG.error(e1);
		}			
		
		if (currentPublication==null) {
			String siteName = openCmsService.getSiteName(cmsObject.getRequestContext().getSiteRoot());
			try {
				currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
			} catch (Exception e) {
				LOG.error(e);
			}
		}
		return currentPublication;
	}
	
}
