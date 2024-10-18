package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

public abstract class A_TfsComentarioValue extends TagSupport  {

	protected I_TfsComentario getCurrentComentario() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsComentario.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Comments not accesible");
	    }
	    
	    I_TfsComentario comentario = (I_TfsComentario) ancestor;
		return comentario;
	}

}
