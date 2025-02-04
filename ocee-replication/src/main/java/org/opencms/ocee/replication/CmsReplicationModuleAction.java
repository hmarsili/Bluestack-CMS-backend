package org.opencms.ocee.replication;

import java.util.List;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

public class CmsReplicationModuleAction implements I_CmsModuleAction {
   public static final String MODULE_PARAMETER_EVENTCLASSES = "eventclasses";
   private static final Log Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = CmsLog.getLog(CmsReplicationModuleAction.class);
   private CmsObject o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public void cmsEvent(CmsEvent event) {
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.getRequestContext().setRequestTime(System.currentTimeMillis());
      if (manager != null) {
         I_CmsReport report = (I_CmsReport)event.getData().get("report");
         CmsDbContext dbc = (CmsDbContext)event.getData().get("dbContext");
         if (event.getType() == 2 && dbc == null) {
            if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
               Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().getBundle().key("ERR_DATABASE_CONTEXT_MISSING_0"));
            }

         } else {
            if (report == null) {
               if (dbc != null && dbc.getRequestContext() != null) {
                  report = new CmsLogReport(dbc.getRequestContext().getLocale(), this.getClass());
               } else {
                  report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), this.getClass());
               }
            }

            if (!manager.isInitialized()) {
               if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
                  Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().getBundle().key("ERR_MANAGER_NOT_INITIALIZED_0"));
               }

               ((I_CmsReport)report).println(Messages.get().container("ERR_MANAGER_NOT_INITIALIZED_0"), 5);
            } else {
               switch(event.getType()) {
               case 2:
                  CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get("publishHistoryId"));
                  manager.handleAfterPublishReplication(dbc, this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, (I_CmsReport)report, publishHistoryId);
                  manager.queuePublishHistory(dbc, publishHistoryId, (I_CmsReport)report);
               default:
               }
            }
         }
      }
   }

   public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null) {
         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = adminCms;
         if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(module.getParameter("eventclasses"))) {
            List evClasses = CmsStringUtil.splitAsList(module.getParameter("eventclasses"), ',');

            for(int i = evClasses.size() - 1; i >= 0; --i) {
               String evClassName = (String)evClasses.get(i);

               try {
                  I_CmsEventListener listener = (I_CmsEventListener)Class.forName(evClassName).newInstance();
                  OpenCms.addCmsEventListener(listener, new int[]{2131, 2132});
               } catch (Exception var9) {
                  if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
                     Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().container("ERR_ADDITIONAL_EVENTHANDLER_INVALID_1", evClassName));
                  }
               }
            }
         }

         if (manager.isInitialized()) {
            OpenCms.addCmsEventListener(this, new int[]{2});
         } else if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().container("ERR_MANAGER_NOT_INITIALIZED_0").key());
         }

      }
   }

   public void moduleUninstall(CmsModule module) {
   }

   public void moduleUpdate(CmsModule module) {
   }

   public void publishProject(CmsObject cms, CmsPublishList publishList, int backupTagId, I_CmsReport report) {
   }

   public void shutDown(CmsModule module) {
   }
}
