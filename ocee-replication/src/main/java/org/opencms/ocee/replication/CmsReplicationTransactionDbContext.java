package org.opencms.ocee.replication;

import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsRequestContext;
import org.opencms.ocee.db.transaction.CmsTransaction;
import org.opencms.ocee.db.transaction.CmsTransactionDbContext;
import org.opencms.util.CmsUUID;

public class CmsReplicationTransactionDbContext extends CmsTransactionDbContext {
   protected CmsReplicationTransactionDbContext(CmsRequestContext context, CmsTransaction transaction) {
      super(context, transaction);
   }

   protected CmsReplicationTransactionDbContext(CmsTransaction transaction) {
      super(transaction);
   }

   public I_CmsHistoryDriver getHistoryDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? super.getHistoryDriver(projectId) : server.getHistoryDriver();
   }

   public I_CmsProjectDriver getProjectDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? super.getProjectDriver(projectId) : server.getProjectDriver();
   }

   public I_CmsUserDriver getUserDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? super.getUserDriver(projectId) : server.getUserDriver();
   }

   public I_CmsVfsDriver getVfsDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? super.getVfsDriver(projectId) : server.getVfsDriver();
   }
}
