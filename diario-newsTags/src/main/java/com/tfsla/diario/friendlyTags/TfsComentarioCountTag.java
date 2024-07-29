package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.comentarios.model.Comment;

public class TfsComentarioCountTag extends A_TfsComentarioValue {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4078844053619545755L;

	@Override
    public int doStartTag() throws JspException {
    	try {
			//pageContext.getOut().print("{comment-text}");
    		Comment comment = getCurrentComentario().getComment();
			pageContext.getOut().print(comment.getCommentCount());

		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }
}
