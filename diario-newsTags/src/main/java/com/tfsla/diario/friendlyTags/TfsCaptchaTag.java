package com.tfsla.diario.friendlyTags;

//import java.io.IOException;

import java.io.IOException;
import java.util.Date;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

//import org.opencms.flex.CmsFlexController;
//import org.opencms.main.OpenCms;

//import com.tfsla.capcha.CaptchaManager;
import com.tfsla.diario.utils.TfsIncludeContentUtil;

public class TfsCaptchaTag  extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7736936139114446095L;

	private String style = "default";
	private String lazyload = "false";
	private String siteName = null;
	private String publication = null;

	@Override
    public int doStartTag() throws JspException {
    	try {
    		
    		if (showMe()) {

				long captchaDivId = new Date().getTime();
	
				TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);
				includeContent.setParameterToRequest("captchaDivId","" + captchaDivId);			
	
				pageContext.getOut().print("<div class=\"captcha_container " + (lazyload.trim().toLowerCase().equals("true") ? " captcha_lazyload " : "") + "\" id=\"" + captchaDivId + "\"  captchaStyle=\"" + style + "\" > ");
				includeContent.setParameterToRequest("style", style);
				includeContent.setParameterToRequest("lazyload", lazyload);
				includeContent.setParameterToRequest("publication", publication);
				
				includeContent.includeNoCache("/system/modules/com.tfsla.diario.newsTags/elements/general/" + style + "/captcha.jsp");
	
				pageContext.getOut().print("</div>");

    		}
    		
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }
    
	protected boolean showMe() throws JspTagException {
	    Tag ancestor = findAncestorWithClass(this, I_CaptchaContainer.class);
	    if (ancestor != null) {
		    I_CaptchaContainer container = (I_CaptchaContainer) ancestor;
		    return container.showCaptcha();
	    }
	
		return true;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	
    public String getLazyload() {
		return lazyload;
	}

	public void setLazyload(String lazyload) {
		this.lazyload = lazyload;
	}


}
