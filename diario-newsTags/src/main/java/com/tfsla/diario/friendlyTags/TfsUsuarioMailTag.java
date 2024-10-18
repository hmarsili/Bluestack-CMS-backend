package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsUser;

public class TfsUsuarioMailTag extends A_TfsUsuarioValueTag {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -5649561770620253619L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		CmsUser user = getCurrentUser().getUser();
	    		if (user!=null)
	    			pageContext.getOut().print(user.getEmail());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
