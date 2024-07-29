package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.terminos.model.Persons;

public class TfsPersonFacebookTag extends A_TfsPersonValueTag {
	private static final long serialVersionUID = -2202159902481671966L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		Persons person = getCurrentPerson().getPerson();
	    		if (person!=null)
	    			pageContext.getOut().print( person.getFacebook());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
