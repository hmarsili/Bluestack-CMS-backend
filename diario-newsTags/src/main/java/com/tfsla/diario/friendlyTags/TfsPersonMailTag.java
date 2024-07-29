package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.terminos.model.Persons;

public class TfsPersonMailTag extends A_TfsPersonValueTag {
	private static final long serialVersionUID = 8846779077428186204L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		Persons person = getCurrentPerson().getPerson();
	    		if (person!=null)
	    			pageContext.getOut().print( person.getEmail());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
