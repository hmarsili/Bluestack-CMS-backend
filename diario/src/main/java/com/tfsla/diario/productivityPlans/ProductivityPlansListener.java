package com.tfsla.diario.productivityPlans;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.FreshnessService;
import com.tfsla.diario.ediciones.services.ProductivityPlanAWS;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.freshness.model.Freshness;
import com.tfsla.utils.CmsObjectUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ProductivityPlansListener implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(ProductivityPlansListener.class);

	static private int noticiaType = -1;

	public void cmsEvent(CmsEvent event) {
		switch (event.getType()) {

		case I_CmsEventListener.EVENT_PUBLISH_PROJECT:

			CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
			CmsResource resource=null;

			if (pubList!=null) {
				CmsObject cmsObject;
				for (Iterator<CmsResource> it = pubList.getFileList().iterator();it.hasNext();) {

					resource = it.next();

					if (resource.getTypeId()==getNoticiaType() ) {
	
						try {
							cmsObject = getCmsObject(event);
	
							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							if (site==null)
								continue;
							cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
	
							TipoEdicionService tService = new TipoEdicionService();
							TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, cmsObject.getSitePath(resource));
	
							String siteName = site.getSiteRoot();
							String publication = "" + tEdicion.getId();
	
							
							JSONObject jsonAuth =  new JSONObject();
							jsonAuth.put("siteName",siteName);
							jsonAuth.put("publication",publication);
							
							JSONObject jsonreq =  new JSONObject();
							jsonreq.put("authentication",jsonAuth);
							
							ProductivityPlanAWS ppAWS = new ProductivityPlanAWS(jsonreq);
	
	
							String newsPath = cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath());
	
							if (ppAWS.isActiveModule()) {
								
								CmsFile file = cmsObject.readFile(newsPath);
								I_CmsXmlDocument m_content = CmsXmlContentFactory.unmarshal(cmsObject, file);
	
								// se procesan todos los usuairos de la nota (firmantes y creadores) 
								// si el usuario no pertence a ningun plan se manda al general.
								// Si el usuario esta en un plan pero no coincide con el tipo. se manda?.
								// Ejemlo: tengo el usuario vtarletta en un plan como creador. Pero en realidad esta firmando una noticia de vpod
								CmsUUID userIDCreation = resource.getUserCreated();
								CmsUser cmsUserCreation = cmsObject.readUser(userIDCreation);
	
								JSONArray usersToNew = new JSONArray();
								String userCreation = cmsUserCreation.getName();
								int authorsCount = m_content.getIndexCount("autor", cmsObject.getRequestContext().getLocale());
	
								String elementName = "autor[1]/internalUser[1]";
								String AuthorName = m_content.getStringValue(cmsObject, elementName, cmsObject.getRequestContext().getLocale());
	
								// el usuairo que firma es internaluser y es el mismo que creo la noticia.
								if(authorsCount == 1 && AuthorName.equals(userCreation))
									usersToNew.add("same_"+userCreation);
								
								// el usuairo que firma es internaluser. Se agrega el creado y el internaluser
								if(authorsCount == 1 && !AuthorName.equals("") && !AuthorName.equals(userCreation)) { //tengo un user que crea y un user que firma
									usersToNew.add("signing_"+AuthorName);
									usersToNew.add(userCreation);
								}
								
								// el usuario que firma es un usuario manual, se debe agregar el usuario creador.
								if(authorsCount == 1 && AuthorName.equals("") && !AuthorName.equals(userCreation)) { //tengo un user que crea y un user que firma
									usersToNew.add(userCreation);
								}

	
								if (authorsCount > 1) {
									LOG.debug("se fue por aca");
									for (int i=1; i<=authorsCount; i++){
										String internalUser = "autor["+i+"]/internalUser[1]";
										String AuthorNameSig = m_content.getStringValue(cmsObject, internalUser, cmsObject.getRequestContext().getLocale());
	
										if (!AuthorNameSig.equals("") && !usersToNew.contains(AuthorNameSig) &&  !AuthorNameSig.equals(userCreation))
											usersToNew.add("signing_"+AuthorNameSig);
	
										if (!AuthorNameSig.equals("") && !usersToNew.contains(AuthorNameSig) && AuthorNameSig.equals(userCreation))
											usersToNew.add("same_"+AuthorNameSig);
									}
	
									if ( !usersToNew.contains(userCreation) && !usersToNew.contains("same_"+userCreation))
										usersToNew.add(userCreation);					                	
	
								}
	
								boolean isFirstPublish = true;
								String estado = cmsObject.readPropertyObject(newsPath, "state", false).getValue("");
								String firstPublishDateGMTProp = cmsObject.readPropertyObject(newsPath, "firstPublishDateGMT", false).getValue("");;
	
								Calendar c = Calendar.getInstance();
								c.setTimeZone(TimeZone.getTimeZone("GMT"));
	
								if (estado.equals("publicada")) {
									// validamos si es republicacion de la noticia ó solo un cambio. Si es republicacion debe ser isFirstPublish = true.
									CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	
									int hoursRepublication = Integer.parseInt(config.getParam(siteName, publication, "productivityPlans", "hoursRepublication"));
									Calendar maxAllowedRepublicationDate = Calendar.getInstance();
									maxAllowedRepublicationDate.setTime(new Date(Long.parseLong(firstPublishDateGMTProp)*1000));
									maxAllowedRepublicationDate.setTimeZone(TimeZone.getTimeZone("GMT"));
									maxAllowedRepublicationDate.add(maxAllowedRepublicationDate.HOUR, hoursRepublication);
	
									if(c.getTime().after(maxAllowedRepublicationDate.getTime())) {
										isFirstPublish = false;
									}           
								} 
	
								JSONObject jsonFirstPub = new JSONObject();
								jsonFirstPub.put("firstPublishDate", firstPublishDateGMTProp);
								jsonFirstPub.put("isFirstPublish", isFirstPublish);
		
								String complianceData = cmsObject.readPropertyObject(newsPath, "complianceData", false).getValue("");
	
								LOG.debug("se va para AWS  usersToNew: " + usersToNew +" newsPath: " + newsPath + " complianceData: " + complianceData );
	
								ppAWS.processUsersPlans(newsPath, usersToNew, jsonFirstPub, !pubList.isDirectPublish(), complianceData, cmsObject);
							}
	
							// se valida si la noticia tiene una frescura de tipo de recurrencia. 
							// si la frescura de la nota, es posterior a la fecha de publicacion, 
							// se busca la proxima ejecucion desde la fecha de publciacion y se actualizan la frescura. 
		
							FreshnessService freshService = new FreshnessService();

							Freshness freshnessExiting = freshService.getFreshness(Integer.parseInt(publication), siteName, newsPath);
							if (freshnessExiting.getSiteName() != null && freshnessExiting.getType().equals("RECURRENCE")) {
								Date freshnessDate = new Date (freshnessExiting.getDate());
								Date today = new Date(); 

								if(freshnessDate.before(today)){
	
									// actualizar fecha, segun recurrencia hasta que sea mayor a hoy. 
									Date newDateToFreshness = freshnessDate; 
									newDateToFreshness = new Date (freshService.setDateFreshness(freshnessExiting.getDate(), freshnessExiting.getRecurrece()));
									while (newDateToFreshness.compareTo(today) <= 0) {
										newDateToFreshness = new Date (freshService.setDateFreshness(newDateToFreshness.getTime(), freshnessExiting.getRecurrece()));
										freshnessExiting.setDate(newDateToFreshness.getTime());
									}
	
	
									freshService.deleteFreshness(Integer.parseInt(publication), siteName, newsPath);
									freshService.createFreshness(freshnessExiting);
	
								}
							}
	
						} catch (CmsException e) {
							LOG.error("Error al intentar procesar los usuarios de las notas para enviar a AWS." + e.getMessage(),e);
						} catch (Exception e) {
							LOG.error("Error al intentar procesar los usuarios de las notas para enviar a AWS." + e.getMessage(),e);
						} 
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
				LOG.debug("entro al try para bscar el noticiaType " + noticiaType);
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
			cmsObject = CmsObjectUtils.loginAsAdmin();		
		}

		return cmsObject;
	}

}
