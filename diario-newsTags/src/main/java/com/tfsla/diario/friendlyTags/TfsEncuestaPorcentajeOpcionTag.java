package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.tfsla.opencmsdev.encuestas.RespuestaEncuestaConVotos;

public class TfsEncuestaPorcentajeOpcionTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5642887618126153353L;
	private String format = "#0.0";
	
	@Override
    public int doStartTag()  {
			try {

				int optionNumber = getCurrentOptionNumber();
				I_TfsEncuesta encuesta = getCurrentEncuesta();

				if (encuesta!=null)
				{
					DecimalFormat df = new DecimalFormat(format);
					
					List<RespuestaEncuestaConVotos> respuestaVotos = encuesta.getResultadosEncuesta().getRespuestas();
					RespuestaEncuestaConVotos resp = null;
					if (respuestaVotos.size()>optionNumber)
						resp = respuestaVotos.get(optionNumber);		
										
					pageContext.getOut().print(df.format(resp !=null ? resp.getPorcentajeVotos() : 0));
					
					
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
