package org.opencms.ocee.cluster;

import java.util.Map;
import org.opencms.main.CmsEvent;

public interface I_CmsClusterEventHandler {
   void forwardEvent(CmsClusterServer var1, int var2, Map var3, Map var4);

   void handleClusterEvent(CmsEvent var1);

   boolean isFowardingEvent();
}
