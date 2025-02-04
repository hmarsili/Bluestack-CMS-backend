package org.opencms.ocee.db.transaction;

import org.opencms.file.CmsObject;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.workplace.tools.CmsOnlyAdminToolHandler;

public class CmsTransactionToolHandler extends CmsOnlyAdminToolHandler {
   public String getDisabledHelpText() {
      return CmsTransactionManager.getInstance() == null ? CmsOceeManager.getInstance().getDefaultHelpText() : "${key.GUI_TRANSACTION_TOOL_DISABLED_0}";
   }

   public boolean isEnabled(CmsObject cms) {
      CmsTransactionManager manager = CmsTransactionManager.getInstance();
      if (manager == null) {
         return false;
      } else {
         return this.getPath().equals("/ocee-transaction/statistics") ? manager.isInitialized() : true;
      }
   }
}
