package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsUser;
public class TfsUserIdTag extends A_TfsUsuarioValueTag {


	private static final long serialVersionUID = 7223625152231728226L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		CmsUser user = getCurrentUser().getUser();
	    		if (user!=null)
	    			pageContext.getOut().print(user.getId().toString());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
