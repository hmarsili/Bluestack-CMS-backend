package org.opencms.ocee.replication;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDbContextFactory;
import org.opencms.file.CmsRequestContext;

public class CmsReplicationDbContextFactory implements I_CmsDbContextFactory {
   public CmsDbContext getDbContext() {
      return new CmsReplicationDbContext();
   }

   public CmsDbContext getDbContext(CmsRequestContext context) {
      return new CmsReplicationDbContext(context);
   }

   public void initialize(CmsDriverManager driverManager) {
   }
}
