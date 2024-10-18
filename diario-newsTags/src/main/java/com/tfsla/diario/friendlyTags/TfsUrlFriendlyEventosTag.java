package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.utils.UrlLinkHelper;



public class TfsUrlFriendlyEventosTag extends A_TfsNoticiaValue {
	
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
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        
        return EVAL_PAGE;
    }
	
	@Override
    public int doStartTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia evento = getCurrentNews();
        boolean relativeBoolean = Boolean.parseBoolean(relative);
        boolean publicUrlBoolean = Boolean.parseBoolean(publicUrl);
     	String content = UrlLinkHelper.getUrlFriendlyEventosLink(evento.getXmlDocument().getFile(), cms, this.pageContext.getRequest(),relativeBoolean , publicUrlBoolean );
        
     	printContent(content);

        relative = "true";
        
        return SKIP_BODY;
    }

}
