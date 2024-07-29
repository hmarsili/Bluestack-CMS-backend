package com.tfsla.rankUsers.action;

import java.util.Iterator;
import java.util.Locale;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.file.CmsObject;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.rankUsers.service.RankService;
import com.tfsla.utils.TfsAdminUserProvider;

public class DeleteUserCounterOnPublishEvent extends A_UserPublishRanks implements I_CmsEventListener {

	public void cmsEvent(CmsEvent event) {
		// TODO Auto-generated method stub
		
		String siteRoot =null;
		CmsObject cms = null;
		try {
			int noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();

			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {
				
				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
				
				CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
				cms = OpenCms.initCmsObject(_cmsObject);

				siteRoot = cms.getRequestContext().getSiteRoot();

				cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

				
				siteRoot = cms.getRequestContext().getSiteRoot();


				for (Iterator it = pubList.getFileList().iterator();it.hasNext();)
				{
					CmsResource resource = (CmsResource)it.next();
					
					if (resource.getTypeId()==noticiaType && resource.getState()==CmsResource.STATE_DELETED)
					{
						CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
						
						if (site!=null && cms.getRequestContext()!=null) {

							cms.getRequestContext().setSiteRoot(site.getSiteRoot());
							
							String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
							int tEd = getPublicationID(cms, resource, siteName);

							CmsUser user = getAutor(resource,cms,siteName,tEd);
							if (user!=null) {
								RankService rService = new RankService();
							
								String state = getState(cms,resource);
								
								String isRanked = getPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKED);
								String rankedPositive = getPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE);
								
								if (isRanked==null)
									rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, -1);
								else {
									if (rankedPositive!=null && rankedPositive.equals("true")) {
										rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, -1);										
									}
									//else if (rankedPositive!=null && rankedPositive.equals("false")) {
									//	rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, 1);										
									//} 
								}
							}
						}
					}
					
				}
			}
		
		} catch (CmsLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (cms!=null)
				cms.getRequestContext().setSiteRoot(siteRoot);
			
		}


	}
	
}
