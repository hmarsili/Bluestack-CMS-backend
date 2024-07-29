package com.tfsla.opencmsdev;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
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
import com.tfsla.diario.twitter.BitlyService;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.TfsAdminUserProvider;

public class ShotUrlLoader implements I_CmsEventListener  {

	protected static final Log LOG = CmsLog.getLog(ShotUrlLoader.class);
	
	public void cmsEvent(CmsEvent event) {
		
		int noticiaType;
		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {
				//CmsObject cmsObject = TfsContext.getInstance().getCmsObject();
				
				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
		
				for (Iterator it = pubList.getFileList().iterator();it.hasNext();)
				{
					CmsResource resource = (CmsResource)it.next();
					if (resource.getTypeId()==noticiaType)
					{
						try {
							CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
							CmsObject cmsObject = OpenCms.initCmsObject(_cmsObject);
							
							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
							cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
							
							String url = CmsResourceUtils.getLink(resource);
							
							
							TipoEdicionService tEdicionService = new TipoEdicionService();
							TipoEdicion tEdicion = tEdicionService.obtenerTipoEdicion(cmsObject, url);

							
							LOG.debug("Analizando codigo bitly de noticia" + url);
							
							if (BitlyService.getInstance(site.getSiteRoot(),"" + tEdicion.getId()).isEnabled()) {
								CmsProperty bitlyUrl = cmsObject.readPropertyObject(resource, "bitlyUrl", false);
								if (bitlyUrl==null || bitlyUrl.getValue()==null || bitlyUrl.getValue().equals(""))
								{
									LOG.debug("Codigo bitly inexistente. agregando codigo a la noticia " + url);
									String shortUrl = BitlyService.getInstance(site.getSiteRoot(),"" + tEdicion.getId()).getShortenUrl(cmsObject, resource);
									CmsResourceUtils.forceLockResource(cmsObject, url);
									cmsObject.writePropertyObject(url, new CmsProperty("bitlyUrl",shortUrl,shortUrl));
									CmsResourceUtils.unlockResource(cmsObject, url, false);
									LOG.debug("Codigo bitly a agregar " + shortUrl + " a la noticia " + url);
	
								}
							}
							
						} catch (CmsException e) {
							CmsLog
							.getLog(this)
							.error(
									"Hubo problemas creando la url corta",
									e);
						} catch (Exception e) {
							CmsLog
							.getLog(this)
							.error(
									"Hubo problemas creando la url corta",
									e);
						}
						
					}
				}
				

			}
		} catch (CmsLoaderException e) {
			CmsLog
			.getLog(this)
			.error(
					"Hubo problemas publicando la nota en twitter",
					e);
		}
		
	}

}

