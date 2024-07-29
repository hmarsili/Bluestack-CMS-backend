package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsUser;

public class TfsUsuarioOuTag extends A_TfsUsuarioValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5822037194838629668L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		CmsUser user = getCurrentUser().getUser();
	    		if (user!=null)
	    			pageContext.getOut().print(user.getOuFqn());

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
