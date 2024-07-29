package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.tfsla.diario.comentarios.model.Comment;

//public class TfsComentarioTextoTag extends TagSupport {

public class TfsComentarioTextoTag extends A_TfsComentarioValue {

    @Override
    public int doStartTag() throws JspException {
    	try {
			//pageContext.getOut().print("{comment-text}");
    		Comment comment = getCurrentComentario().getComment();
			pageContext.getOut().print(comment.getText());

		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }
}
