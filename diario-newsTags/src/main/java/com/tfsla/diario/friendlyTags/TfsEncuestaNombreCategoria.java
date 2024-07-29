package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class TfsEncuestaNombreCategoria extends TagSupport {

	private static final long serialVersionUID = -9177044423153522185L;
	
	@Override
    public int doStartTag()  {
			try {

				String categoria = getCurrentCategoriaEncuesta();

				if (categoria!=null)
					pageContext.getOut().print(categoria);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JspTagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return SKIP_BODY;

	}

	protected String getCurrentCategoriaEncuesta() throws JspTagException {
		
	    Tag ancestor = findAncestorWithClass(this, TfsEncuestaCategoriasTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    TfsEncuestaCategoriasTag encuestaCategories = (TfsEncuestaCategoriasTag) ancestor;
	    
	    return encuestaCategories.getValue();
	}
}
