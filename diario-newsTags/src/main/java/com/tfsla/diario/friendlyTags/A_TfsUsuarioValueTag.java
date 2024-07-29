package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class A_TfsUsuarioValueTag extends TagSupport {

	private static final long serialVersionUID = -1043953984760562066L;

	public A_TfsUsuarioValueTag() {
		super();
	}

	protected I_TfsUser getCurrentUser() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsUser.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag User not accesible");
	    }
	    
	    I_TfsUser usuario = (I_TfsUser) ancestor;
		return usuario;
	}

}