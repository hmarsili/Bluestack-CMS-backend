package org.opencms.ocee.replication;

import org.opencms.main.CmsEvent;
import org.opencms.ocee.cluster.CmsClusterEventHandler;

public class CmsReplicationClusterEventHandler extends CmsClusterEventHandler {
   public void cmsEvent(CmsEvent event) {
      this.handleClusterEvent(event);
   }
}
