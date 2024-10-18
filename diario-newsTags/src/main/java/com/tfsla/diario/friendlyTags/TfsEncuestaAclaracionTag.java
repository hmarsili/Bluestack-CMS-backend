package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.tfsla.opencmsdev.encuestas.Encuesta;

public class TfsEncuestaAclaracionTag extends A_TfsEncuestaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7022709249707521566L;

	@Override
    public int doStartTag() throws JspException {
    	try {
    		 I_TfsEncuesta encuesta = getCurrentEncuesta();
    		if (encuesta!=null)
    		{
    			Encuesta poll = encuesta.getEncuesta();
    			if (poll!=null)
    				pageContext.getOut().print(poll.getTextoAclaratorio());
    		}
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }

}
