package org.opencms.file;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.utils.TfsAdminUserProvider;

public class SwitcherHelper {
	public static void Switch(CmsRequestContext context, CmsUser user, CmsProject project)
	{
		context.switchUser(user, project, user.getOuFqn());
	}
	
	public static CmsObject getUserCmsObject(CmsUUID uuid) throws CmsException
	{
		CmsObject cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
		CmsObject m_cloneCms = OpenCms.initCmsObject(cmsObject);
		
		m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
		
		m_cloneCms.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
		
		CmsUser user = m_cloneCms.readUser(uuid);
		
		Switch(m_cloneCms.getRequestContext(), user, cmsObject.readProject("Offline"));

		return m_cloneCms;
		
	}
}
