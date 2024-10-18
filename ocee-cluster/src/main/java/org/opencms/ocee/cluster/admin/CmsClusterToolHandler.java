package org.opencms.ocee.cluster.admin;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsToolHandler;

public class CmsClusterToolHandler extends A_CmsToolHandler {
   private static final Log LOG = CmsLog.getLog(CmsClusterToolHandler.class);
   private static CmsGroup m_managerGroup;
   private Set m_disabledPaths;

   public String getDisabledHelpText() {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager == null) {
         return CmsOceeManager.getInstance().getDefaultHelpText();
      } else if (!manager.isConfigured()) {
         return Messages.get().getBundle().key("GUI_CLUSTER_DISABLED_NO_CONFIG_0");
      } else if (!manager.isInitialized()) {
         return Messages.get().getBundle().key("GUI_CLUSTER_DISABLED_NO_INIT_0");
      } else if (manager.getThisServer() == null) {
         return Messages.get().getBundle().key("GUI_CLUSTER_DISABLED_BAD_CONFIG_0");
      } else if (manager.getThisServer().isWpServer() && this.getPath().equals("/ocee-cluster/wpserver")) {
         return Messages.get().getBundle().key("GUI_CLUSTER_DISABLED_ALREADY_ACTIVE_0");
      } else if (!manager.isOtherServerAccessible() && this.getDisabledPathsIfAlone().contains(this.getPath())) {
         return Messages.get().getBundle().key("GUI_CLUSTER_DISABLED_NO_OTHER_SERVER_0");
      } else {
         return (this.getPath().startsWith("/ocee-cluster/config/") || this.getPath().startsWith("/ocee-cluster/config-all/") || this.getPath().equals("/ocee-cluster/config") || this.getPath().startsWith("/ocee-cluster/config-all")) && !CmsOceeManager.getInstance().checkCoreVersion("7.0.5") ? "${key.GUI_CLUSTER_DISABLED_NO_COMP_0}" : Messages.get().getBundle().key("GUI_CLUSTER_DISABLED_NO_ACTIVE_0");
      }
   }

   public boolean isEnabled(CmsObject cms) {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager == null) {
         return false;
      } else if (!manager.isConfigured()) {
         return false;
      } else if (!this.getPath().equals("/ocee-cluster") && !this.getPath().equals("/ocee-cluster/refresh")) {
         if (!manager.isInitialized()) {
            return false;
         } else if (manager.getThisServer() == null) {
            return false;
         } else if ((this.getPath().startsWith("/ocee-cluster/config/") || this.getPath().startsWith("/ocee-cluster/config-all/") || this.getPath().equals("/ocee-cluster/config") || this.getPath().startsWith("/ocee-cluster/config-all")) && !CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            return false;
         } else if (manager.getThisServer().isWpServer() && this.getPath().equals("/ocee-cluster/wpserver")) {
            return false;
         } else {
            return manager.isOtherServerAccessible() || !this.getDisabledPathsIfAlone().contains(this.getPath());
         }
      } else {
         return true;
      }
   }

   public boolean isVisible(CmsObject cms) {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager != null && manager.isConfigured()) {
         if ((this.getPath().startsWith("/ocee-cluster/config/") || this.getPath().startsWith("/ocee-cluster/config-all/") || this.getPath().equals("/ocee-cluster/config") || this.getPath().startsWith("/ocee-cluster/config-all")) && !CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            return false;
         } else if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN)) {
            return true;
         } else {
            if (manager.getConfiguration().getManagersGroup() != null) {
               try {
                  if (m_managerGroup == null) {
                     m_managerGroup = cms.readGroup(manager.getConfiguration().getManagersGroup());
                  }

                  return cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName(), false).contains(m_managerGroup);
               } catch (CmsException var4) {
                  LOG.error(var4.getLocalizedMessage(), var4);
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   public boolean isVisible(CmsWorkplace wp) {
      boolean visible = super.isVisible(wp);
      if (!visible) {
         return false;
      } else if (this.getPath().endsWith("/compare")) {
         return !CmsClusterManager.getInstance().getWpServer().getName().equals(wp.getJsp().getRequest().getParameter("server"));
      } else {
         return visible;
      }
   }

   protected Set getDisabledPathsIfAlone() {
      if (this.m_disabledPaths == null) {
         this.m_disabledPaths = new HashSet();
         this.m_disabledPaths.add("/ocee-cluster/config-check");
         this.m_disabledPaths.add("/ocee-cluster/export");
         this.m_disabledPaths.add("/ocee-cluster/searchindex-rebuild");
         this.m_disabledPaths.add("/ocee-cluster/license_info");
         this.m_disabledPaths.add("/ocee-cluster/config-all");
      }

      return this.m_disabledPaths;
   }
}
