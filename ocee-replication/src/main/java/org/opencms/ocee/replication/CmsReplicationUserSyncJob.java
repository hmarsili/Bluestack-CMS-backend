package org.opencms.ocee.replication;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.CmsHtmlReport;
import org.opencms.scheduler.I_CmsScheduledJob;

public class CmsReplicationUserSyncJob implements I_CmsScheduledJob {
   private static final Log Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = CmsLog.getLog(CmsReplicationUserSyncJob.class);
   private static final String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = "action";
   private static final String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = "settings";
   private static final String Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = "mode";
   private static final String Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = "mail-to-user";
   public static boolean m_busy;

   public String launch(CmsObject cms, Map parameters) throws Exception {
      Date jobStart = new Date();
      CmsHtmlReport report = new CmsHtmlReport(cms.getRequestContext().getLocale(), (String)null);
      CmsReplicationUserSyncAction action = CmsReplicationUserSyncAction.ALL;
      if (parameters != null) {
         action = CmsReplicationUserSyncAction.valueOf((String)parameters.get("action"));
         if (action == null) {
            action = CmsReplicationUserSyncAction.ALL;
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.warn(Messages.get().getBundle().key("LOG_SYNC_WRONG_PARAMETER_2", "action", action));
         }
      }

      CmsReplicationUserSettings settings = null;
      if (parameters != null) {
         String settingsName = (String)parameters.get("settings");
         if (settingsName != null) {
            settings = CmsReplicationManager.getInstance().getConfiguration().getUserReplication(settingsName);
            if (settings == null) {
               Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.warn(Messages.get().getBundle().key("LOG_SYNC_WRONG_PARAMETER_2", "settings", settingsName));
            }
         }
      }

      CmsReplicationUserSyncMode mode = CmsReplicationUserSyncMode.DISCARD;
      if (parameters != null) {
         mode = CmsReplicationUserSyncMode.valueOf((String)parameters.get("mode"));
         if (mode == null) {
            mode = CmsReplicationUserSyncMode.DISCARD;
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.warn(Messages.get().getBundle().key("LOG_SYNC_WRONG_PARAMETER_2", "mode", mode));
         }
      }

      List userData = null;
      if (!m_busy) {
         try {
            m_busy = true;
            if (settings == null) {
               userData = CmsReplicationManager.getInstance().getSynchronizeUsers(cms, action);
            } else {
               userData = CmsReplicationManager.getInstance().getSynchronizeUsers(cms, settings, action);
            }

            if (userData.size() > 0) {
               List userSync = new ArrayList(userData.size());
               Iterator i$ = userData.iterator();

               while(i$.hasNext()) {
                  CmsReplicationUserData data = (CmsReplicationUserData)i$.next();
                  userSync.add(data.getSyncData());
               }

               CmsReplicationManager.getInstance().synchronizeUsers(cms, userSync, mode, report);
            }
         } catch (CmsException var24) {
            report.addError(var24);
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(var24.getLocalizedMessage(), var24);
         } finally {
            m_busy = false;
            if (parameters != null && (report.hasWarning() || report.hasError())) {
               try {
                  String mailToUser = (String)parameters.get("mail-to-user");
                  CmsUser user = cms.readUser(mailToUser);
                  CmsReplicationUserNotification notification = new CmsReplicationUserNotification(cms, user, report);
                  DateFormat df = DateFormat.getDateTimeInstance(3, 3);
                  notification.addMacro("jobStart", df.format(jobStart));
                  notification.send();
               } catch (Exception var23) {
                  Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().getBundle().key("LOG_REPLICATION_USER_SEND_NOTIFICATION_FAILED_0"), var23);
               }
            }

         }
      }

      Integer count = new Integer(0);
      if (userData != null) {
         count = new Integer(userData.size());
      }

      return Messages.get().getBundle().key("LOG_SYNC_USERS_COUNT_1", count);
   }
}
