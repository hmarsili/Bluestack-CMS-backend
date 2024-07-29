package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

public class TfsRecipePathTag extends A_TfsNoticiaCollectionValue {

	
    @Override
    public int doStartTag() throws JspException {

    	CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = cms.getSitePath(noticia.getXmlDocument().getFile());
        
        printContent(content);

        return SKIP_BODY;
    }
    
	

}


