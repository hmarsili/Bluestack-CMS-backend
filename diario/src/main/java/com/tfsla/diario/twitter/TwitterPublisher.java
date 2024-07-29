package com.tfsla.diario.twitter;

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

import twitter4j.TwitterException;

import com.tfsla.utils.CmsObjectUtils;

public class TwitterPublisher  implements I_CmsEventListener  {

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
								
								CmsProperty twitterPublish = cmsObject.readPropertyObject(resource, "twitterPublish", false);
								if (twitterPublish!=null && twitterPublish.getValue()!=null && twitterPublish.getValue().equals("true")) {
									try {
										TwitterService.getInstance().publishNews(resource, cmsObject);
									} catch (CmsSecurityException ex) {
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
												cmsObject.getRequestContext().currentUser().getName());
									
										cmsObject = CmsObjectUtils.loginAsAdmin();	
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											TwitterService.getInstance().publishNews(resource, cmsObject);
										}
									}
								}
								
							} catch (CmsException e) {
								CmsLog.getLog(this).error("Hubo problemas publicando la nota en twitter",e);
							} catch (TwitterException e) {
								CmsLog.getLog(this).error("Hubo problemas publicando la nota en twitter",e);
							} catch (Exception e) {
								CmsLog.getLog(this).error("Hubo problemas publicando la nota en twitter",e);
							}
							
						}
					}
				} 
			}
		} catch (CmsLoaderException e) {
			CmsLog.getLog(this).error("Hubo problemas publicando la nota en twitter",e);
		}
		
	}

}
