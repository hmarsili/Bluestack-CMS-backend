package org.opencms.ocee.license;

import org.opencms.file.CmsObject;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.workplace.tools.CmsOnlyAdminToolHandler;

public class CmsLicenseToolHandler extends CmsOnlyAdminToolHandler {
   public String getDisabledHelpText() {
      return CmsLicenseManager.getInstance() == null ? CmsOceeManager.getInstance().getDefaultHelpText() : "${key.GUI_LICENSE_DISABLED_NO_CONFIG_0}";
   }

   public boolean isEnabled(CmsObject cms) {
      return CmsLicenseManager.getInstance() != null && CmsLicenseManager.getInstance().isInitialized();
   }
}
