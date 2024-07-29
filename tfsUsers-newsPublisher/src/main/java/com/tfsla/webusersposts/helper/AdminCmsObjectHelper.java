package com.tfsla.webusersposts.helper;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;


public class AdminCmsObjectHelper {
	public static CmsObject getAdminCmsObject() throws CmsException {
		CmsObject cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
		return OpenCms.initCmsObject(cmsObject);
	}
}
