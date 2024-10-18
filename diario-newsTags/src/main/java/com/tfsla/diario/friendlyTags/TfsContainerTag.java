package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.tfsla.templateManager.jsp.PageBuilder;

public class TfsContainerTag  extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1636382826286720384L;

	private String name = null;
	
	@Override
    public int doStartTag() throws JspException {
		
		PageBuilder pb  = (PageBuilder) pageContext.getRequest().getAttribute("pageBuilder");
		
		if (pb!=null)
			pb.printContainerHTML(name);

		return SKIP_BODY;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
