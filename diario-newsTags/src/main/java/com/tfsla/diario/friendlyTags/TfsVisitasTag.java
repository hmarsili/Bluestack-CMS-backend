package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;
import org.opencms.flex.CmsFlexController;

public class TfsVisitasTag extends A_TfsNoticiaValue {

	
    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        String newsPath = cms.getSitePath(noticia.getXmlDocument().getFile());

        String content = "<div style=\"display:inline\" type=\"ranking\" mode=\"views\" path=\"" + newsPath + "\">0</div>";
        printContent(content);

        return SKIP_BODY;
    }

}
