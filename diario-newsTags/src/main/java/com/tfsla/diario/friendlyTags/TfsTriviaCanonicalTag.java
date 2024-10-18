package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.utils.UrlLinkHelper;

public class TfsTriviaCanonicalTag extends A_TfsTriviaValueTag {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public int doStartTag() throws JspException {
    	CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	I_TfsTrivia trivia = getCurrentTrivia();
        
        String content = "";
       
        content = UrlLinkHelper.getCanonicalLinkNoNews(trivia.getXmlDocument().getFile(), cms, this.pageContext.getRequest(), "trivias","urlFriendlyFormat","urlFriendlyRegex");
        
        printContent(content);
    	
    	return SKIP_BODY;
    }
    
}

