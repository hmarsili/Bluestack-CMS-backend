package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.utils.TfsIncludeContentUtil;
import com.tfsla.opencms.webusers.TfsUserHelper;

public class TfsComentarioRedSocialTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3773787522749114350L;
	private String style = "default";
	@Override
    public int doStartTag() throws JspException {
		
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    
	    TfsUserHelper tfsUser = new TfsUserHelper(cms.getRequestContext().currentUser());
	    
	    boolean canPostFacebook = false;
	    boolean canPostTwitter = true;
	    
	    if (!cms.getRequestContext().currentUser().isGuestUser()) {
	    	canPostFacebook = tfsUser.canPostToProvider("facebook");
	    	canPostTwitter = tfsUser.canPostToProvider("twitter");
	    }
	    
	   /* String share = "<div class=\"share\">" +
	    		"<span>Subir mi comentario a:</span>" + 
				"<input type=\"checkbox\" name=\"fb\" id=\"fb\" " + (canPostFacebook ? "checked":"disabled") + " >" + 
				"<label for=\"fb\"><span class=\"fb\">Facebook</span></label>" +

				"<input type=\"checkbox\" name=\"tw\" id=\"tw\" " + (canPostTwitter ? "checked":"disabled") + " >" + 
				"<label for=\"tw\"><span class=\"tw\">Twitter</span></label>" +
				"</div>";*/
	
	try {
		//pageContext.getOut().print(share);
		TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);
		includeContent.setParameterToRequest("canPostFacebook","" + canPostFacebook);			
		includeContent.setParameterToRequest("canPostTwitter","" + canPostTwitter);
		//pageContext.getOut().print("<div class=\"captcha_container\" id=\"" + captchaDivId + "\"> ");
		includeContent.setParameterToRequest("style", style);
		includeContent.includeNoCache("/system/modules/com.tfsla.diario.newsTags/elements/general/" + style + "/share.jsp");


	} catch (Exception e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
		throw new JspException(e);
	}

	return SKIP_BODY;
				
				
				
	}
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
