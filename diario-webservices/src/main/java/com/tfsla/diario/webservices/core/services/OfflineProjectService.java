package com.tfsla.diario.webservices.core.services;

import javax.servlet.http.HttpServletRequest;

import org.opencms.main.CmsException;

import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.diario.webservices.core.services.TfsWebService;

/**
 * Represents a service that needs to be switched to the offline project to create VFS contents
 */
public abstract class OfflineProjectService extends TfsWebService {

	public OfflineProjectService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	/**
	 * Switch the current session to the offline project to create new contents
	 * @throws CmsException
	 */
	protected void switchToOfflineSession() throws CmsException {
		this.oldProjectName = SessionManager.getCurrentProject(cms);
		if(this.oldProjectName != null && !this.oldProjectName.equals(SessionManager.PROJECT_OFFLINE)) {
			SessionManager.switchToProject(cms, SessionManager.PROJECT_OFFLINE);
		}
		
		if(this.site != null && !this.site.equals("")) {
			this.oldSite = SessionManager.getSiteRoot(cms);
			if(!this.site.equals(this.oldSite))
				SessionManager.selectSiteRoot(cms, this.site);
		}
	}
	
	/**
	 * Restores the session context before switching to the offline project
	 * @throws CmsException
	 */
	protected void restoreSession() throws CmsException {
		if(this.oldProjectName != null && !this.oldProjectName.equals(SessionManager.PROJECT_OFFLINE))
			SessionManager.switchToProject(cms, this.oldProjectName);
		if(this.site != null && !this.site.equals("") && !this.site.equals(this.oldSite))
			SessionManager.selectSiteRoot(cms, this.oldSite);
	}
	
	protected String publication;
	protected String site;
	protected String oldSite;
	protected String oldProjectName;

}
