package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.tfsla.templateManager.jsp.PageBuilder;

public class TfsPageTemplateTag extends BodyTagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7318203529984459667L;

	private PageBuilder pb=null;
	@Override
    public int doStartTag() throws JspException {
		
		pb  = (PageBuilder) pageContext.getRequest().getAttribute("pageBuilder");
		
		if (pb!=null)
			pb.printHeaderHTML();
		
		return EVAL_BODY_INCLUDE;
	}

	
	@Override
	public int doEndTag() throws JspException {

		if (pb!=null)
			pb.printFooterHTML();

		return super.doEndTag();
	}

}
