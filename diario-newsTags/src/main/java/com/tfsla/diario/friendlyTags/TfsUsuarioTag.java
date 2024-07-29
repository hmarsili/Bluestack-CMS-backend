package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import com.tfsla.diario.model.TfsUsuario;
import com.tfsla.opencms.webusers.RegistrationModule;

public class TfsUsuarioTag extends BaseTag implements I_TfsUser  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5858499398845594510L;
	protected static final Log LOG = CmsLog.getLog(TfsUsuarioTag.class);
	private CmsUser user = null;
	private String username="";
	private String nickname="";
	

	private TfsUsuario previousUsuario = null;
	
	public TfsUsuarioTag()
	{
		user=null;
		
	}

    @Override
	public int doStartTag() throws JspException {
    	
    	super.init();
    	
	    saveUsuario();
	    
	    if (previousUsuario!=null){ 
    		username= previousUsuario.getId();
	    }
	    
	    if (nickname!=null && !nickname.trim().equals(""))
			try {
				username=RegistrationModule.getInstance(m_cms).UserNameByAdditionalInfo(m_cms, "APODO", nickname,true);
			} catch (CmsException e1) {	
				e1.printStackTrace();
			}
	    user = null;
	    if (username==null || username.trim().equals(""))
    	{
    		
    		user = m_cms.getRequestContext().currentUser();
    		
    		
    	} else
			try {
				user = m_cms.readUser(username);
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
	    if (user==null)
	    	return SKIP_BODY;
	    
    	exposeUsuario(user);
    	
		return EVAL_BODY_INCLUDE; //SKIP_BODY;		

    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public CmsUser getUser() {
		
		return user;
		
	}
    
	
    protected void exposeUsuario(CmsUser user)
    {
    	TfsUsuario tfsUser = new TfsUsuario(user,m_cms);
		pageContext.getRequest().setAttribute("ntuser", tfsUser);

    }
    
    protected void restoreUsuario()
    {
    	pageContext.getRequest().setAttribute("ntuser", previousUsuario );
    }

	protected void saveUsuario()
    {
		previousUsuario = (TfsUsuario) pageContext.getRequest().getAttribute("ntuser");
    	pageContext.getRequest().setAttribute("ntuser",null);
    }

	@Override
	public int doEndTag() throws JspException {
		restoreUsuario();
		
		return super.doEndTag();
	}

	

}
