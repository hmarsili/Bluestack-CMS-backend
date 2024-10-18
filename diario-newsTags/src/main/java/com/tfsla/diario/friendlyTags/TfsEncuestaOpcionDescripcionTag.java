package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

public class TfsEncuestaOpcionDescripcionTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7056200812134942012L;

	@Override
    public int doStartTag()  {
			try {

				String descripcion = getCurrentOpcionDescripcionEncuesta();

				if (descripcion!=null)
					pageContext.getOut().print(descripcion);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JspTagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return SKIP_BODY;

	}
	
	protected String getCurrentOpcionDescripcionEncuesta() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, TfsEncuestaOpcionesTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    TfsEncuestaOpcionesTag encuestaOpciones = (TfsEncuestaOpcionesTag) ancestor;
	    
	    return encuestaOpciones.getDescription();
	}


}
