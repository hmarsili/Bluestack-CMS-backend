package org.opencms.ocee.cluster;

import java.util.Map;
import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

public class CmsClusterRetryConnectionJob implements I_CmsScheduledJob {
   public String launch(CmsObject cms, Map parameters) throws Exception {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager != null) {
         manager.retryForwardingEvents();
      }

      return "DONE";
   }
}
