package com.tfsla.diario.friendlyTags;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.tfsla.templateManager.jsp.PageBuilder;

public class TfsPageBuilderTag extends TagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2622181569492323222L;
	PageBuilder pb  = null;
	
	@Override
    public int doStartTag() throws JspException {
		
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();

		pb = new PageBuilder(pageContext, request, response);
		
		pageContext.getRequest().setAttribute("pageBuilder", pb);
		
		pb.printCssStyles();
		pb.printJS();
		
		return SKIP_BODY;
	}
	
}
