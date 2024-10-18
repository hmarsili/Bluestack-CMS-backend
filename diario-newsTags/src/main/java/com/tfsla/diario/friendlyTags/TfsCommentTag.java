package com.tfsla.diario.friendlyTags;

//import java.util.List;

import jakarta.servlet.jsp.JspException;
//import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
//import jakarta.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.comentarios.data.CommentPersistor;
import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.diario.model.TfsComentario;



public class TfsCommentTag extends BodyTagSupport implements I_TfsComentario {   

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5324532744564095136L;

	/** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(TfsCommentTag.class);

    protected TfsComentario previousComentario = null;
    
	
	protected Comment comment=null;
	
		protected String commentId;
	
	
	

	@Override
	public int doStartTag() throws JspException {
		saveComentario();
		try{
		findComment();
		
		} catch (Exception e) {
			LOG.error("Failed to find the comment " + commentId);
			return SKIP_BODY;
		}
			exposeComentario(getComment());
		
			return EVAL_BODY_INCLUDE;
				

	}

	

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}


	protected void findComment()
	{
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    saveComentario();
	    
	 
	    if (commentId==null || commentId.equals(""))
	    	commentId = "0";
		CommentPersistor cPer = new CommentPersistor(CommentsModule.getInstance(cms));
		
		comment =cPer.getComment(cms, commentId);
			
		
	}
	
	public Comment getComment()
	{
		return comment;
	}
	
	
	
	
    protected void exposeComentario(Comment comentario)
    {
    	TfsComentario comment = new TfsComentario(comentario);
		pageContext.getRequest().setAttribute("comment", comment);

    }
    
    protected void restoreComentario()
    {
    	pageContext.getRequest().setAttribute("comment", previousComentario );
    }

	protected void saveComentario()
    {
		previousComentario  = (TfsComentario) pageContext.getRequest().getAttribute("comment");
    	pageContext.getRequest().setAttribute("comment",null);
    }
	
	

}
