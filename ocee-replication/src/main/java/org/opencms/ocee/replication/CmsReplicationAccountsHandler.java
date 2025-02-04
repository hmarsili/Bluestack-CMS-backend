package org.opencms.ocee.replication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;

public class CmsReplicationAccountsHandler extends A_CmsReplicationHandler {
   private static final String[] oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do = new String[]{"users and groups", "CMS_GROUPS", "CMS_GROUPUSERS", "CMS_USERS", "CMS_USERDATA"};
   private static final CmsMessageContainer ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if = Messages.get().container("GUI_REPLICATION_HANDLER_ACCOUNTS_DETAILS_ACCOUNTS_HELP_0");
   private static final CmsMessageContainer õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int = Messages.get().container("GUI_REPLICATION_HANDLER_ACCOUNTS_DETAILS_ACCOUNTS_NAME_0");
   private static final CmsMessageContainer ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public = Messages.get().container("GUI_REPLICATION_HANDLER_ACCOUNTS_HELP_0");
   private static final CmsMessageContainer ÒO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while = Messages.get().container("GUI_REPLICATION_HANDLER_ACCOUNTS_NAME_0");
   private static final Log ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float = CmsLog.getLog(CmsReplicationAccountsHandler.class);
   private static final String OO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000for = "accounts";
   private static final List ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private = Collections.unmodifiableList(Arrays.asList("accounts"));

   public void cleanUp(CmsObject cms, CmsDbContext dbc, I_CmsReport report, CmsReplicationServer server, List publishedResources) throws CmsDataAccessException {
      this.initStats(cms);

      try {
         this.deleteDbTables(report, oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do, server);
         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(dbc, report, server, true);
         this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(dbc, report, server, true);
         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(dbc, report, server, false);
         this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(dbc, report, server, false);
      } catch (CmsException var7) {
         throw new CmsReplicationException(var7.getMessageContainer(), var7);
      }

      this.setStatistics(Collections.singletonMap("accounts", Boolean.toString(true)));
   }

   public CmsMessageContainer getHelpText() {
      return ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public;
   }

   public CmsMessageContainer getName() {
      return ÒO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while;
   }

   public CmsMessageContainer getStatDetailHelpText(String key) {
      return key.equals("accounts") ? ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if : null;
   }

   public List getStatDetailKeys() {
      return ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private;
   }

   public CmsMessageContainer getStatDetailName(String key) {
      return key.equals("accounts") ? õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int : null;
   }

   public boolean needResources() {
      return false;
   }

   public void replicate(CmsObject cms, CmsDbContext dbc, I_CmsReport report, CmsReplicationServer server, List publishedResources, boolean isFullReplication) {
   }

