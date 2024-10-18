package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagLink;

import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.UrlLinkHelper;

public class TfsLinkTag extends A_TfsNoticiaValue {

    @Override
    public int doStartTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = "";
        String external = UrlLinkHelper.getExternalLink(noticia.getXmlDocument().getFile(), cms);
	
        if (external!=null && !external.isEmpty())
        	content = external;
        else
        	content =CmsJspTagLink.linkTagAction("/" + CmsResourceUtils.getLink(noticia.getXmlDocument().getFile()), this.pageContext.getRequest());
        
        printContent(content);

        return SKIP_BODY;
    }

}
