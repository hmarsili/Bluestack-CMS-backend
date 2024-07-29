package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.opencms.file.CmsUser;
public class TfsUsuarioStateTag extends A_TfsUsuarioValueTag {
	private static final long serialVersionUID = 4413654015466156899L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		CmsUser user = getCurrentUser().getUser();
	    		String descripcion="";
	    		if (user!=null){
	    			String pending =(String) user.getAdditionalInfo("USER_PENDING");

	    			if (!user.isEnabled()) {
	    				descripcion = "Inactivo";
	    				
	    			}
	    			else if (pending !=null && pending.equals("true")) {
	    				descripcion = "Pendiente";
	    				
	    			}
	    			else {
	    				descripcion = "Activo";
	    				
	    			}
	    			
					pageContext.getOut().print(descripcion);
	    			
	    		}
			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
