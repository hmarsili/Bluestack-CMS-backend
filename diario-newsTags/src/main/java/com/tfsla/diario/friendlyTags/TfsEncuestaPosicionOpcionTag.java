package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.tfsla.opencmsdev.encuestas.RespuestaEncuestaConVotos;

public class TfsEncuestaPosicionOpcionTag extends TagSupport {


	/**
	 * 
	 */
	private static final long serialVersionUID = -1135814578064499190L;

	@Override
    public int doStartTag()  {
			try {

				int optionNumber = getCurrentOptionNumber();
				I_TfsEncuesta encuesta = getCurrentEncuesta();

				if (encuesta!=null)
				{
					List<RespuestaEncuestaConVotos> respuestaVotos = encuesta.getResultadosEncuesta().getRespuestas();
					RespuestaEncuestaConVotos resp = null;
					if (respuestaVotos.size()>optionNumber)
						resp = respuestaVotos.get(optionNumber);		
					
					pageContext.getOut().print(resp != null ? resp.getPosicionResultados() : 0);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JspTagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return SKIP_BODY;

	}
	
	protected int getCurrentOptionNumber() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, TfsEncuestaOpcionesTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    TfsEncuestaOpcionesTag encuestaOpciones = (TfsEncuestaOpcionesTag) ancestor;
	    return  encuestaOpciones.getOpcionIndex();
	    
	}

	protected I_TfsEncuesta getCurrentEncuesta() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsEncuesta.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    I_TfsEncuesta encuesta = (I_TfsEncuesta) ancestor;
		return encuesta;
	}
}
