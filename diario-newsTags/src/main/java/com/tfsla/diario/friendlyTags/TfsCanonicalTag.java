package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.utils.UrlLinkHelper;

public class TfsCanonicalTag extends A_TfsNoticiaValue {
	
    @Override
    public int doStartTag() throws JspException {
    	CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = "";
        String external = UrlLinkHelper.getExternalLink(noticia.getXmlDocument().getFile(), cms);
	
        if (external!=null && !external.isEmpty())
        	content = external;
        else
        	content = UrlLinkHelper.getCanonicalLink(noticia.getXmlDocument().getFile(), cms, this.pageContext.getRequest());
        
        printContent(content);
    	
    	return SKIP_BODY;
    }
    
    
}
