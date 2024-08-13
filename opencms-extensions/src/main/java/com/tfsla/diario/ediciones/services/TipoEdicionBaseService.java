package com.tfsla.diario.ediciones.services;

//import java.util.Random;
//import org.opencms.configuration.CmsMediosInit;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;

/**
 * @author Victor Podberezski
 *
 */
public class TipoEdicionBaseService extends baseService {

	

	/**
	 * Obtiene una publicacion en base a su indentificador.
	 * @param id
	 * @return
	 */
	public TipoEdicion obtenerTipoEdicion(int id)
	{
		TipoEdicion tipoEdicion = TipoEdicionCache.getInstance().getTipoEdicion(id);
		if (tipoEdicion!=null)
			return tipoEdicion;
		
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			tipoEdicion = eDAO.getTipoEdicion(id);
			
			if (tipoEdicion!=null)
				TipoEdicionCache.getInstance().putTipoEdicion(tipoEdicion);
			
			hasError = false;

			return tipoEdicion;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Obtiene una publicacion en base a su nombre.
	 * @param id
	 * @return
	 */
	public TipoEdicion obtenerTipoEdicion(String nombre,String proyecto)
	{
		TipoEdicion tipoEdicion = TipoEdicionCache.getInstance().getTipoEdicion(nombre,proyecto);
		if (tipoEdicion!=null)
			return tipoEdicion;

		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			tipoEdicion = eDAO.getTipoEdicion(proyecto, nombre);
			if (tipoEdicion!=null)
				TipoEdicionCache.getInstance().putTipoEdicion(tipoEdicion);
			
			hasError = false;

			return tipoEdicion;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public TipoEdicion obtenerTipoEdicion(String nombre)
	{
		TipoEdicion tipoEdicion = TipoEdicionCache.getInstance().getTipoEdicion(nombre,"");
		if (tipoEdicion!=null)
			return tipoEdicion;

		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			tipoEdicion = eDAO.getTipoEdicion(nombre);
			if (tipoEdicion!=null)
				TipoEdicionCache.getInstance().putTipoEdicion(tipoEdicion);
			
			hasError = false;

			return tipoEdicion;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Obtiena en base al directorio la publicacion a la que pertence.
	 * @param cms
	 * @param path
	 * @return TipoEdicion
	 * @throws Exception
	 */
	public TipoEdicion obtenerTipoEdicion(CmsObject cms, String path) throws Exception
	{
/*		
		if (cms.getRequestContext().currentProject().isOnlineProject() &&  !OpenCms.getSiteManager().getCurrentSite(cms).getUrl().equals("/") && !cms.getRequestContext().getUri().startsWith("/system/")) {

			boolean restrictive = CmsMediosInit.getInstance().restrictiveMode(cms);
			long views = CmsMediosInit.getInstance().getViews(cms);
			long maxViews = CmsMediosInit.getInstance().getPermViews(cms);
			
			Random rnd = new Random();
			int rNro = rnd.nextInt(10000);
			int license = rNro>=1000 ? 0 : CmsMediosInit.getInstance().checkLicense(cms);
			
			if (license!=0 || restrictive && maxViews<views || views==-1L)
				return null;
		}
*/
		String siteRoot = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		String fullPath = siteRoot + path;
		TipoEdicion tipoEdicion = TipoEdicionCache.getInstance().getTipoEdicionByPath(fullPath);
		if (tipoEdicion!=null)
			return tipoEdicion;
		
		String siteName = OpenCmsBaseService.getCurrentSite(cms);
		
    	TipoEdicion tEdicion = null;
    	TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
		
		try {
			String publicationName = path;

			publicationName = publicationName.replaceFirst("/", "");
			publicationName = publicationName.substring(0, publicationName.indexOf("/"));

	    	// fijarse si el diario es el online.
	    	if (publicationName.equals("contenidos"))
	    		tEdicion = obtenerEdicionOnline(siteName);
	    	else {
				//Fijarse si previamente se constato que la carpeta no es una publicacion.
				if (TipoEdicionCache.getInstance().isFolderPath(publicationName)) {
					tEdicion = obtenerEdicionOnline(siteName);
					TipoEdicionCache.getInstance().putFolderPath(publicationName);
				}
				else {
					
					tEdicion = obtenerTipoEdicion(publicationName,siteName);
					if (tEdicion==null) {
						//no corresponde a una publicacion
						TipoEdicionCache.getInstance().putFolderPath(publicationName);
						tEdicion = obtenerEdicionOnline(siteName);
					}	
				}
	    	}
	    	
	    	if (tEdicion==null)
	    		tEdicion = obtenerEdicionOnline(siteName);
		}
		catch (Exception e)
		{
			tEdicion = obtenerEdicionOnline(siteName);
		}
		
		if (tipoEdicion!=null)
			TipoEdicionCache.getInstance().putTipoEdicion(tEdicion,fullPath);
    	return tEdicion;
	}
	
	public TipoEdicion obtenerEdicionOnlineRoot(String proyecto) throws Exception {
		TipoEdicion tipoEdicion = TipoEdicionCache.getInstance().getTipoEdicionPrincipal(proyecto);
		if (tipoEdicion!=null)
			return tipoEdicion;

		TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
		TipoEdicion tEdicion = tDAO.getTipoEdicionOnlineRoot(proyecto);
		
		if (tEdicion!=null)
			TipoEdicionCache.getInstance().putTipoEdicion(tEdicion);
		return tEdicion;
	}
		
	public boolean hasEdicionOnlineRoot(String proyecto) throws Exception {
		TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
		return tDAO.getTipoEdicionOnlineRoot(proyecto) != null;
	}
	
	public TipoEdicion obtenerEdicionOnline(String siteName) throws Exception {
		return obtenerEdicionOnlineRoot(siteName);
	}	
}
