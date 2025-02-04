package org.opencms.ocee.db.transaction;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDbContextFactory;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;

public class CmsTransactionDbContextFactory implements I_CmsDbContextFactory {
   private CmsDriverManager o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public CmsDbContext getDbContext() {
      CmsTransactionManager manager = CmsTransactionManager.getInstance();
      return (CmsDbContext)(manager != null ? this.getTransactionDbContext() : new CmsDbContext());
   }

   public CmsDbContext getDbContext(CmsRequestContext context) {
      CmsTransactionManager manager = CmsTransactionManager.getInstance();
      return (CmsDbContext)(manager != null ? this.getTransactionDbContext(context) : new CmsDbContext(context));
   }

   public CmsTransactionDbContext getTransactionDbContext() {
      return new CmsTransactionDbContext(new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, (CmsProject)null));
   }

   public CmsTransactionDbContext getTransactionDbContext(CmsProject project) {
      CmsTransactionDbContext dbc = new CmsTransactionDbContext(new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, project));
      dbc.setProjectId(project.getUuid());
      return dbc;
   }

   public CmsTransactionDbContext getTransactionDbContext(CmsRequestContext context) {
      return new CmsTransactionDbContext(context, new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, (CmsProject)null));
   }

   public CmsTransactionDbContext getTransactionDbContext(CmsRequestContext context, CmsProject project) {
      CmsTransactionDbContext dbc = new CmsTransactionDbContext(context, new CmsTransaction(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, project));
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
