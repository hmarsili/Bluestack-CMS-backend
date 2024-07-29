package org.opencms.jsp;

import org.apache.commons.lang.StringUtils;

public class CapitalizeTag extends AbstractOpenCmsTag {

	public CapitalizeTag() {
		super();
	}
	
	@Override
	public int doStartTag() {
		this.getWriter().print(StringUtils.capitalize(this.getContent()));
		return SKIP_BODY;
	}

}
