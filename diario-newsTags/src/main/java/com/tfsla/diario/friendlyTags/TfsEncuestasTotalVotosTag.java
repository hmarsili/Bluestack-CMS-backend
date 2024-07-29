package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.opencmsdev.encuestas.ResultadoEncuestaBean;

public class TfsEncuestasTotalVotosTag extends A_TfsEncuestaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -553015557025393417L;

	@Override
    public int doStartTag() throws JspException {
    	try {
    		 I_TfsEncuesta encuesta = getCurrentEncuesta();
    		if (encuesta!=null)
    		{
    			ResultadoEncuestaBean pollResults = encuesta.getResultadosEncuesta();
    			if (pollResults!=null)
    				pageContext.getOut().print(pollResults.getTotalVotos());
    		}
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }

}
