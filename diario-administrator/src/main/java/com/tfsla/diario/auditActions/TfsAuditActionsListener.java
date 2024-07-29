package com.tfsla.diario.auditActions;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.auditActions.data.TfsAuditActionDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAction;
import com.tfsla.diario.auditActions.service.hangout.HangoutNotificationSender;
import com.tfsla.diario.auditActions.service.slack.SlackNotificationSender;
import com.tfsla.diario.auditActions.service.telegram.TelegramNotificationSender;
import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.event.I_TfsEventListener;
import com.tfsla.utils.TfsAdminUserProvider;
import com.tfsla.utils.UrlLinkHelper;
import com.tfsla.webusersnewspublisher.service.NewsPublisherModerationManager;
import com.tfsla.workflow.QueryBuilder;

import org.opencms.db.CmsUserSettings;

public class TfsAuditActionsListener implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(TfsAuditActionsListener.class);
	
	private static final String BUNDLE_NAME = "com.tfsla.diario.admin.workplace";

	static private int noticiaType = -1;
	static private int encuestaType = -1;
	
	@Override
	public void cmsEvent(CmsEvent event) {
		try {
			
			TfsAuditActionDAO auditDAO = new TfsAuditActionDAO();
			TfsAuditAction action = null;
			
			CmsResource resource = null;
			List<CmsResource> recursos = null;
			
			CmsUUID userId = null;
			String userAction = null;
			CmsUser user = null;
			
			CmsObject cmsObject = OpenCms.initCmsObject(TfsAdminCmsMediosModuleAction.getAdminCmsObject());
			cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
			
			switch (event.getType()) {
			case I_CmsEventListener.EVENT_USER_MODIFIED:
				
				userId = new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_USER_ID));
				userAction = (String) event.getData().get(I_CmsEventListener.KEY_USER_ACTION);
					
				try {
						user = cmsObject.readUser(userId);
					} catch (CmsException e) {
						LOG.error("Error al intentar obtener la informacion del usuario",e);
					}

				if (userAction.equals("createUser"))
				{
					
					action = new TfsAuditAction();

					action.setActionId(TfsAuditAction.ACTION_USER_CREATED);
					action.setTimeStamp(new Date());
					action.setUserName(user.getName());

					auditDAO.insertUserAuditEvent(action);

				}
				break;
			
//			case I_CmsEventListener.EVENT_LOGIN_USER:
//					user = (CmsUser) event.getData().get("data");
					
//					if (!user.getName().equals("TFS-ADMIN")) {
//						action = new TfsAuditAction();
	
//						action.setActionId(TfsAuditAction.ACTION_USER_LOGIN);
//						action.setTimeStamp(new Date());
//						action.setUserName(user.getName());
	
//						auditDAO.insertUserAuditEvent(action);
//					}
//					break;

			case I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT:
				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);

				for (Iterator<CmsResource> it = pubList.getFileList().iterator();it.hasNext();)
				{
					resource = it.next();

					action = new TfsAuditAction();

					//Me fijo si el recurso dejo de estar en un sitio (por si borraron el sitio).
					// en ese caso no sigo auditando.
					CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
					if (site==null)
						continue;
					
					if (resource.getTypeId()==getNoticiaType() || resource.getTypeId()==getEncuestaType()) {

						extractResourceInformation(resource, cmsObject, action);
						action.setDescription(cmsObject.readPropertyObject(resource, "Title", false).getValue(""));

						CmsDbContext dbc = (CmsDbContext) event.getData().get(I_CmsEventListener.KEY_DBCONTEXT);
						action.setUserName(dbc.currentUser().getName());
						
						if (resource.getTypeId()==getNoticiaType()) {
							if (!isPost(cmsObject,resource)) {
								if (resource.getState() == CmsResourceState.STATE_DELETED) {
									action.setActionId(TfsAuditAction.ACTION_NEWS_DELETED);
								}
								else {
									action.setActionId(TfsAuditAction.ACTION_NEWS_PUBLISHED);
									if (resource.getState() == CmsResourceState.STATE_NEW) {
										broadcastNewsPublication(resource, cmsObject, action,"NEWS_PUBLISHED");		
									}						}
							}
							else {
								continue;
								//user = TfsAuditActionsListener.newsRevisionResponsable(cmsObject, resource, action.getSitio(),action.getPublicacion());
								//action.setActionId(TfsAuditAction.ACTION_POST_PUBLISHED);
							}
						}
						else
							if (resource.getState() == CmsResourceState.STATE_DELETED)
								action.setActionId(TfsAuditAction.ACTION_POLL_DELETED);
							else
								action.setActionId(TfsAuditAction.ACTION_POLL_PUBLISHED);

						
						auditDAO.insertUserAuditEvent(action);
						
					}
	
		    	}   	
			
				break;
				
		    case I_CmsEventListener.EVENT_RESOURCE_DELETED:        
				recursos = (List<CmsResource>) event.getData().get("resources");

				
				for (Iterator<CmsResource> it = recursos.iterator();it.hasNext();)
				{
					resource = (CmsResource)it.next();
					
					if (!resource.getName().startsWith("~")) {
						action = new TfsAuditAction();
	
						
						if (resource.getTypeId()==getNoticiaType() || resource.getTypeId()==getEncuestaType()) {
							
							action.setDescription(cmsObject.readPropertyObject(resource, "Title", false).getValue(""));
							if (resource.getTypeId()==getNoticiaType())
								action.setActionId(TfsAuditAction.ACTION_NEWS_DELETED);
							else
								action.setActionId(TfsAuditAction.ACTION_POLL_DELETED);
	
							extractResourceInformation(resource, cmsObject, action);
							
							action.setUserName((String)event.getData().get(I_CmsEventListener.KEY_USER_NAME));
							
							//Comentado. por el momento solo logueo aquellas noticias que se borran estando publicadas.
							//auditDAO.insertUserAuditEvent(action);
							
						}
					}

				}				
				break;
			case I_CmsEventListener.EVENT_RESOURCE_CREATED:
				resource = (CmsResource) event.getData().get("resource");
	
				action = new TfsAuditAction();
				if (!resource.getName().startsWith("~")) {
					if (resource.getTypeId()==getNoticiaType() || resource.getTypeId()==getEncuestaType()) {

						extractResourceInformation(resource, cmsObject, action);

						if (resource.getTypeId()==getNoticiaType()) {
							// chequear que no sea creado por el usuario TFS-ADMIN
							if (!cmsObject.readUser(resource.getUserCreated()).getName().equals(CmsWorkplaceAction.getInstance().getCmsAdminObject().getRequestContext().currentUser().getName()))
								action.setActionId(TfsAuditAction.ACTION_NEWS_CREATED);
							else
								action.setActionId(TfsAuditAction.ACTION_POST_CREATED);
							
							broadcastNewsPublication(resource, cmsObject, action,"NEWS_CREATED");
						}
						else
							action.setActionId(TfsAuditAction.ACTION_POLL_CREATED);
	
						
						auditDAO.insertUserAuditEvent(action);
						
					}
				}

	
				break;
			case I_CmsEventListener.EVENT_PROPERTY_MODIFIED:
				resource = (CmsResource) event.getData().get("resource");
				CmsProperty property = (CmsProperty) event.getData().get("property");
				
				action = new TfsAuditAction();

				if (property.getName().equals("state")) {

					if (resource.getName().startsWith("~")) {
						
						if (resource.getTypeId()==getNoticiaType() || resource.getTypeId()==getEncuestaType()) {
	
							action.setDescription(property.getValue());
							extractResourceInformation(resource, cmsObject, action);
							
							if (resource.getTypeId()==getNoticiaType()){
								
								action.setActionId(TfsAuditAction.ACTION_NEWS_STATUS_CHANGED);
								
								String resPath = cmsObject.getSitePath(resource).replaceFirst("~", "");
								CmsResource res = cmsObject.readResource(resPath);
								CmsProperty prop = cmsObject.readPropertyObject(res, "state", false);

								String stateProp = ""; 
								
								if(prop != null)
									stateProp = prop.getValue();
									
								if(stateProp!=null && !stateProp.equals(property.getValue()))
										broadcastNewsPublication(res, cmsObject, action,"NEWS_STATUS_CHANGED");	
								
							}else{
								action.setActionId(TfsAuditAction.ACTION_POLL_STATUS_CHANGED);
							}
							
						}
					}

				}
	
				break;

			case I_TfsEventListener.EVENT_COMMENT_REPORTED:
				Comment abusiveComment  = (Comment) event.getData().get(I_TfsEventListener.KEY_COMMENT);
				String abuseType = (String) event.getData().get(I_TfsEventListener.KEY_ABUSETYPE);
				String userName = (String) event.getData().get(I_TfsEventListener.KEY_USER_NAME);
				
				action = new TfsAuditAction();
				action.setActionId(TfsAuditAction.ACTION_COMMENT_REPORTED);
				if (cmsObject.existsResource(abusiveComment.getNoticiaURL())){
				resource = cmsObject.readResource(abusiveComment.getNoticiaURL(),CmsResourceFilter.ALL);
				}else{
					String sitenameresource=OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot()+abusiveComment.getNoticiaURL();
					sitenameresource=sitenameresource.replace("//", "/");
					resource = cmsObject.readResource(sitenameresource,CmsResourceFilter.ALL);	
				}
					
				extractResourceInformation(resource, cmsObject, action);
				
				action.setUserName(userName);
				action.setDescription(abuseType + "|" + abusiveComment.getId());
				auditDAO.insertUserAuditEvent(action);

				break;
			case I_TfsEventListener.EVENT_COMMENT_REVISION:

				Comment comment  = (Comment) event.getData().get(I_TfsEventListener.KEY_COMMENT);

				action = new TfsAuditAction();
				action.setActionId(TfsAuditAction.ACTION_COMMENT_REVISION);

				if (cmsObject.existsResource(comment.getNoticiaURL())){
					resource = cmsObject.readResource(comment.getNoticiaURL(),CmsResourceFilter.ALL);
					}else{
						String sitenameresource=OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot()+comment.getNoticiaURL();
						
						sitenameresource=sitenameresource.replace("//", "/");
						resource = cmsObject.readResource(sitenameresource,CmsResourceFilter.ALL);	
					}
				extractResourceInformation(resource, cmsObject, action);
				
				action.setUserName(CmsWorkplaceAction.getInstance().getCmsAdminObject().getRequestContext().currentUser().getName());

				auditDAO.insertUserAuditEvent(action);

				if (isPost(cmsObject,resource))
					user = postRevisionResponsable(cmsObject, action.getSitio(),action.getPublicacion());
				else
					user = newsRevisionResponsable(cmsObject, resource, action.getSitio(),action.getPublicacion());
				
				auditDAO.insertNotificationAuditEvent(action.getEventId(),action.getTimeStamp(),user.getName());
				break;

			case I_TfsEventListener.EVENT_POST_ACEPTED:
				action = new TfsAuditAction();
				action.setActionId(TfsAuditAction.ACTION_POST_ACEPTED);
				
				resource = (CmsResource)event.getData().get(I_TfsEventListener.KEY_RESOURCE);
		        userName = (String)event.getData().get(I_TfsEventListener.KEY_USER_NAME);
				
				extractResourceInformation(resource, cmsObject, action);
				
				action.setUserName(userName);

				auditDAO.insertUserAuditEvent(action);
				
				break;
			case I_TfsEventListener.EVENT_POST_REJECTED:
				action = new TfsAuditAction();
				action.setActionId(TfsAuditAction.ACTION_POST_REJECTED);
				
				resource = (CmsResource)event.getData().get(I_TfsEventListener.KEY_RESOURCE);
		        userName = (String)event.getData().get(I_TfsEventListener.KEY_USER_NAME);
				
				extractResourceInformation(resource, cmsObject, action);
				
				action.setUserName(userName);

				auditDAO.insertUserAuditEvent(action);
				
				break;
			case I_TfsEventListener.EVENT_POST_REPORTED:
				action = new TfsAuditAction();
				action.setActionId(TfsAuditAction.ACTION_POST_REPORTED);

				resource = (CmsResource)event.getData().get(I_TfsEventListener.KEY_RESOURCE);
				extractResourceInformation(resource, cmsObject, action);

				abuseType = (String) event.getData().get(I_TfsEventListener.KEY_ABUSETYPE);
				String userMessage = (String) event.getData().get(I_TfsEventListener.KEY_USERMESSAGE);

				action.setDescription(abuseType + "|" + userMessage);
				String userNameReport = (String) event.getData().get(I_TfsEventListener.KEY_USER_NAME);
				action.setUserName(userNameReport);

				auditDAO.insertUserAuditEvent(action);
				break;
			case I_TfsEventListener.EVENT_POST_REVISION:
				action = new TfsAuditAction();
				action.setActionId(TfsAuditAction.ACTION_POST_REVISION);

				resource = (CmsResource)event.getData().get(I_TfsEventListener.KEY_RESOURCE);
		        userName = (String)event.getData().get(I_TfsEventListener.KEY_USER_NAME);
				
				extractResourceInformation(resource, cmsObject, action);
				
				action.setUserName(userName);

				auditDAO.insertUserAuditEvent(action);

				List<CmsGroup> groups = cmsObject.getGroupsOfUser(userName, true);
				List<String> moderators = NewsPublisherModerationManager.getInstance(action.getSitio(),action.getPublicacion()).getGroupModerators(groups);

				for (String moderator : moderators) {
					auditDAO.insertNotificationAuditEvent(action.getEventId(),action.getTimeStamp(),moderator);
				}
				break;
				
			}
		} catch (CmsException e) {
			LOG.error("Error al intentar registrar evento en log", e);
		} catch (Exception e) {
			LOG.error("Error al intentar registrar evento en log", e);
		}



	}

	public static CmsUser newsRevisionResponsable(CmsObject cms, CmsResource resource, String site, String publication)
	{

		CmsFile file;
		try {
			file = cms.readFile(resource);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

			I_CmsXmlContentValue contentValue = content.getValue("autor/internalUser",cms.getRequestContext().getLocale());
			if (contentValue!=null)
			{
				String autor = contentValue.getStringValue(cms);
				if (autor!=null && autor.trim().length()>0)
				{
					
					CmsUser cmsUser = null;
					try {
						cmsUser = cms.readUser(autor);
					}
					catch (org.opencms.db.CmsDbEntryNotFoundException e){} 
					catch (CmsException e) {}
					if (cmsUser!=null)
						return cmsUser;
					else
					{
						try {
							cmsUser = cms.readUser(new CmsUUID(autor));
						}
						catch (org.opencms.db.CmsDbEntryNotFoundException e){}
						catch (NumberFormatException e){} 
						catch (CmsException e) {}
	
						if (cmsUser!=null)
							return cmsUser;
					}
				}
			}
			try {
				return cms.readUser(resource.getUserCreated());
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (CmsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return null;

	}

	public static boolean isPost(CmsObject cms, CmsResource resource) throws CmsException {
		
		CmsProperty newsType = cms.readPropertyObject(cms.getSitePath(resource), "newsType",false); 
		return (newsType.getValue("").equals("post"));
		
	}
	
	public static boolean isEnabledNotification(String eventName, CmsResource resource,
			CmsObject cmsObject) throws Exception{
		
		CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
		
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, cmsObject.getSitePath(resource));
		int publicationID = tEdicion.getId();
		
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		String configNotifications = config.getParam(site.getSiteRoot(),""+publicationID, "dashboardConfiguration","eventsNotifications",null);
		
		 
		 boolean isEnabled = false;
			
		 if(configNotifications!=null && configNotifications.indexOf(eventName)>-1)
			 isEnabled = true;		 
		
		 return isEnabled;
	}

	public static CmsUser postRevisionResponsable(CmsObject cms, String site, String publication)
	{
		String moduleConfigName = "comments";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

 		String userName = config.getParam(site, publication, moduleConfigName, "revisionCommentPostUserName");

 		try {
			return cms.readUser(userName);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
 		return null;
	}

	public static void broadcastNewsPublication(CmsResource resource,
			CmsObject cmsObject, TfsAuditAction action, String eventName) throws Exception {
		
		CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
		if (site==null)
			return; 
		
		if(!isEnabledNotification(eventName,resource,cmsObject))
			return;
		
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, cmsObject.getSitePath(resource));
		
		TelegramNotificationSender telegramSender = new TelegramNotificationSender(site.getSiteRoot(), tEdicion.getId());
		
		SlackNotificationSender slackSender =  new SlackNotificationSender(site.getSiteRoot(), tEdicion.getId());
		
		HangoutNotificationSender hangoutSenter = new HangoutNotificationSender(site.getSiteRoot(), tEdicion.getId()); 
		
		CmsUser user = cmsObject.readUser(action.getUserName());
		String userFullName =user.getFullName();
		
		//CmsUserSettings userSettings = new CmsUserSettings(user);
		//Locale locale = userSettings.getLocale();
		
		//if(locale ==null)
		//	locale = cmsObject.getRequestContext().getLocale();
		
		CmsMessages messages = new CmsMessages(BUNDLE_NAME, cmsObject.getRequestContext().getLocale());
		
		String text = 
				userFullName +" "+ 
				messages.key("GUI_AUDITACTION_"+eventName) +
				" " + cmsObject.readPropertyObject(cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath()), "Title", false).getValue("") + 
				" " + 
				UrlLinkHelper.getUrlFriendlyLink(resource, cmsObject, false, true) +
				" "+
				messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + 
				(tEdicion!=null ? tEdicion.getDescripcion() : "-");
		
		LOG.debug("broadcastNewsPublication: " + text);
		
		//telegramSender.sendMessage(text);
		//slackSender.sendMessage(text);
		//hangoutSenter.sendMessage(text);
		
		Date publication_Date = new Date();
		
		TfsAuditActionDAO auditDAO = new TfsAuditActionDAO();
		auditDAO.scheduleMessageWebhooks(cmsObject, site.getSiteRoot(), tEdicion.getId(),cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath()),text, publication_Date);
			 
	}
	
	public static void extractResourceInformation(CmsResource resource,
			CmsObject cmsObject, TfsAuditAction action) throws CmsException,
			Exception {
		action.setTimeStamp(new Date());
		action.setUserName(cmsObject.readUser(resource.getUserLastModified()).getName());
		
		CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
		if (site==null)
			return;
		
		action.setSitio(site.getTitle());
		
		cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
	
		
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, cmsObject.getSitePath(resource));
		
		action.setPublicacion("" + tEdicion.getId());
		
		action.setTargetId(cmsObject.getSitePath(resource).replaceFirst("~", ""));
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
	
	
	private int getEncuestaType()
	{
		if (encuestaType==-1) {

			try {
				encuestaType = OpenCms.getResourceManager().getResourceType("encuesta").getTypeId();
			} catch (CmsLoaderException e) {
				LOG.error("Error al intentar obtener el identificador de la encuesta",e);
			}

		}
		return encuestaType;
	}
	
}
