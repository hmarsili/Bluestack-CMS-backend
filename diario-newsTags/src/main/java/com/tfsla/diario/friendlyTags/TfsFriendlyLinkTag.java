package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.utils.UrlLinkHelper;

public class TfsFriendlyLinkTag extends A_TfsNoticiaValue {

	private String relative = "true";
	private String publicUrl = "false";
	
	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	public TfsFriendlyLinkTag() {
		relative = "true";
	}
	
	@Override
    public int doStartTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = "";
        String external = UrlLinkHelper.getExternalLink(noticia.getXmlDocument().getFile(), cms);
	
        if (external!=null && !external.isEmpty())
        	content = external;
        else
        	content = UrlLinkHelper.getUrlFriendlyLink(noticia.getXmlDocument().getFile(), cms, this.pageContext.getRequest(),Boolean.parseBoolean(relative), Boolean.parseBoolean(publicUrl));
        
        printContent(content);

        relative = "true";
        
        return SKIP_BODY;
    }
	
    public String getRelative() {
		return relative;
	}

	public void setRelative(String relative) {
		if (relative!=null)
			this.relative = relative;
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
