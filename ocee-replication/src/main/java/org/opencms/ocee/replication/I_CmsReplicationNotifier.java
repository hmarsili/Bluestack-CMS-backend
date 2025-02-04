package org.opencms.ocee.replication;

import org.opencms.file.CmsObject;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

public interface I_CmsReplicationNotifier {
   void beforeEventNotification(CmsObject var1, I_CmsReport var2, CmsReplicationServer var3, CmsUUID var4);

   void afterEventNotification(CmsObject var1, I_CmsReport var2, CmsReplicationServer var3, CmsUUID var4);
}
