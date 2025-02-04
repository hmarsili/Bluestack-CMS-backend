package org.opencms.ocee.replication;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDbContextFactory;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.ocee.db.transaction.CmsTransaction;
import org.opencms.ocee.db.transaction.CmsTransactionDbContext;
import org.opencms.ocee.db.transaction.CmsTransactionManager;

public class CmsReplicationTransactionDbContextFactory implements I_CmsDbContextFactory {
   private CmsDriverManager o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public CmsDbContext getDbContext() {
      CmsTransactionManager manager = CmsTransactionManager.getInstance();
      CmsDbContext result = null;
      if (manager != null) {
         result = this.getTransactionDbContext();
      } else {
         result = new CmsDbContext();
      }

      return (CmsDbContext)result;
   }

   public CmsDbContext getDbContext(CmsRequestContext context) {
      CmsTransactionManager manager = CmsTransactionManager.getInstance();
      CmsDbContext result = null;
      if (manager != null) {
         result = this.getTransactionDbContext(context);
      } else {
         result = new CmsDbContext(context);
      }

      return (CmsDbContext)result;
   }

   public CmsTransactionDbContext getTransactionDbContext() {
      CmsTransactionDbContext result = new CmsReplicationTransactionDbContext(new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, (CmsProject)null));
      return result;
   }

   public CmsTransactionDbContext getTransactionDbContext(CmsProject project) {
      CmsTransactionDbContext dbc = new CmsReplicationTransactionDbContext(new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, project));
      dbc.setProjectId(project.getUuid());
      return dbc;
   }

   public CmsTransactionDbContext getTransactionDbContext(CmsRequestContext context) {
      CmsTransactionDbContext result = new CmsReplicationTransactionDbContext(context, new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, (CmsProject)null));
      return result;
   }

   public CmsTransactionDbContext getTransactionDbContext(CmsRequestContext context, CmsProject project) {
      CmsTransactionDbContext dbc = new CmsReplicationTransactionDbContext(context, new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, project));
      dbc.setProjectId(project.getUuid());
      return dbc;
   }

   public void initialize(CmsDriverManager driverManager) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = driverManager;
      CmsTransactionManager manager = CmsTransactionManager.getInstance();
      if (manager != null) {
         manager.initialize();
      }

   }
}
