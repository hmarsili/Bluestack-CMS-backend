package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class A_TfsEncuestaValueTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5053747488013311242L;

	public A_TfsEncuestaValueTag() {
		super();
	}

	protected I_TfsEncuesta getCurrentEncuesta() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsEncuesta.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    I_TfsEncuesta encuesta = (I_TfsEncuesta) ancestor;
		return encuesta;
	}

}
