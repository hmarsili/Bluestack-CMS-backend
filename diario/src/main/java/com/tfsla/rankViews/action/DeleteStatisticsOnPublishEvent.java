package com.tfsla.rankViews.action;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.file.CmsObject;

import com.tfsla.rankViews.service.RankService;
import com.tfsla.utils.TfsAdminUserProvider;

public class DeleteStatisticsOnPublishEvent implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(DeleteStatisticsOnPublishEvent.class);

	public void cmsEvent(CmsEvent event) {
		// TODO Auto-generated method stub
		
		String siteRoot =null;
		CmsObject cms = null;
		try {
			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {
				
				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
				
				CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
				cms = OpenCms.initCmsObject(_cmsObject);

				siteRoot = cms.getRequestContext().getSiteRoot();

				cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

				
				siteRoot = cms.getRequestContext().getSiteRoot();


				for (Iterator<CmsResource> it = (Iterator<CmsResource>)pubList.getFileList().iterator();it.hasNext();)
				{
					CmsResource resource = it.next();
					
					if (resource.getState()==CmsResource.STATE_DELETED)
					{
						CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
						
						if (site!=null && cms.getRequestContext()!=null) {

							cms.getRequestContext().setSiteRoot(site.getSiteRoot());


							RankService rService = new RankService();
							LOG.debug("Enviando a servidor de estadisticas eliminacion de recurso " + resource.getRootPath());

							rService.removeResourceFromStatistics(resource, cms);
						}
					}
					else if (resource.getState()==CmsResource.STATE_NEW || resource.getState()==CmsResource.STATE_CHANGED){
						CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
						
						if (site!=null && cms.getRequestContext()!=null) {

							cms.getRequestContext().setSiteRoot(site.getSiteRoot());

							RankService rService = new RankService();
							
							LOG.debug("Enviando a servidor de estadisticas tags de recurso " + resource.getRootPath());
							rService.putTags(resource, cms);
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
