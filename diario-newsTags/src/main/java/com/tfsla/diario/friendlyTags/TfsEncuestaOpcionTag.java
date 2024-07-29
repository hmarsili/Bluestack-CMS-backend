package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class TfsEncuestaOpcionTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4264160777731756651L;

	@Override
    public int doStartTag()  {
			try {

				String opcion = getCurrentOpcionEncuesta();

				if (opcion!=null)
					pageContext.getOut().print(opcion);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JspTagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return SKIP_BODY;

	}
	
	protected String getCurrentOpcionEncuesta() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, TfsEncuestaOpcionesTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    TfsEncuestaOpcionesTag encuestaOpciones = (TfsEncuestaOpcionesTag) ancestor;
	    
	    return encuestaOpciones.getValue();
	}

}
