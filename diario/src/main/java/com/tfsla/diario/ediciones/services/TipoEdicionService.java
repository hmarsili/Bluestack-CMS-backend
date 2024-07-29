package com.tfsla.diario.ediciones.services;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;

/**
 * @author Victor Podberezski
 *
 */
public class TipoEdicionService extends TipoEdicionBaseService {

	/**
	 * Crea una publicacion.
	 * @param tipoEdicion
	 * @param cms
	 */
	public void crearTipoEdicion(TipoEdicion tipoEdicion, CmsObject cms) {

		TipoEdicionesDAO tDAO = new TipoEdicionesDAO();

		String siteRoot = cms.getRequestContext().getSiteRoot();

		CmsResourceManager res_manager = OpenCms.getResourceManager();
		
		try {
			cms.getRequestContext().setSiteRoot("/");
			
			TipoPublicacion tipoPublicacion = TipoPublicacion.getTipoPublicacionByCode(tipoEdicion.getTipoPublicacion());
			
			//EDICION_IMPRESA
			if(tipoPublicacion.equals(TipoPublicacion.EDICION_IMPRESA)){
				cms.createResource(tipoEdicion.getBaseURL(), org.opencms.file.types.CmsResourceTypeFolder.getStaticTypeId());					
				
				cms.copyResource("/system/modules/com.tfsla.diario.ediciones/resources/baseTemplate", tipoEdicion.getBaseURL() + "baseTemplate", CmsResource.COPY_AS_NEW );
				
				int tipo_recursoXml = res_manager.getResourceType("xmlpage").getTypeId();
				String HomeName = tipoEdicion.getBaseURL() + "index.html";
				cms.createResource(HomeName, tipo_recursoXml);
				cms.writePropertyObject(HomeName, new CmsProperty("template", "/system/modules/com.tfsla.diario.ediciones/templates/index.jsp",null));
			}
			//ONLINE_ROOT
			else if(tipoPublicacion.equals(TipoPublicacion.ONLINE_ROOT))
				cms.createResource(tipoEdicion.getBaseURL(), org.opencms.file.types.CmsResourceTypeFolder.getStaticTypeId());
			//ONLINE			
			else if(tipoPublicacion.equals(TipoPublicacion.ONLINE))
				cms.copyResource("/system/modules/com.tfsla.diario.ediciones/resources/baseTemplateOnline", tipoEdicion.getBaseURL(), CmsResource.COPY_AS_NEW );

			cms.unlockResource(tipoEdicion.getBaseURL());
			
			//cms.publishResource(tipoEdicion.getBaseURL());
			OpenCms.getPublishManager().publishResource(cms, tipoEdicion.getBaseURL());   

			tDAO.insertTipoEdicion(tipoEdicion);

			hasError = false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			cms.getRequestContext().setSiteRoot(siteRoot);

		}

	}

	/**
	 * Elimina una publicacion.
	 * @param tipoEdicion
	 * @param cms
	 */
	public void borrarTipoEdicion(TipoEdicion tipoEdicion, CmsObject cms) {
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();

		String siteRoot = cms.getRequestContext().getSiteRoot();

		try {

			if (!TipoPublicacion.getTipoPublicacionByCode(tipoEdicion.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT)) {
			
				cms.getRequestContext().setSiteRoot("/");
			
				cms.lockResource(tipoEdicion.getBaseURL());

				cms.deleteResource(tipoEdicion.getBaseURL(),CmsResource.DELETE_PRESERVE_SIBLINGS );

				cms.unlockResource(tipoEdicion.getBaseURL());
				//cms.publishResource(tipoEdicion.getBaseURL());
				OpenCms.getPublishManager().publishResource(cms,tipoEdicion.getBaseURL());
			}
				
			eDAO.deleteTipoEdicion(tipoEdicion.getId());
			TipoEdicionCache.getInstance().reset();
			
			hasError = false;

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			cms.getRequestContext().setSiteRoot(siteRoot );

		}


	}

	/**
	 * Actualiza los datos de una publicacion.
	 * @param tipoEdicion
	 */
	public void actualizarTipoEdicion(TipoEdicion tipoEdicion) {
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			eDAO.updateTipoEdicion(tipoEdicion);

			TipoEdicionCache.getInstance().reset();
			hasError = false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


	/**
	 * Retorna todas las publicaciones.
	 * @return Lista de publicaciones.
	 */
	public List<TipoEdicion> obtenerTipoEdiciones()
	{
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			List<TipoEdicion> tipoEdiciones = eDAO.getTipoEdiciones();

			hasError = false;

			return tipoEdiciones;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Obtiene las publicaciones de un sitio.
	 * @param sitio
	 * @return Lista de publicaciones
	 */
	public List<TipoEdicion> obtenerTipoEdiciones(String proyecto) {
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			List<TipoEdicion> tipoEdiciones = eDAO.getTipoEdiciones(proyecto);

			hasError = false;

			return tipoEdiciones;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Determina si una publicacion existente tiene el nombre indicado.
	 * @param nombre
	 * @return True si existe ese nombre de publicacion.
	 */
	public boolean TipoEdicionExists(String nombre, String sitio) {
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			boolean exists = eDAO.TipoEdicionExists(nombre, sitio);

			hasError = false;

			return exists;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Obtiene las publicaciones impresas de un sitio.
	 * @param sitio.
	 * @return Listado de publicaciones.
	 */
	public List<TipoEdicion> obtenerTipoEdicionesImpresas(String proyecto) {
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			List<TipoEdicion> tipoEdiciones = eDAO.getTipoEdicionesImpresas(proyecto);

			hasError = false;

			return tipoEdiciones;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Establece la edicion activa de una publicacion.
	 * @param tipo
	 * @param numero
	 */
	public void establecerEdicionActiva(int tipo, int numero)
	{
		TipoEdicionesDAO eDAO = new TipoEdicionesDAO();
		try {
			eDAO.updateEdicionActiva(tipo,numero);
			TipoEdicionCache.getInstance().reset();
			hasError = false;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
