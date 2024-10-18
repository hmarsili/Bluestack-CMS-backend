package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.terminos.model.Persons;

public class TfsPersonShortdescriptionTag extends A_TfsPersonValueTag {
	private static final long serialVersionUID = 7471109791774285104L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		Persons person = getCurrentPerson().getPerson();
	    		if (person!=null)
	    			pageContext.getOut().print( person.getShortdescription());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
