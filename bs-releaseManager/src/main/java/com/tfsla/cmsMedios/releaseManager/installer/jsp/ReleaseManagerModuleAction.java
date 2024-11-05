package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsObject;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;

public class ReleaseManagerModuleAction extends A_CmsModuleAction {
	
	@Override
	public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {
		super.initialize(adminCms, configurationManager, module);
		
		try {
 			Messages.get().getBundle(adminCms.getRequestContext().getLocale()).getResourceBundle();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
	}
}