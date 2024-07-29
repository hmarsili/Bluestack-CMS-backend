package org.opencms.file;

import java.lang.reflect.Field;

import org.opencms.db.CmsSecurityManager;

/**
 * Para otener una connection a la DB, se necesitan atributos que están en CMSObject
 * 
 * Esta clase esta en el mismo package que un CMSObject,  
 * De esta forma puede acceder a sus miembros protected
 * 
 * Oh si! violaré el encapsulamiento! mbuajajaja
 * 
 * 
 * @author lgassman
 *
 */
public class CMSObjectAccesor {

	public static CmsSecurityManager getSecurityManager(CmsObject object) {
		return object.m_securityManager;
	}
	
	public static void reallySetCurrentProject(CmsObject cmsObject, CmsProject project) {
		try {
			Field field  = cmsObject.getRequestContext().getClass().getDeclaredField("m_currentProject");
			field.setAccessible(true);
			field.set(cmsObject.getRequestContext(), project);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
	}

}
