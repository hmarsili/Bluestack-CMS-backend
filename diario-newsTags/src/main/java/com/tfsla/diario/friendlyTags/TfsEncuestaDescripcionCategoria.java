package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;

public class TfsEncuestaDescripcionCategoria extends TagSupport {

	private static final long serialVersionUID = -9177044423153522185L;
	
	@Override
    public int doStartTag()  {
			try {

				CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
				String category = getCurrentCategoriaEncuesta();

				if (category!=null && !category.trim().equals("")){
					CmsProperty property;
					try {
						property = cms.readPropertyObject(category, "Title", false);
						if (property!=null && property.getValue()!=null)
							pageContext.getOut().print(property.getValue());
					} catch (CmsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	protected String getCurrentCategoriaEncuesta() throws JspTagException {
		
	    Tag ancestor = findAncestorWithClass(this, TfsEncuestaCategoriasTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    TfsEncuestaCategoriasTag encuestaCategories = (TfsEncuestaCategoriasTag) ancestor;
	    
	    return encuestaCategories.getValue();
	   
	}
}
