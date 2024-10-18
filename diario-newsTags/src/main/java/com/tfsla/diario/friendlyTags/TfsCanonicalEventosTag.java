package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.utils.UrlLinkHelper;

public class TfsCanonicalEventosTag extends A_TfsNoticiaValue {
	
    @Override
    public int doStartTag() throws JspException {
    	CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia evento = getCurrentNews();
        
       	String content = UrlLinkHelper.getCanonicalEventosLink(evento.getXmlDocument().getFile(), cms, this.pageContext.getRequest());
            
        printContent(content);
    	
    	return SKIP_BODY;
    }

}
