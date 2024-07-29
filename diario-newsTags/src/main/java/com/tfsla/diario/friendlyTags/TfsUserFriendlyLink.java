package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

public class TfsUserFriendlyLink extends A_TfsUsuarioValueTag {
	
	private static final long serialVersionUID = -4605992760516801115L;

	static final String reg = "['\"!#$%&()*+./:;<=>?@^`~-]";
	
	public TfsUserFriendlyLink(){
		urlpart = null;
	}
	
	@Override
    public int doStartTag() throws JspException {
		
    	try {
    		CmsUser user = getCurrentUser().getUser();
    		if (user!=null){
    		String apodo = (String) user.getAdditionalInfo("APODO");
    		
    		String ou = user.getOuFqn();
    		String urlFriendly = null;
    		
    		    		
    		if(ou.equals("")){
    			if(urlpart!=null) 
    				urlFriendly = urlpart+user.getName();
    			else
    			    urlFriendly = "/staff/"+user.getName();
    		}else{
    			
    			try{   
	    			apodo = apodo.replaceAll(reg, "");
	        		apodo = apodo.replaceAll(" ", "-");
    			}catch(Exception e){
    				e.printStackTrace();
    			}
        		if(urlpart!=null) 
        			urlFriendly = urlpart+apodo;
        		else
    			    urlFriendly = "/u/"+apodo;
    		}    		
    			pageContext.getOut().print(urlFriendly);
    		}

		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }
	
	private String urlpart ="";
	
	public String getUrlpart() {
		return urlpart;
	}

	public void setUrlpart(String urlpart) {
		this.urlpart = urlpart;
	}

}