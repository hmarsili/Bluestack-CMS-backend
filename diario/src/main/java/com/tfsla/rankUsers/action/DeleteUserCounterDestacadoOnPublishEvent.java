package com.tfsla.rankUsers.action;

import java.util.Iterator;
import java.util.Locale;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
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

import com.tfsla.rankUsers.service.RankService;
import com.tfsla.utils.TfsAdminUserProvider;

public class DeleteUserCounterDestacadoOnPublishEvent implements I_CmsEventListener {

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

				for (Iterator<CmsResource> it = (Iterator<CmsResource>)pubList.getFileList().iterator();it.hasNext();)
				{
					CmsResource resource = it.next();
					
					if (resource.getState()==CmsResource.STATE_DELETED && resource.getTypeId()==noticiaType)
					{
						CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
						
						if (site!=null && cms.getRequestContext()!=null) {

							CmsFile file;
							try {
								file = cms.readFile(resource);
	
								CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
		
								if (checkDestacado(resource,applyCount(content,cms),cms)) {
									RankService rService = new RankService();
									rService.addUserHit(getAutor(content,cms), cms, RankService.COUNTER_CUSTOM1, -1);

								}
							} catch (CmsException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
	
	public boolean applyCount(CmsXmlContent content, CmsObject cms)
	{
		
		I_CmsXmlContentValue contentValue = null;
		int i=1;
		String xmlNodeName = "Categorias[" + i + "]";
		contentValue = content.getValue(xmlNodeName,Locale.ENGLISH);
		while (contentValue!=null)
		{
			String categoria = contentValue.getStringValue(cms);
			if (categoria.indexOf("/destacado")>0)
				return true;				
				
			i++;;
			xmlNodeName = "Categorias[" + i + "]";
			contentValue = content.getValue(xmlNodeName,Locale.ENGLISH);
		}
		

		return false;
	}
	
	public boolean checkDestacado(CmsResource resource, boolean esDestacada, CmsObject cms)
	{
		try {
			if (!esDestacada)
				return false;
			
			CmsProperty prop = cms.readPropertyObject(resource, "esDestacada", false);
			
			if (prop==null)
				return true;
			if (prop.getValue()==null)
				return true;
			
			if (prop.getValue().equals("true"))
				return false;
			
			return true;
		} catch (CmsException e) {
			return false;
		}
		
	}
	
	public CmsUser getAutor(CmsXmlContent content, CmsObject cms) throws CmsException
	{

		I_CmsXmlContentValue contentValue = content.getValue("autor/internalUser",Locale.ENGLISH);
		CmsUser user = null;
		if (contentValue!=null)
		{
			String autor = contentValue.getStringValue(cms);
			if (autor!=null && autor.trim().length()>0)
			{
				user = cms.readUser(autor);
			
				if (user==null)
					user = cms.readUser(new CmsUUID(autor));
			}
		}
		if (user==null)
			user = cms.readUser(content.getFile().getUserCreated());

		return user;
	}}
