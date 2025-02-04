package org.opencms.ocee.replication;

import org.opencms.db.CmsDbContext;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsRequestContext;
import org.opencms.util.CmsUUID;

public class CmsReplicationDbContext extends CmsDbContext {
   public CmsReplicationDbContext() {
   }

   public CmsReplicationDbContext(CmsRequestContext context) {
      super(context);
   }

   public I_CmsHistoryDriver getHistoryDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? null : server.getHistoryDriver();
   }

   public I_CmsProjectDriver getProjectDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? null : server.getProjectDriver();
   }

   public I_CmsUserDriver getUserDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? null : server.getUserDriver();
   }

   public I_CmsVfsDriver getVfsDriver(CmsUUID projectId) {
      CmsReplicationServer server = CmsReplicationManager.getServerByProject(projectId);
      return server == null ? null : server.getVfsDriver();
   }
}
