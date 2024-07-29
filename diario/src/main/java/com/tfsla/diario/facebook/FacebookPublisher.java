package com.tfsla.diario.facebook;

import java.util.Iterator;

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
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;

import com.tfsla.utils.CmsObjectUtils;


public class FacebookPublisher  implements I_CmsEventListener  {

	public void cmsEvent(CmsEvent event) {
		int noticiaType;
		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {
				
				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
			
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
					cmsObject = CmsObjectUtils.loginAsAdmin();		
				}
				
				if (cmsObject != null) {
					for (Iterator it = pubList.getFileList().iterator();it.hasNext();) {
						CmsResource resource = (CmsResource)it.next();
						if (resource.getTypeId()==noticiaType) {
							try {
								CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
								if (site==null)
									continue;
	
								cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
	
								CmsProperty facebookPublish = cmsObject.readPropertyObject(resource, "facebookPublish", false);
								if (facebookPublish.getValue()!=null && facebookPublish.getValue().equals("true")) {
									CmsLog.getLog(this).info("Facebook - Publicacion en perfil del recurso: "+resource.getName());
									try {
										FacebookService.getInstance().publishNews(resource, cmsObject);
									} catch (Exception ex) {
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
													cmsObject.getRequestContext().currentUser().getName());
										
										cmsObject = CmsObjectUtils.loginAsAdmin();
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											FacebookService.getInstance().publishNews(resource, cmsObject);
										}	
									}
								}
								
								CmsProperty facebookPagePublish = cmsObject.readPropertyObject(resource, "facebookPagePublish", false);
							
								if (facebookPagePublish.getValue()!=null && facebookPagePublish.getValue().equals("true")) {
									CmsLog.getLog(this).info("Facebook - Publicacion en Fan Page del recurso: "+resource.getName());
									try {
										FacebookPageService.getInstance().publishPageNews(resource, cmsObject);
									} catch (CmsSecurityException ex){
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
												cmsObject.getRequestContext().currentUser().getName());
									
										cmsObject = CmsObjectUtils.loginAsAdmin();
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											FacebookPageService.getInstance().publishPageNews(resource, cmsObject);
										}
									}
								}
								
							} catch (Exception e) {
								CmsLog.getLog(this).error("Hubo problemas publicando la nota en facebook",e);
							}
						}
					}
				}
			}
		} catch (CmsLoaderException e) {
			CmsLog.getLog(this).error("Hubo problemas publicando la nota en facebook",e);
		}
	}


}
