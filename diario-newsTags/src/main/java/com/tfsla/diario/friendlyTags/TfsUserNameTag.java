package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsUser;

public class TfsUserNameTag extends A_TfsUsuarioValueTag {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2804585833394542745L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		CmsUser user = getCurrentUser().getUser();
	    		if (user!=null)
	    			pageContext.getOut().print(user.getName());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
