package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.opencmsdev.encuestas.Encuesta;

public class TfsEncuestaIdTag extends A_TfsEncuestaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4721468239493623228L;

	@Override
    public int doStartTag() throws JspException {
    	try {
    		 I_TfsEncuesta encuesta = getCurrentEncuesta();
    		if (encuesta!=null)
    		{
    			Encuesta poll = encuesta.getEncuesta();
    			if (poll!=null)
    				pageContext.getOut().print(poll.getIdEncuesta());
    		}
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }

}

