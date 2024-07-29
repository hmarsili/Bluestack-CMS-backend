package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.opencms.file.CmsUser;

public class TfsUserFollowTag extends A_TfsUsuarioValueTag {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	private String style = "default";
		
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	
	@Override
    public int doStartTag() throws JspException {

	    CmsUser currentUser = getCurrentUser().getUser();        
        String content = "";        
        
    	try{
    		if(currentUser!=null){
        		content = "<div class=\"btnFollow\" style=\"display:none\" styleParam=\""+ style +"\" id=\"" + currentUser.getId().getStringValue() + "\">";
            	content += "</div >";	            	
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}        	
              
        try {
			pageContext.getOut().print(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return SKIP_BODY;
    }	
}
