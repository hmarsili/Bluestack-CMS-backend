package org.opencms.ocee.cluster;

import org.opencms.monitor.CmsMemoryMonitor;

public final class CmsClusterMemoryMonitor extends CmsMemoryMonitor {
   public boolean requiresPersistency() {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager == null) {
         return false;
      } else {
         if (!manager.isInitialized()) {
            manager.reInitializeCluster();
         }

         return manager.getThisServer() != null && manager.getThisServer().isWpServer();
      }
   }
}
