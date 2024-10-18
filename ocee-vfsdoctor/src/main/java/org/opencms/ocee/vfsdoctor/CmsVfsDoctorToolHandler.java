package org.opencms.ocee.vfsdoctor;

import java.util.HashMap;
import java.util.Map;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsToolHandler;

public class CmsVfsDoctorToolHandler extends A_CmsToolHandler {
   public String getDisabledHelpText() {
      return CmsVfsDoctorManager.getInstance() == null ? CmsOceeManager.getInstance().getDefaultHelpText() : "${key.ERR_VFSDOCTOR_MANAGER_NOT_INIT_0}";
   }

   public boolean isEnabled(CmsObject cms) {
      return CmsVfsDoctorManager.getInstance() != null && CmsVfsDoctorManager.getInstance().isInitialized();
   }

   public boolean isVisible(CmsObject cms) {
      return OpenCms.getRoleManager().hasRole(cms, CmsRole.VFS_MANAGER);
   }

   public Map getParameters(CmsWorkplace wp) {
      Map params = new HashMap(super.getParameters(wp));
      if (this.getPath().equals("/ocee-vfsdoctor/rfs-browser")) {
         params.put("dir", CmsVfsDoctorManager.getInstance().getRfsBrowser().getInitialDir());
      }

      return params;
   }
}
