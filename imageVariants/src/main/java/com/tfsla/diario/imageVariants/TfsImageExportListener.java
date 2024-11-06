package com.tfsla.diario.imageVariants;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.Messages;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;

import com.tfsla.utils.CmsObjectUtils;


public class TfsImageExportListener  implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(TfsImageExportListener.class);
	
	static private int noticiaType = -1;
	
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
	
	private CmsObject getCmsObject(CmsEvent event) {
		CmsObject cmsObject=null;
		try {
			CmsObject cmsObjectToClone = (CmsObject)event.getData().get(I_CmsEventListener.KEY_CMS_OBJECT);
			cmsObject = CmsObjectUtils.getClone(cmsObjectToClone);
			if (cmsObject != null)
				cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
		} catch (Exception ex){
			CmsLog.getLog(this).error("Error al intentar obtener el cmsObject del evento",ex);
		}
		if (cmsObject == null) {
			CmsLog.getLog(this).info("no se encuentro cmsObject para evento de publicacion se utiliza el del admin.");
			if (event.getData() != null) {
                Iterator i = event.getData().keySet().iterator();
                while (i.hasNext()) {
                    String key = (String)i.next();
                    Object value = event.getData().get(key);
                    CmsLog.getLog(this).debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_EVENT_VALUE_3,
                        key,
                        value,
                        event.toString()));
                }
            } else {
            	CmsLog.getLog(this).debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_NO_EVENT_VALUE_1, event.toString()));
            }
			cmsObject = CmsObjectUtils.loginAsAdmin();		
		}

		return cmsObject;
	}
	
	@Override
	public void cmsEvent(CmsEvent event) {
		
		CmsResource resource = null;
		
		ImageFinder imgFinder = new ImageFinder();
		try {
		
			switch (event.getType()) {
				case I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT:
					CmsLog.getLog(this).info("Verificando la necesidad de exportar imagenes de los recursos publicados");

					CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
	
					CmsObject cmsObject = getCmsObject(event);
					
					for (Iterator<CmsResource> it = pubList.getFileList().iterator();it.hasNext();)
					{
						resource = it.next();
						
						CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
						if (site==null)
							continue;
						
						cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
						
						if (resource.getTypeId()==getNoticiaType()) {
							CmsLog.getLog(this).info("Verificando la necesidad de exportar imagenes de la noticia " + resource.getRootPath());

							imgFinder.publishImages(resource, cmsObject, false);
						}
					}
					break;
			}
		} catch (CmsException e) {
			LOG.error("Error al intentar generar variantes de las imagenes de noticia", e);
		} catch (Exception e) {
			LOG.error("Error al intentar generar variantes de las imagenes de noticia", e);
		}		
	}
}
