package com.tfsla.diario.webservices.core;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.tfsla.diario.webservices.common.*;
import com.tfsla.diario.webservices.common.exceptions.*;
import com.tfsla.utils.TfsAdminUserProvider;

/**
 * Manages active web sessions
 */
public final class SessionManager {
	
	public final static String PROJECT_ONLINE = "Online";
	public final static String PROJECT_OFFLINE = "Offline";
	
	/**
	 * Returns the project for the current cms session
	 * @param cmsObject the CmsObject related to a session
	 * @return a String representing the project name
	 * @throws CmsException
	 */
	public synchronized static String getCurrentProject(CmsObject cmsObject) throws CmsException {
		CmsRequestContext cmsContext = cmsObject.getRequestContext();
		return cmsContext.currentProject().getName();
	}
	
	/**
	 * Selects the project within a cms session
	 * @param cmsObject the CmsObject where the session will be changed
	 * @param project the project to switch to
	 * @throws CmsException
	 */
	public synchronized static void switchToProject(CmsObject cmsObject, String project) throws CmsException {
		CmsRequestContext cmsContext = cmsObject.getRequestContext();
		CmsProject newProject = cmsObject.readProject(project);
		cmsContext.setCurrentProject(newProject);
	}
	
	/**
	 * Returns the site selected for the current cms session
	 * @param cmsObject the CmsObject related to a session
	 * @return a String representing the site root
	 * @throws CmsException
	 */
	public synchronized static String getSiteRoot(CmsObject cmsObject) throws CmsException {
		CmsRequestContext cmsContext = cmsObject.getRequestContext();
		return cmsContext.getSiteRoot();
	}
	
	/**
	 * Selects the site root within a cms session
	 * @param cmsObject the CmsObject where the session will be changed
	 * @param site the site to be selected
	 * @throws CmsException
	 */
	public synchronized static void selectSiteRoot(CmsObject cmsObject, String site) throws CmsException {
		CmsRequestContext cmsContext = cmsObject.getRequestContext();
		cmsContext.setSiteRoot(site);
	}
	
	/**
	 * Retrieves an active WebSession by having their previously issued token
	 * @param token the Token key to retrieve the session
	 * @return a WebSession instance representing the active session
	 * @throws TokenExpiredException 
	 * @throws InvalidTokenException 
	 */
	public synchronized static WebSession getSession(String token) throws TokenExpiredException, InvalidTokenException {
		Cache cache = WebServicesCacheManager.getCache();
		Element element = cache.get(token);
		if(element == null || element.getObjectValue() == null)
			throw new InvalidTokenException();
		if(element.isExpired())
			throw new TokenExpiredException();
		return (WebSession)element.getObjectValue();
	}
	
	/**
	 * Stores a new session into the session pool
	 * @param token an issued Token with the token key and the expiration time
	 * @param session a WebSession instance to be stored into the session pool
	 */
	public synchronized static void saveSession(Token token, WebSession session) {
		Cache cache = WebServicesCacheManager.getCache();
		Element element = new Element(token.getValue(), session);
		element.setTimeToLive((int)token.getDuration());
		element.setEternal(false);
		cache.put(element);
	}
	
	/**
	 * Retrieves a CmsObject instance with administrative privileges
	 * @return CmsObject instance with administrative privileges
	 * @throws CmsException
	 */
	public synchronized static CmsObject getAdminCmsObject() throws CmsException {
		return getAdminCmsObject(null);
	}
	
	/**
	 * Retrieves a CmsObject instance with administrative privileges and sets its current site root
	 * @param site the site to be used
	 * @return CmsObject instance with administrative privileges
	 * @throws CmsException
	 */
	public synchronized static CmsObject getAdminCmsObject(String site) throws CmsException {
		if(adminCmsObject == null) {
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			adminCmsObject = OpenCms.initCmsObject(_cmsObject);
		}
		if(site != null) {
			adminCmsObject.getRequestContext().setSiteRoot(site);
		}
		switchToProject(adminCmsObject, SessionManager.PROJECT_OFFLINE);
		return adminCmsObject;
	}
	
	private static CmsObject adminCmsObject;
}