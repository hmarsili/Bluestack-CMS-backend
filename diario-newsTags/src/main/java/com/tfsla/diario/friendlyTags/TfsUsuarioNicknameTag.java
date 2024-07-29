package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;

import com.tfsla.diario.model.TfsUsuario;

public class TfsUsuarioNicknameTag extends A_TfsUsuarioValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3200806906000457416L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		CmsUser user = getCurrentUser().getUser();
	    		//TfsUsuario  usuario = (TfsUsuario)pageContext.getRequest().getAttribute("ntuser");
	    		//CmsUser user = CmsFlexController.getCmsObject(pageContext.getRequest()).readUser(usuario.getId());
	    		if (user!=null)
	    			pageContext.getOut().print(user.getAdditionalInfo("APODO") );

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }
}
