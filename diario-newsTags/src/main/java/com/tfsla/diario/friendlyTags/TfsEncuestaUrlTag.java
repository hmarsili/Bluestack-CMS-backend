package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.tfsla.opencmsdev.encuestas.Encuesta;

public class TfsEncuestaUrlTag extends A_TfsEncuestaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3553971086811376306L;

	@Override
    public int doStartTag() throws JspException {
    	try {
    		 I_TfsEncuesta encuesta = getCurrentEncuesta();
    		if (encuesta!=null)
    		{
    			String pollUrl = encuesta.getEncuestaUrl();
    			if (pollUrl!=null)
    				pageContext.getOut().print(pollUrl);
    		}
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }
}