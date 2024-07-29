package com.tfsla.utils;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;

public class CmsObjectUtils {
	
	private static final Log LOG = CmsLog.getLog(CmsObjectUtils.class);

	
	public static CmsObject getClone(CmsObject cmsObject){
		
		CmsObject m_cloneCms = null;
		try {
			m_cloneCms = OpenCms.initCmsObject(cmsObject);
			m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
	        
		} catch (CmsException e) {
			LOG.error("Error al intentar clonar el cmsObject",e);
		}
        
        return m_cloneCms;
	}
	
	public static CmsObject loginUser(CmsUser user) {
		CmsObject cmsObject = null;
		try {
			cmsObject = OpenCms.initCmsObject(new CmsDefaultUsers().getUserGuest());
			cmsObject.loginUser(user.getName());
			cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
		} catch (CmsException e) {
			cmsObject=null;
			LOG.error("Error al intentar loguearse con el usuario administrador",e);
		} 
		return cmsObject;
	} 

	public static CmsObject loginAsAdmin() {
		CmsObject cmsObject = null;
		try {
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			cmsObject = OpenCms.initCmsObject(_cmsObject);
			cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
		} catch (CmsException e) {
			cmsObject=null;
			LOG.error("Error al intentar loguearse con el usuario administrador",e);
		} 
		return cmsObject;
	}

}
