package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

public class A_TfsPersonValueTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6273771783243572263L;

	public A_TfsPersonValueTag() {
		super();
	}

	protected I_TfsPerson getCurrentPerson() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsPerson.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Person not accesible");
	    }
	    
	    I_TfsPerson persona = (I_TfsPerson) ancestor;
		return persona;
	}

}