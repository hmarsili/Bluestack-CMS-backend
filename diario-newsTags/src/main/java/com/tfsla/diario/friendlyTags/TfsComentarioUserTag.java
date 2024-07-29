package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.comentarios.model.Comment;

public class TfsComentarioUserTag extends A_TfsComentarioValue {

    @Override
    public int doStartTag() throws JspException {
    	try {
    		Comment comment = getCurrentComentario().getComment();
			pageContext.getOut().print(comment.getUser());
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }


}
