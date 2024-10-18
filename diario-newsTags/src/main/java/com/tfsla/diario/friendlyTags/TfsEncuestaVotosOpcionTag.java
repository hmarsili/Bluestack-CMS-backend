package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.tfsla.opencmsdev.encuestas.RespuestaEncuestaConVotos;

public class TfsEncuestaVotosOpcionTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -786081845666975300L;

	@Override
    public int doStartTag()  {
			try {

				int optionNumber = getCurrentOptionNumber();
				I_TfsEncuesta encuesta = getCurrentEncuesta();

				if (encuesta!=null)
				{
					try{
					   RespuestaEncuestaConVotos resp = encuesta.getResultadosEncuesta().getRespuestas().get(optionNumber);
					
					   pageContext.getOut().print(resp.getCantVotos());
					}catch (IndexOutOfBoundsException ex){
						pageContext.getOut().print("0");
					}
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
