package org.opencms.ocee.replication;

import org.opencms.file.CmsUser;

public interface I_CmsReplicationUserSynchronization {
   CmsUser commit(CmsUser var1, CmsUser var2);

   boolean needsUpdate(CmsUser var1, CmsUser var2);

   CmsUser update(CmsUser var1, CmsUser var2);
}
