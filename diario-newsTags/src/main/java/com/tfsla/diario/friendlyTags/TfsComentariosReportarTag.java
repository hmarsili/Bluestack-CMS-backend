package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;

public class TfsComentariosReportarTag  extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9013507443186697471L;

	String commentid = "";
    @Override
    public int doStartTag() throws JspException {
    	
    	
    	String functionOnclickEnviar = "";
    	
    	CmsObject cms = CmsFlexController.getController(pageContext.getRequest()).getCmsObject();
    	Comment comment = CommentsModule.getInstance(cms).getComment(cms, commentid);

    	//String username = comment.getUser();
    	//if(username.indexOf("/")>-1)
    	//	username = username.substring(username.indexOf("/")+1);
    	//String fecha_completa = comment.getDateAsString(); 

    	if (cms.getRequestContext().currentUser().isGuestUser())
    		functionOnclickEnviar = "javascript:redirectToLogin();";
    	
    /*	String comment_text = comment.getText().replace("<","&lt;");
    	       comment_text = comment_text.replaceAll("\"", "&quot;");
    	       comment_text = comment_text.replaceAll("'", "&quot;");
*/
		functionOnclickEnviar = "showPopupReportarComentario(";
		//functionOnclickEnviar += "'" + comment_text + "',";
		functionOnclickEnviar += "'" + commentid + "'";
		//functionOnclickEnviar += "'" + username + "',";
		//functionOnclickEnviar += "'" + fecha_completa + "',";
		functionOnclickEnviar += ");";

    	String txt = 
			"<a href=\"#tope\" title=\"Reportar\" onclick=\"" + functionOnclickEnviar + "\">Reportar</a>";

    	try {
			pageContext.getOut().print(txt);

		} catch (IOException e) {
			throw new JspException(e);
		}

		return SKIP_BODY;
    }
	public String getCommentid() {
		return commentid;
	}
	public void setCommentid(String commentid) {
		this.commentid = commentid;
	}
}
