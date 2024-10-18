package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.utils.UrlLinkHelper;

public class TfsTriviaFriendlyLinkTag extends A_TfsTriviaValueTag {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String relative = "true";
	private String publicUrl = "false";
	
	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}
	
	public String getRelative() {
		return relative;
	}

	public void setRelative(String relative) {
		if (relative!=null)
			this.relative = relative;
	}


	@Override
    public int doStartTag() throws JspException {
    	CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	I_TfsTrivia trivia = getCurrentTrivia();
        
        String content = "";
       
        content = UrlLinkHelper.getUrlFriendlyLinkRegex(trivia.getXmlDocument().getFile(), cms,
        		Boolean.parseBoolean(relative),
        		Boolean.parseBoolean(publicUrl), "trivias","urlFriendlyFormat","urlFriendlyRegex");
        
        printContent(content);
    	
    	return SKIP_BODY;
    }
	
	@Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        
        return EVAL_PAGE;
    }
    
}

