package com.tfsla.diario.admin.jsp;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.opencms.flex.CmsFlexController;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import com.tfsla.diario.auditActions.Messages;

public class TfsMessages {
		
	private Locale m_locale;
	private CmsWorkplaceSettings settings = null;
	private HttpSession m_session;
	
	public TfsMessages(PageContext context, HttpServletRequest req, HttpServletResponse res)
    {
    	m_session = req.getSession();
        settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        CmsFlexController m_controller = CmsFlexController.getController(req);
        
        if (settings==null)  {
        	settings = CmsWorkplace.initWorkplaceSettings( m_controller.getCmsObject(), settings, true);
        	m_session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, settings);
        }
        
    	m_locale = settings.getUserSettings().getLocale();
    }
	
	public Locale GetLocale(){
	    return m_locale;	
	}
	
	public void setLocale(Locale locale){
		this.m_locale = locale;
	}
	
   public String key(String key) {
	   
		try {
			String msg = com.tfsla.utils.StringEncoding.fixEncoding(Messages.get().getBundle(this.m_locale).key(key));
			
			return msg;
			//return new String(Messages.get().getBundle(this.m_locale).key(key).getBytes("iso-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Messages.get().getBundle(this.m_locale).key(key);
   }
   
   public String keyDefault(String keyName, String defaultValue) {
	   
	   try {
		   String msg = com.tfsla.utils.StringEncoding.fixEncoding(Messages.get().getBundle(this.m_locale).keyDefault(keyName,defaultValue));
			
		   return msg;
			//return new String(Messages.get().getBundle(this.m_locale).keyDefault(keyName,defaultValue).getBytes("iso-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Messages.get().getBundle(this.m_locale).keyDefault(keyName,defaultValue);
	   
   }
	
	
}
