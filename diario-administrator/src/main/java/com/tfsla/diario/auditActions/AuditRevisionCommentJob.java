package com.tfsla.diario.auditActions;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.diario.auditActions.data.TfsAuditActionDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAction;
import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;

public class AuditRevisionCommentJob implements I_CmsScheduledJob {

	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		// TODO Auto-generated method stub
		
		Calendar cal = new GregorianCalendar();
		String hours = (String)parameters.get("hours");
		
		if (hours==null)
			hours = "1";

		cal.add(Calendar.HOUR, -1 *Integer.parseInt(hours));

		Date from = cal.getTime();
		
		List<Comment> comments = CommentsModule.getInstance(cms).getCommentsInReVision(cms,from);
		
		TfsAuditActionDAO auditDAO = new TfsAuditActionDAO();
		
		String site = cms.getRequestContext().getSiteRoot();
		
		for (Comment comment : comments) {
		
			cms.getRequestContext().setSiteRoot("/sites/" + comment.getSite() + "/");
			
			TfsAuditAction action = new TfsAuditAction();
			action.setActionId(TfsAuditAction.ACTION_COMMENT_REVISION);

			CmsResource resource = cms.readResource(comment.getNoticiaURL(),CmsResourceFilter.ALL);
			TfsAuditActionsListener.extractResourceInformation(resource, cms, action);
			
			action.setUserName(CmsWorkplaceAction.getInstance().getCmsAdminObject().getRequestContext().currentUser().getName());

			auditDAO.insertUserAuditEvent(action);

			CmsUser user = null;
			if (TfsAuditActionsListener.isPost(cms,resource))
				user = TfsAuditActionsListener.postRevisionResponsable(cms, action.getSitio(),action.getPublicacion());
			else
				user = TfsAuditActionsListener.newsRevisionResponsable(cms, resource, action.getSitio(),action.getPublicacion());
			
			auditDAO.insertNotificationAuditEvent(action.getEventId(),action.getTimeStamp(),user.getName());

		}
		
		cms.getRequestContext().setSiteRoot(site);
		
		return "Auditoria de comentarios en revision ejecutado en " + new Date() + " - " + comments.size() + " comentarios en revision";
	}

}
