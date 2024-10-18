package org.opencms.ocee.cluster;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsObject;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;

public class CmsClusterModuleAction extends A_CmsModuleAction {
   public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {
      super.initialize(adminCms, configurationManager, module);
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager != null) {
         manager.initConnectionRetryJob(adminCms);
      }

   }
}
