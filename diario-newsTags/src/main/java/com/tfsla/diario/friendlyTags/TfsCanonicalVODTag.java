package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

public class TfsCanonicalVODTag extends A_TfsNoticiaValue {
	
    @Override
    public int doStartTag() throws JspException {
    	CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia evento = getCurrentNews();
        
       	//String content = UrlLinkHelper.getCanonicalVODLink(evento.getXmlDocument().getFile(), cms, this.pageContext.getRequest());
            
        //OprintContent(content);
    	
    	return SKIP_BODY;
    }

}