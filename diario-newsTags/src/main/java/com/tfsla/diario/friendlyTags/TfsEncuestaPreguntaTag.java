package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.tfsla.opencmsdev.encuestas.Encuesta;

public class TfsEncuestaPreguntaTag extends A_TfsEncuestaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1551857254335268836L;

	@Override
    public int doStartTag() throws JspException {
    	try {
    		 I_TfsEncuesta encuesta = getCurrentEncuesta();
    		if (encuesta!=null)
    		{
    			Encuesta poll = encuesta.getEncuesta();
    			if (poll!=null)
    				pageContext.getOut().print(poll.getPregunta());
    		}
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }

}
