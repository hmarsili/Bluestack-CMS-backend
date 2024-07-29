package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.terminos.model.Persons;

public class TfsPersonTwitterTag extends A_TfsPersonValueTag {
	private static final long serialVersionUID = -1981501912267791261L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		Persons person = getCurrentPerson().getPerson();
	    		if (person!=null)
	    			pageContext.getOut().print( person.getTwitter());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
