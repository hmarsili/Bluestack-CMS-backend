	package com.tfsla.cdnIntegration.service;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.utils.TfsAdminUserProvider;


public class PurgeCacheCdnListener implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(PurgeCacheCdnListener.class);

	static private int noticiaType = -1;
	private String module = "contentDeliveryNetwork";

	
	public void cmsEvent(CmsEvent event) {
		switch (event.getType()) {
			
			case I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT:
				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
				LOG.debug("CDN - Elementos a publicar - tiene elementos relacionados: " + (pubList.getFileList()!=null?  String.valueOf(pubList.getFileList().size()):"0"));
				CmsResource resource=null;
				CmsResource previousResource=null;
				
				CmsObject cmsObject;
				for (Iterator<CmsResource> it = pubList.getFileList().iterator();it.hasNext();) {
					resource = it.next();
					if (resource.getTypeId()==getNoticiaType() 
							&& (resource.getState() == CmsResourceState.STATE_CHANGED 
								|| resource.getState() == CmsResourceState.STATE_DELETED) &&
							(previousResource== null || (previousResource != null && !previousResource.equals(resource)) )) {
						String sitio = "";
						String publicacion = "";
						try {

							CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
							cmsObject = OpenCms.initCmsObject(_cmsObject);

							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							if (site==null)
								continue;
							cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
	
							TipoEdicionService tService = new TipoEdicionService();
							TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, cmsObject.getSitePath(resource));
							
							sitio = site.getSiteRoot();
							publicacion = "" + tEdicion.getId();
							
							
							LOG.debug("Agregando noticia " + resource.getRootPath() + " a purga de cdn de publicacion " + publicacion);							
							if (CmsMedios.getInstance().getCmsParaMediosConfiguration().getModule(sitio, publicacion, module) != null
									&& CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(sitio, publicacion, module, "isActive", false)) {
								CdnManager.getInstance(sitio,publicacion).addResource(cmsObject, resource);
							}
							previousResource = resource;
							
						} catch (CmsException e) {
							LOG.error("Error al intentar agregar noticia a cola de purga de cdn (" + sitio + " | " + publicacion + ")",e);
						} catch (Exception e) {
							LOG.error("Error al intentar agregar noticia a cola de purga de cdn  (" + sitio + " | " + publicacion + ")",e);
						} 
					}
				}
				break;
		}
	}


	private int getNoticiaType()
	{
		if (noticiaType==-1) {

			try {
				noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			} catch (CmsLoaderException e) {
				LOG.error("Error al intentar obtener el identificador de la noticia",e);
			}

		}
		return noticiaType;
	}
}
