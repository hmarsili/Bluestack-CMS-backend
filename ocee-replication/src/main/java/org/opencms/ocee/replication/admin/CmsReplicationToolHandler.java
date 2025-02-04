package org.opencms.ocee.replication.admin;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.security.CmsRole;
import org.opencms.workplace.tools.A_CmsToolHandler;

public class CmsReplicationToolHandler extends A_CmsToolHandler {
   private static final Log Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = CmsLog.getLog(CmsReplicationToolHandler.class);
   private static CmsGroup o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public String getDisabledHelpText() {
      return CmsReplicationManager.getInstance() == null ? CmsOceeManager.getInstance().getDefaultHelpText() : "${key.GUI_REPLICATION_DISABLED_NO_CONFIG_0}";
   }

   public boolean isEnabled(CmsObject cms) {
      return CmsReplicationManager.getInstance() != null && CmsReplicationManager.getInstance().isInitialized();
   }

   public boolean isVisible(CmsObject cms) {
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null && manager.isInitialized()) {
         if (this.getPath().equals("/ocee-replication/users") && manager.isInitialized() && manager.getConfiguration().getUserReplications().size() == 0) {
            return false;
         } else if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN)) {
            return true;
         } else {
            if (manager.getConfiguration().getManagersGroup() != null) {
               try {
                  if (o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super == null) {
                     o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = cms.readGroup(manager.getConfiguration().getManagersGroup());
                  }

                  return cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName(), false).contains(o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super);
               } catch (CmsException var4) {
                  Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(var4.getLocalizedMessage(), var4);
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }
}
