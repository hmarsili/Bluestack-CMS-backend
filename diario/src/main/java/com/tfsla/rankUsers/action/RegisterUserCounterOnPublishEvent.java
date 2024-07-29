package com.tfsla.rankUsers.action;

import java.util.Iterator;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.file.CmsObject;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.rankUsers.service.RankService;
import com.tfsla.utils.TfsAdminUserProvider;

public class RegisterUserCounterOnPublishEvent extends A_UserPublishRanks implements I_CmsEventListener {

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


				cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

				
				siteRoot = cms.getRequestContext().getSiteRoot();


				for (Iterator it = pubList.getFileList().iterator();it.hasNext();)
				{
					CmsResource resource = (CmsResource)it.next();
					
					//if (resource.getTypeId()==noticiaType && resource.getState()==CmsResource.STATE_NEW)
					if (resource.getTypeId()==noticiaType)
					{
						CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
						
						if (site!=null && cms.getRequestContext()!=null) {

							cms.getRequestContext().setSiteRoot(site.getSiteRoot());

							String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
							int tEd = getPublicationID(cms, resource, siteName);

							RankService rService = new RankService();
							CmsUser user = getAutor(resource,cms,siteName,tEd);
							if (user!=null) {

								//Si la nota no es nueva y no tiene el indicador "alreadyRanked" entonces 
								// es que esta desde antes del cambio de solo dar ranking o quitar si esta aprobada o rechazada.
								
								String state = getState(cms,resource);
								
								String isRanked = getPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKED);
								String rankedPositive = getPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE);
						
								LOG.debug("noticia " + resource.getRootPath() + " - state: " + state + " - isRanked: " + (isRanked == null ? "null" : isRanked )  + " - isRanked: " + (rankedPositive == null ? "null" : rankedPositive ));
								if (isRanked==null) {
									// no esta la property y por lo tanto hay que crearla.
									createPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKED, "true");									
									if (state.equals(PlanillaFormConstants.PUBLICADA_VALUE)) {
										createPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE, "true");
										//Si la noticia es nueva es que es la primera vez que se publica. sino es preexistente y no la rankeo
										if (resource.getState()==CmsResource.STATE_NEW) {
											rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, 1);
											LOG.debug("noticia "  + resource.getRootPath() + " - user: " + user.getEmail() + " > + ranking");
											
										}
										
									}
									else if (state.equals(PlanillaFormConstants.RECHAZADA_VALUE)) {
										createPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE, "");
										if (resource.getState()!=CmsResource.STATE_NEW) {
											rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, -1);
											LOG.debug("noticia "  + resource.getRootPath() + " - user: " + user.getEmail() + " > - ranking");
										}
									}
									else {
										createPropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE, "");
										if (resource.getState()!=CmsResource.STATE_NEW) {
											rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, -1);
											LOG.debug("noticia "  + resource.getRootPath() + " - user: " + user.getEmail() + " > - ranking");
										}
									}
								}
								else { 
									// ya entro en el circuito de rankear al usuario. por lo tanto es noticia post cambio.
									if (state.equals(PlanillaFormConstants.PUBLICADA_VALUE)) {
										if (rankedPositive==null || rankedPositive.equals("false") || rankedPositive.equals("") ) {
											rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, 1);
											updatePropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE, "true");
											LOG.debug("noticia "  + resource.getRootPath() + " - user: " + user.getEmail() + " > + ranking");
										}
									}
									else if (state.equals(PlanillaFormConstants.RECHAZADA_VALUE)) {
										if (rankedPositive!=null && rankedPositive.equals("true")) {
											rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, -1);
											updatePropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE, "false");
											LOG.debug("noticia "  + resource.getRootPath() + " rechazada - user: " + user.getEmail() + " > - ranking");
										}
									}
									else if (state.equals(PlanillaFormConstants.PENDIENTE_MODERACION_VALUE)) {
										if (rankedPositive!=null && rankedPositive.equals("true")) {
											rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, -1);
											updatePropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE, "");
											LOG.debug("noticia "  + resource.getRootPath() + " a moderar previamente aprobada - user: " + user.getEmail() + " > - ranking");											
										}
										else if (rankedPositive==null || rankedPositive.equals("false")) {
											//rService.addUserHit(user, cms, RankService.COUNTER_NOTASPUBLICADAS, 1);
											updatePropertyInShadowMode(cms, resource, PROPERTYNAME_RANKPOSITIVE, "");
											LOG.debug("noticia "  + resource.getRootPath() + " - user: " + user.getEmail() + " > + ranking");
										}

									}

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
