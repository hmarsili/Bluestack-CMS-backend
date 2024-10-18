package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;
import com.tfsla.templateManager.jsp.PageBuilder;

public class TfsPageBuilderControlsTag extends TagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8487641533215234536L;

	@Override
    public int doStartTag() throws JspException {
		
		PageBuilder pb  = (PageBuilder) pageContext.getRequest().getAttribute("pageBuilder");

		if (pb!=null)
			pb.printFooterHTML();		
		return SKIP_BODY;

	}

}