   private void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsDbContext dbc, I_CmsReport report, CmsReplicationServer server, boolean defGroups) throws CmsException {
      List alreadyReplicatedGroupIds = new ArrayList();
      I_CmsUserDriver userDriverSrc = CmsReplicationManager.getUserDriver(this.getDriverManager(), dbc);
      List allGroups = new ArrayList(userDriverSrc.getGroups(dbc, this.getDriverManager().readOrganizationalUnit(dbc, ""), true, false));
      Iterator itGroups = allGroups.iterator();

      while(true) {
         while(itGroups.hasNext()) {
            CmsGroup currentGroup = (CmsGroup)itGroups.next();
            if (!currentGroup.getId().equals(CmsOceeManager.LDAP_GROUP_ID) && (defGroups || !OpenCms.getDefaultUsers().isDefaultGroup(currentGroup.getName()))) {
               if (defGroups && !OpenCms.getDefaultUsers().isDefaultGroup(currentGroup.getName())) {
                  itGroups.remove();
               }
            } else {
               itGroups.remove();
               alreadyReplicatedGroupIds.add(currentGroup.getId());
            }
         }

         if (defGroups) {
            allGroups.addAll(userDriverSrc.getGroups(dbc, this.getDriverManager().readOrganizationalUnit(dbc, ""), true, true));
         }

         label196:
         while(!allGroups.isEmpty()) {
            int size = allGroups.size();
            Iterator i = allGroups.iterator();

            while(true) {
               CmsGroup currentGroup;
               String parentGroupName;
               while(true) {
                  if (!i.hasNext()) {
                     if (size != allGroups.size()) {
                        continue label196;
                     }

                     String remainingGroups = "";
                     Iterator it = allGroups.iterator();

                     while(it.hasNext()) {
                        CmsGroup group = (CmsGroup)it.next();
                        remainingGroups = remainingGroups + group.getName();
                        if (it.hasNext()) {
                           remainingGroups = remainingGroups + ", ";
                        }
                     }

                     report.println(Messages.get().container("ERR_ERROR_REPLICATING_GROUP_DEPENDENCY_2", remainingGroups, server.getName()), 5);
                     return;
                  }

                  currentGroup = (CmsGroup)i.next();
                  parentGroupName = null;
                  if (currentGroup.getParentId().isNullUUID()) {
                     break;
                  }

                  if (alreadyReplicatedGroupIds.contains(currentGroup.getParentId())) {
                     parentGroupName = userDriverSrc.readGroup(dbc, currentGroup.getParentId()).getName();
                     break;
                  }
               }

               report.print(org.opencms.report.Messages.get().container("RPT_SUCCESSION_1", server.getName()), 3);
               report.print(Messages.get().container("RPT_REPLICATE_GROUP_0"), 3);
               report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", currentGroup.getName()));
               report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.getDriverManager(), remoteDbc).createGroup(remoteDbc, currentGroup.getId(), currentGroup.getName(), currentGroup.getDescription(), currentGroup.getFlags(), parentGroupName);
                  report.println(org.opencms.report.Messages.get().container("RPT_OK_0"), 4);
               } catch (CmsDataAccessException var19) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_GROUP_2", currentGroup.getName(), server.getName());
                  if (ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float.isWarnEnabled()) {
                     ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float.warn(message.key(), var19);
                  }

                  report.println(org.opencms.report.Messages.get().container("RPT_FAILED_0"), 5);
                  if (remoteDbc != null) {
                     remoteDbc.report(report, message, var19);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }

               alreadyReplicatedGroupIds.add(currentGroup.getId());
               i.remove();
            }
         }

         return;
      }
   }

   private void Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(CmsDbContext dbc, I_CmsReport report, CmsReplicationServer server, boolean defUsers) throws CmsException {
      I_CmsUserDriver userDriver = CmsReplicationManager.getUserDriver(this.getDriverManager(), dbc);
      CmsOrganizationalUnit rootOu = userDriver.readOrganizationalUnit(dbc, "");
      List allUsers = userDriver.getUsers(dbc, rootOu, true);
      Iterator j = allUsers.iterator();

      while(true) {
         CmsUser currentUser;
         do {
            do {
               if (!j.hasNext()) {
                  return;
               }

               currentUser = (CmsUser)j.next();
            } while(defUsers && !OpenCms.getDefaultUsers().isDefaultUser(currentUser.getName()));
         } while(!defUsers && OpenCms.getDefaultUsers().isDefaultUser(currentUser.getName()));

         report.print(org.opencms.report.Messages.get().container("RPT_SUCCESSION_1", server.getName()), 3);
         report.print(Messages.get().container("RPT_REPLICATE_USER_0"), 3);
         report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", currentUser.getName()));
         report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
         CmsDbContext remoteDbc = null;

         try {
            remoteDbc = server.getDbContext(dbc.getRequestContext());
            I_CmsUserDriver userDriverDest = CmsReplicationManager.getUserDriver(this.getDriverManager(), remoteDbc);
            userDriverDest.createUser(remoteDbc, currentUser.getId(), currentUser.getName(), currentUser.getPassword(), currentUser.getFirstname(), currentUser.getLastname(), currentUser.getEmail(), 0L, currentUser.getFlags(), currentUser.getDateCreated(), currentUser.getAdditionalInfo());
            List groupsOfUser = userDriver.readGroupsOfUser(dbc, currentUser.getId(), "", true, (String)null, false);
            groupsOfUser.addAll(userDriver.readGroupsOfUser(dbc, currentUser.getId(), "", true, (String)null, true));
            Iterator k = groupsOfUser.iterator();

            while(k.hasNext()) {
               CmsGroup currentGroup = (CmsGroup)k.next();
               userDriverDest.createUserInGroup(remoteDbc, currentUser.getId(), currentGroup.getId());
            }

            report.println(org.opencms.report.Messages.get().container("RPT_OK_0"), 4);
         } catch (CmsDataAccessException var18) {
            CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_USER_2", currentUser.getName(), server.getName());
            if (ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float.isWarnEnabled()) {
               ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float.warn(message.key(), var18);
            }

            report.println(org.opencms.report.Messages.get().container("RPT_FAILED_0"), 5);
            if (remoteDbc != null) {
               remoteDbc.report(report, message, var18);
            }
         } finally {
            if (remoteDbc != null) {
               remoteDbc.clear();
            }

         }
      }
   }
}
