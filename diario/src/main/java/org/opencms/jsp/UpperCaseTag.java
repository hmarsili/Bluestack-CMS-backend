package org.opencms.jsp;

import org.apache.commons.lang.StringUtils;

public class UpperCaseTag extends AbstractOpenCmsTag {

	public UpperCaseTag() {
		super();
	}
	
	@Override
	public int doStartTag() {
		this.getWriter().print(StringUtils.upperCase(this.getContent()));
		return SKIP_BODY;
	}

}
