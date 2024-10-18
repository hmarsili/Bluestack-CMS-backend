package com.tfsla.diario.auditActions.service;

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.admin.jsp.TfsMessages;
import com.tfsla.diario.auditActions.TfsAuditActionsListener;
import com.tfsla.diario.auditActions.data.TfsAuditActionDAO;
import com.tfsla.diario.auditActions.data.TfsAuditCommentDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAction;
import com.tfsla.diario.auditActions.model.TfsAuditComment;
import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsAuditActionService {
		
	public String getFullDescription(CmsJspActionElement jspAction, TfsAuditAction action) {
		String text="";
		CmsObject cms = jspAction.getCmsObject();
		TipoEdicionService tService = new TipoEdicionService();
		
		CmsUser user = null;
		String userFullName =""; 
		CmsResource resource = null;
		TipoEdicion tEdicion =null;
		
		PageContext context     = jspAction.getJspContext();
		HttpServletRequest req  = jspAction.getRequest();
		HttpServletResponse res = jspAction.getResponse();
		
		TfsMessages messages = new TfsMessages(context, req, res);
		
		try {
			if (action.getUserName()!=null) {
				user = cms.readUser(action.getUserName());
				userFullName =user.getFullName();
			}
		} catch (CmsException e) {
			user = null;
			userFullName = messages.keyDefault("GUI_AUDITACTION_USER_UNKNOWN", "usuario eliminado o desconocido");
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		switch (action.getActionId()) {
		case TfsAuditAction.ACTION_USER_STATUS:
			text = action.getDescription();
			
			break;
		case TfsAuditAction.ACTION_USER_REPORT:
			text = action.getDescription();
			
			break;
		case TfsAuditAction.ACTION_NEWS_PUBLISHED:
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName +" "+ messages.keyDefault("GUI_AUDITACTION_NEWS_PUBLISHED", "publicó la noticia") +" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + "</a> "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_POST_PUBLISHED:
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_POST_PUBLISHED","publicó el post") +" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + "</a> "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;

		case TfsAuditAction.ACTION_NEWS_STATUS_CHANGED:
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName +" "+messages.keyDefault("GUI_AUDITACTION_NEWS_STATUS_CHANGED", "modificó el estado de la noticia") + " <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + "</a>  "+messages.keyDefault("GUI_AUDITACTION_TO","a")+" " + action.getDescription() + " "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_NEWS_VALUE_CHANGED:
			break;
		case TfsAuditAction.ACTION_POLL_PUBLISHED:
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName +" "+ messages.keyDefault("GUI_AUDITACTION_POLL_PUBLISHED", "publicó la encuesta") + " <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + "</a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_POLL_STATUS_CHANGED:
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName +" "+ messages.keyDefault("GUI_AUDITACTION_POLL_STATUS_CHANGED", "modificó el estado de la encuesta") + " <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + "</a>  "+messages.keyDefault("GUI_AUDITACTION_TO","a")+" " + action.getDescription() + "  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_USER_CREATED:
			text = userFullName + " "+ messages.keyDefault("GUI_AUDITACTION_USER_CREATED", "se registró en el sitio");
			break;
		case TfsAuditAction.ACTION_USER_LOGIN:
			text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_USER_LOGIN","ingresó al sitio");
			
			break;
		case TfsAuditAction.ACTION_NEWS_DELETED:
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_NEWS_DELETED","eliminó la noticia")+" " + action.getDescription() + "(" + action.getTargetId() + ")  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_POLL_DELETED:
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_POLL_DELETED","eliminó la encuesta")+" " + action.getDescription() + "(" + action.getTargetId() + ")  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_NEWS_CREATED:
			
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_NEWS_CREATED","creó la noticia")+" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_POST_CREATED:

			try {
				resource = cms.readResource(action.getTargetId(),CmsResourceFilter.ALL);

				tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
				user = TfsAuditActionsListener.newsRevisionResponsable(cms, resource, action.getSitio(),action.getPublicacion());
				
				text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_POST_CREATED","creó el post")+" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");

			} catch (CmsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			break;
		case TfsAuditAction.ACTION_POLL_CREATED:		
			
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			
			text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_POLL_CREATED","creó la encuesta")+" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
			
		case TfsAuditAction.ACTION_COMMENT_REPORTED:
			String description = action.getDescription();
			String[] descParts = description.split("\\|");
			
			String abuseType = descParts[0];
			String commentId = descParts[1];
			
			String abuseDescription = CommentsModule.getInstance(action.getSitio(),action.getPublicacion()).getAbuseTypeDescription(new Integer(abuseType));
			
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			text = userFullName + " "+messages.keyDefault("GUI_AUDITACTION_COMMENT_REPORTED_0","reportó abuso")+" (" + abuseDescription + ") "+messages.keyDefault("GUI_AUDITACTION_COMMENT_REPORTED_1","sobre el comentario")+" " + commentId + " "+messages.keyDefault("GUI_AUDITACTION_COMMENT_REPORTED_2","en la noticia")+" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-");
			break;
		case TfsAuditAction.ACTION_COMMENT_REVISION:
			
			boolean ispost = false;

			try {
				resource = cms.readResource(action.getTargetId(),CmsResourceFilter.ALL);

				ispost = TfsAuditActionsListener.isPost(cms,resource);
											
				tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
	
				if (ispost)
					user = TfsAuditActionsListener.postRevisionResponsable(cms, action.getSitio(),action.getPublicacion());
				else
					user = TfsAuditActionsListener.newsRevisionResponsable(cms, resource, action.getSitio(),action.getPublicacion());
			
				if (user != null)
					userFullName = user.getFullName();
				else
					userFullName = messages.keyDefault("GUI_AUDITACTION_USER_UNKNOWN", "usuario eliminado o desconocido");
				
				text = userFullName + (ispost ? " "+messages.keyDefault("GUI_AUDITACTION_THE_POST","el post")+" " : " "+messages.keyDefault("GUI_AUDITACTION_THE_NEWS","la noticia")+" ") + " <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-") + " "+messages.keyDefault("GUI_AUDITACTION_COMMENT_REVISION","posee un comentario que requiere REVISION");
			
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case TfsAuditAction.ACTION_POST_REPORTED:

			String abuseActionDescriptionPost = action.getDescription();
			String[] descPartsPost = abuseActionDescriptionPost.split("|");
			
			String abusePostDescription = descPartsPost[0];
			String abusePostUserMessage = org.apache.commons.lang.StringEscapeUtils.escapeHtml(descPartsPost[1]);
			
			
			tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(action.getPublicacion()));
			text = 
					userFullName + " " +
					messages.keyDefault("GUI_AUDITACTION_COMMENT_REPORTED_0","reportó abuso") + 
					" (" + abusePostDescription + ") "+messages.keyDefault("GUI_AUDITACTION_POST_REPORTED_1","sobre el post")+
					" " + "<a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+
					messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-") + 
					"<br>\"" + abusePostUserMessage + "\"";
			
			break;
		case TfsAuditAction.ACTION_POST_REVISION:
			text = messages.keyDefault("GUI_AUDITACTION_THE_POST","el post")+" " + 
					" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-") + " "+
					messages.keyDefault("GUI_AUDITACTION_POST_REVISION","requiere REVISION");
			break;
			
		case TfsAuditAction.ACTION_POST_ACEPTED:
			text = messages.keyDefault("GUI_AUDITACTION_THE_POST","el post")+" " + 
					" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-") + " ";
			String msgAccepted = messages.keyDefault("GUI_AUDITACTION_POST_APPROVED","fue aprobado " + (userFullName.trim().equals("") ? "" : "por " + userFullName) );
			if(userFullName.trim().equals("")) msgAccepted = msgAccepted.replace("por", "").replace("by", "");
			else msgAccepted += userFullName;
			text += msgAccepted;
			break;
		case TfsAuditAction.ACTION_POST_REJECTED:
			text = messages.keyDefault("GUI_AUDITACTION_THE_POST","el post")+" " + 
					" <a href='" + jspAction.link(action.getTargetId()) + "' target='_blank'>" + resourceTitle(cms,action.getTargetId(),action.getSitio()) + " </a>  "+messages.keyDefault("GUI_AUDITACTION_IN","en")+" " + (tEdicion!=null ? tEdicion.getDescripcion() : "-") + " ";
			String msgRejected = messages.keyDefault("GUI_AUDITACTION_POST_REJECTED","fue rechazado " + (userFullName.trim().equals("") ? "" : "por " + userFullName) );
			if(userFullName.trim().equals("")) msgRejected = msgRejected.replace("por", "").replace("by", "");
			else msgRejected += userFullName;
			text += msgRejected;
			break;
		case TfsAuditAction.ACTION_VIDEO_ENCODER:
			text = action.getDescription();
			
			break;
		}

		
		return text;
	}
	
	private String resourceTitle(CmsObject cms, String url, String SiteRoot) {
		String title="";
		String lastSiteRoot = "";
		if (!cms.getRequestContext().getSiteRoot().equals(SiteRoot)){
			lastSiteRoot = cms.getRequestContext().getSiteRoot();
			cms.getRequestContext().setSiteRoot(SiteRoot);
		}
		try {
			CmsResource res = cms.readResource(url,CmsResourceFilter.ALL);
			title = cms.readPropertyObject(res, "Title", false).getValue(url);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!lastSiteRoot.isEmpty())
			cms.getRequestContext().setSiteRoot(lastSiteRoot);
		
		return title;
	}
	
	public String getActionDescription(CmsJspActionElement jspAction,TfsAuditAction action){
		
		PageContext context     = jspAction.getJspContext();
		HttpServletRequest req  = jspAction.getRequest();
		HttpServletResponse res = jspAction.getResponse();
		
		CmsObject cms = jspAction.getCmsObject();
		
		TfsMessages messages = new TfsMessages(context, req, res);
		CmsUser user = null;
		String userFullName = "";
		try {
			if (action.getUserName()!=null) {
				user = cms.readUser(action.getUserName());
				userFullName =user.getFullName();
			}
		} catch (CmsException e) {
			user = null;
			userFullName = messages.keyDefault("GUI_AUDITACTION_USER_UNKNOWN", "usuario eliminado o desconocido");
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		switch (action.getActionId()) {
		case TfsAuditAction.ACTION_USER_STATUS:
			return userFullName + " " + messages.keyDefault("GUI_AUDITACTION_USER_STATUS_TITLE","publico su estado");
		case TfsAuditAction.ACTION_USER_REPORT:
			return userFullName + " " + messages.keyDefault("GUI_AUDITACTION_USER_REPORT_TITLE","comento");
		case TfsAuditAction.ACTION_NEWS_PUBLISHED:
			return messages.keyDefault("GUI_AUDITACTION_NEWS_PUBLISHED_TITLE","Noticia publicada");
		case TfsAuditAction.ACTION_POST_PUBLISHED:
			return messages.keyDefault("GUI_AUDITACTION_POST_PUBLISHED_TITLE","Post publicado");
		case TfsAuditAction.ACTION_NEWS_STATUS_CHANGED:
			return messages.keyDefault("GUI_AUDTIACTION_NEWS_STATUS_CHANGED_TITLE","Modificación de noticia");
		case TfsAuditAction.ACTION_NEWS_VALUE_CHANGED:
			break;
		case TfsAuditAction.ACTION_POLL_PUBLISHED:
			return messages.keyDefault("GUI_AUDITACTION_POLL_PUBLISHED_TITLE","Encuesta publicada");
		case TfsAuditAction.ACTION_POLL_STATUS_CHANGED:
			return messages.keyDefault("GUI_AUDTIACTION_POLL_STATUS_CHANGED_TITLE","Modificación de encuesta");
		case TfsAuditAction.ACTION_USER_CREATED:
			return messages.keyDefault("GUI_AUDTIACTION_USER_CREATED_TITLE","Nuevo usuario");
		case TfsAuditAction.ACTION_USER_LOGIN:
			return messages.keyDefault("GUI_AUDITACTION_USER_LOGIN_TITLE","Login de usuario");
		case TfsAuditAction.ACTION_NEWS_DELETED:
			return messages.keyDefault("GUI_AUDITACTION_NEWS_DELETED_TITLE","Noticia eliminada");
		case TfsAuditAction.ACTION_POLL_DELETED:
			return messages.keyDefault("GUI_AUDITACTION_POLL_DELETED_TITLE","Encuesta eliminada");
		case TfsAuditAction.ACTION_POST_CREATED:
			return messages.keyDefault("GUI_AUDITACTION_POST_CREATED_TITLE","Nuevo post");
		case TfsAuditAction.ACTION_NEWS_CREATED:
			return messages.keyDefault("GUI_AUDITACTION_NEWS_CREATED_TITLE","Nueva noticia");
		case TfsAuditAction.ACTION_POLL_CREATED:
			return messages.keyDefault("GUI_AUDITACTION_POLL_CREATED_TITLE","Nueva encuesta");
		case TfsAuditAction.ACTION_COMMENT_REPORTED:
			return messages.keyDefault("GUI_AUDITACTION_COMMENT_REPORTED_TITLE","Nuevo reporte de comentario");
		case TfsAuditAction.ACTION_COMMENT_REVISION:
			return messages.keyDefault("GUI_AUDITACTION_COMMENT_REVISION_TITLE","Comentario pendiente de revisión");
		case TfsAuditAction.ACTION_POST_REPORTED:
			return messages.keyDefault("GUI_AUDITACTION_POST_REPORTED_TITLE","Nuevo reporte de post");
		case TfsAuditAction.ACTION_POST_REVISION:
			return messages.keyDefault("GUI_AUDITACTION_POST_REVISION_TITLE","Post pendiente de revisión");
		case TfsAuditAction.ACTION_POST_ACEPTED:
			return messages.keyDefault("GUI_AUDITACTION_POST_APPROVED_TITLE","Post aprobado");
		case TfsAuditAction.ACTION_POST_REJECTED:
			return messages.keyDefault("GUI_AUDITACTION_POST_REJECTED_TITLE","Post rechazado");
		case TfsAuditAction.ACTION_VIDEO_ENCODER:
			return messages.keyDefault("GUI_AUDITACTION_VIDEO_CONVERTER","Conversión de videos");
		}
		return "";
		
	}
	
	public long addStatus(CmsObject cms, String text, String fromUser, List<String> toUsers){
		TfsAuditAction action = new TfsAuditAction();
		action.setActionId(TfsAuditAction.ACTION_USER_STATUS);
		action.setTimeStamp(new Date());
		action.setUserName(fromUser);
		action.setDescription(text);
		
		TfsAuditActionDAO aDAO = new TfsAuditActionDAO();
		try {
			aDAO.insertUserAuditEvent(action);
		
			for (String destUser : toUsers ) {
				aDAO.insertNotificationAuditEvent(action.getEventId(),action.getTimeStamp(),destUser);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return action.getEventId();
	}

	public TfsAuditComment addComment(CmsObject cms, String text, String fromUser, Integer eventId, List<String> toUsers){
		
		TfsAuditComment comment = new TfsAuditComment();
		
		comment.setTimeStamp(new Date());
		comment.setUserName(fromUser);
		comment.setDescription(text);
		comment.setEventId(eventId);
		
		TfsAuditCommentDAO cDAO = new TfsAuditCommentDAO();
		TfsAuditActionDAO aDAO = new TfsAuditActionDAO();
		try {
			cDAO.insertCommentAuditEvent(comment);
		
			aDAO.addCommentCount(eventId);
			
			for (String destUser : toUsers ) {
				aDAO.insertNotificationAuditEvent(eventId, comment.getTimeStamp(),destUser);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return comment;
	}

	public int getCountUnreadUserNotifications(CmsObject cms, String userName) throws Exception
	{
		TfsAuditActionDAO aDAO = new TfsAuditActionDAO();
		CmsUser user = cms.readUser(userName);
		
		Long lastRead = (Long) user.getAdditionalInfo("USER_NOTIFICATION_LAST_READ");
		Date fromTimeStamp = null;
		
		if (lastRead!=null){
			fromTimeStamp = new Date(lastRead);
		}
		
		return aDAO.unreadUserNotifications(fromTimeStamp, userName);
		
	}
	
	public List<TfsAuditAction> getUnreadUserNotifications(CmsObject cms, String userName, int max) throws Exception
	{
		TfsAuditActionDAO aDAO = new TfsAuditActionDAO();
		CmsUser user = cms.readUser(userName);
		
		Long lastRead = (Long) user.getAdditionalInfo("USER_NOTIFICATION_LAST_READ");
		Date fromTimeStamp = null;
		
		if (lastRead!=null){
			fromTimeStamp = new Date(lastRead);
		}

		user.setAdditionalInfo("USER_NOTIFICATION_LAST_READ",new Date().getTime());

		cms.writeUser(user);
		
		return aDAO.getActions(fromTimeStamp, null, null, userName, null, max);
		
	}
	
	public List<String> getDefaultVisibleActions(PageContext context, HttpServletRequest req, HttpServletResponse res)  throws Exception {
	
	    CmsFlexController m_controller = CmsFlexController.getController(req);
	    HttpSession m_session = req.getSession();
        
		CmsObject cms = m_controller.getCmsObject();
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	
    	TipoEdicion currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	
    	String publication = "" + currentPublication.getId();


		String moduleConfigName = "dashboardConfiguration";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
        
 		return config.getParamList(siteName, publication, moduleConfigName, "DefaultVisibleActions");
		
	}
	
	public String getActionName(CmsJspActionElement jspAction,String action){
		
		PageContext context     = jspAction.getJspContext();
		HttpServletRequest req  = jspAction.getRequest();
		HttpServletResponse res = jspAction.getResponse();
		
		TfsMessages messages = new TfsMessages(context, req, res);
		
		return messages.keyDefault("ACTION_" + action,action);
	}
}
