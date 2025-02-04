package org.opencms.ocee.replication;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.db.generic.CmsSqlManager;
//import org.opencms.ocee.db.oracle.CmsSqlManager700;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsUUID;

public final class CmsReplicationManager {
   public static final String KEY_SERVER_PREFIX = "replication.server.";
   public static final Comparator PUBRES_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
         if (o1 instanceof CmsPublishedResource && o2 instanceof CmsPublishedResource) {
            CmsPublishedResource r1 = (CmsPublishedResource)o1;
            CmsPublishedResource r2 = (CmsPublishedResource)o2;
            return !r1.getRootPath().equals(r2.getRootPath()) ? r1.getRootPath().compareTo(r2.getRootPath()) : r1.getPublishTag() - r2.getPublishTag();
         } else {
            return 0;
         }
      }
   };
   public static final int STATE_REPLICATED = 256;
   private static final Log Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = CmsLog.getLog(CmsReplicationManager.class);
   private CmsReplicationConfiguration Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   private CmsDriverManager Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
   private boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   private boolean Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;

   public static I_CmsHistoryDriver getHistoryDriver(CmsDriverManager driverManager, CmsDbContext dbc) {
      return supportsMultiDbReplication() ? driverManager.getHistoryDriver(dbc) : driverManager.getHistoryDriver();
   }

   public static CmsReplicationManager getInstance() {
      try {
         return (CmsReplicationManager)CmsOceeManager.getInstance().getClassLoader().loadObject(CmsReplicationManager.class.getName());
      } catch (Exception var1) {
         return null;
      }
   }

   public static CmsSqlManager getOceeSqlManager(org.opencms.db.generic.CmsSqlManager sqlManagerObj) {
      CmsSqlManager sqlManager = null;
      //if (sqlManagerObj instanceof CmsSqlManager700) {
      //   sqlManager = ((CmsSqlManager700)sqlManagerObj).getSqlManager();
      //} else {
         sqlManager = (CmsSqlManager)sqlManagerObj;
      //}

      return (CmsSqlManager)sqlManager;
   }

   public static I_CmsProjectDriver getProjectDriver(CmsDriverManager driverManager, CmsDbContext dbc) {
      return supportsMultiDbReplication() ? driverManager.getProjectDriver(dbc) : driverManager.getProjectDriver();
   }

   public static I_CmsProjectDriver getProjectDriver(CmsDriverManager manager, CmsDbContext dbc, I_CmsProjectDriver defaultValue) {
      return supportsMultiDbReplication() ? manager.getProjectDriver(dbc, defaultValue) : defaultValue;
   }

   public static CmsReplicationServer getServerByProject(CmsUUID project) {
      CmsReplicationManager manager = getInstance();
      return manager == null ? null : manager.getConfiguration().getServerByProject(project);
   }

   public static I_CmsUserDriver getUserDriver(CmsDriverManager driverManager, CmsDbContext dbc) {
      return supportsMultiDbReplication() ? driverManager.getUserDriver(dbc) : driverManager.getUserDriver();
   }

   public static I_CmsUserDriver getUserDriver(CmsDriverManager manager, CmsDbContext dbc, I_CmsUserDriver defaultValue) {
      return supportsMultiDbReplication() ? manager.getUserDriver(dbc, defaultValue) : defaultValue;
   }

   public static I_CmsVfsDriver getVfsDriver(CmsDriverManager driverManager, CmsDbContext dbc) {
      return supportsMultiDbReplication() ? driverManager.getVfsDriver(dbc) : driverManager.getVfsDriver();
   }

   public static boolean publishedResourcesListContains(List publishedResources, String path) {
      Iterator it = publishedResources.iterator();

      CmsPublishedResource res;
      do {
         if (!it.hasNext()) {
            return false;
         }

         res = (CmsPublishedResource)it.next();
      } while(!res.getRootPath().equals(path));

      return true;
   }

   public static boolean publishedResourcesListContainsId(List<CmsPublishedResource> publishedResources, CmsUUID id) {
      Iterator i$ = publishedResources.iterator();

      CmsPublishedResource res;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         res = (CmsPublishedResource)i$.next();
      } while(!res.getStructureId().equals(id));

      return true;
   }

   public static boolean supportsMultiDbReplication() {
      return CmsOceeManager.getInstance().checkCoreVersion("7.5.3");
   }

   public CmsUser createReplicatedUser(CmsObject cms, CmsUser user, CmsReplicationUserSettings settings) throws CmsException {
      CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
      CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), onlineProject);
      CmsDbContext remoteDbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), settings.getReplicationProject());
      if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isDebugEnabled()) {
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.debug(Messages.get().getBundle().key("LOG_CREATING_REPLICATION_USER_REMOTE_2", user.getName(), settings.getServerName()));
      }

      CmsUser var10;
      try {
         List shouldHaveGroups = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(dbc, user.getName(), false);
         List shouldHaveRoles = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(dbc, user.getName(), true);
         CmsUser created = getUserDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, remoteDbc).createUser(remoteDbc, user.getId(), user.getName(), user.getPassword(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getLastlogin(), user.getFlags(), user.getDateCreated(), user.getAdditionalInfo());
         this.addUserToGroupsAndRoles(remoteDbc, created, shouldHaveGroups, shouldHaveRoles);
         var10 = created;
      } finally {
         remoteDbc.clear();
         dbc.clear();
      }

      return var10;
   }

   public CmsUser createUser(CmsObject cms, CmsUser user, CmsReplicationUserSettings settings) throws CmsException {
      CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
      CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), onlineProject);
      CmsDbContext remoteDbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), settings.getReplicationProject());
      if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isDebugEnabled()) {
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.debug(Messages.get().getBundle().key("LOG_CREATING_REPLICATION_USER_LOCAL_2", user.getName(), settings.getServerName()));
      }

      CmsUser var14;
      try {
         List shouldHaveGroups = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(remoteDbc, user.getName(), false);
         List shouldHaveRoles = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(remoteDbc, user.getName(), true);
         CmsUser created = getUserDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, dbc).createUser(dbc, user.getId(), user.getName(), user.getPassword(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getLastlogin(), user.getFlags(), user.getDateCreated(), user.getAdditionalInfo());
         if (CmsOceeManager.getInstance().checkCoreVersion("7.5.0")) {
            Map eventData = new HashMap();
            eventData.put("userId", user.getId().toString());
            eventData.put("userAction", "createUser");
            OpenCms.fireCmsEvent(new CmsEvent(29, eventData));
         }

         this.addUserToGroupsAndRoles(dbc, created, shouldHaveGroups, shouldHaveRoles);
         var14 = created;
      } finally {
         remoteDbc.clear();
         dbc.clear();
      }

      return var14;
   }

   public void deleteReplicatedUser(CmsObject cms, CmsUser user, CmsReplicationUserSettings settings) throws CmsException {
      CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), settings.getReplicationProject());

      try {
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.deleteUser(dbc, (CmsProject)null, user.getName(), (String)null);
      } finally {
         dbc.clear();
      }

   }

   public CmsReplicationConfiguration getConfiguration() {
      return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   }

   public CmsDriverManager getDriverManager() {
      return this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
   }

   public List<CmsPublishedResource> getReplicationResources(CmsObject cms, CmsUUID serverId) throws CmsException {
      if (serverId == null) {
         return new ArrayList();
      } else {
         CmsReplicationServer server = this.getConfiguration().getReplicationServer(serverId);
         if (server == null) {
            return new ArrayList();
         } else {
            List sortedRes = this.getSortedReplicationResources(cms, server);
            return sortedRes;
         }
      }
   }

   public List getReplicationResourcesFromPublishHistory(CmsDbContext dbc, CmsUUID serverId) throws CmsException {
      CmsUUID origProject = dbc.getProjectId();

      List var5;
      try {
         CmsReplicationServer server = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getReplicationServer(serverId);
         dbc.setProjectId(server.getOrgReplicationProject().getUuid());
         var5 = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.readPublishedResources(dbc, serverId);
      } finally {
         dbc.setProjectId(origProject);
      }

      return var5;
   }

   public Map<CmsPublishedResource, String> getReplicationResourcesWithHistories(CmsObject cms, CmsUUID serverId) throws CmsException {
      if (serverId == null) {
         return new HashMap();
      } else {
         CmsReplicationServer server = this.getConfiguration().getReplicationServer(serverId);
         if (server == null) {
            return new HashMap();
         } else {
            List sortedRes = this.getSortedReplicationResources(cms, server);
            Map ret = this.getPublishedResourceHistories(sortedRes);
            return ret;
         }
      }
   }

   public CmsUser getReplicationUser(CmsObject cms, String settingsName, CmsUUID userId) throws CmsException {
      if (settingsName == null) {
         return null;
      } else {
         CmsReplicationUserSettings settings = getInstance().getConfiguration().getUserReplication(settingsName);
         if (settings == null) {
            return null;
         } else {
            CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), settings.getReplicationProject());

            CmsUser var6;
            try {
               var6 = getUserDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, dbc).readUser(dbc, userId);
            } finally {
               dbc.clear();
            }

            return var6;
         }
      }
   }

   public List getReplicationUsers(CmsObject cms, CmsReplicationUserSettings settings, CmsProject project) throws CmsException {
      if (settings == null) {
         return new ArrayList();
      } else {
         Set result = new TreeSet();
         CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), project);

         try {
            if (project.getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
               dbc.setProjectId(CmsUUID.getNullUUID());
            }

            I_CmsUserDriver remoteUserDriver = getUserDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, dbc);
            if (settings.getGroups().isEmpty() && settings.getOrgUnits().isEmpty()) {
               CmsOrganizationalUnit orgUnit = remoteUserDriver.readOrganizationalUnit(dbc, "");
               result.addAll(remoteUserDriver.getUsers(dbc, orgUnit, true));
            } else {
               Iterator groupIter = settings.getGroups().iterator();

               while(groupIter.hasNext()) {
                  String groupName = (String)groupIter.next();
                  result.addAll(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getUsersOfGroup(dbc, groupName, true, false, false));
               }

               Iterator ouIter = settings.getOrgUnits().iterator();

               while(ouIter.hasNext()) {
                  String ou = (String)ouIter.next();
                  CmsOrganizationalUnit orgUnit = remoteUserDriver.readOrganizationalUnit(dbc, CmsOrganizationalUnit.removeLeadingSeparator(ou));
                  result.addAll(remoteUserDriver.getUsers(dbc, orgUnit, true));
               }
            }
         } finally {
            if (dbc != null) {
               dbc.clear();
            }

         }

         return new ArrayList(result);
      }
   }

   public List getSynchronizeUsers(CmsObject cms) throws CmsException {
      return this.getSynchronizeUsers(cms, CmsReplicationUserSyncAction.ALL);
   }

   public List getSynchronizeUsers(CmsObject cms, CmsReplicationUserSettings settings, CmsReplicationUserSyncAction action) throws CmsException {
      List syncUsers = new ArrayList();
      I_CmsReplicationUserSynchronization synch = this.getConfiguration().getUserSynchronizationHandler();
      List remoteUsers = this.getReplicationUsers(cms, settings, settings.getReplicationProject());
      List localUsers = this.getReplicationUsers(cms, settings, cms.readProject(CmsProject.ONLINE_PROJECT_ID));
      TreeSet users;
      Iterator changedIter;
      CmsUser changed;
      if (action == CmsReplicationUserSyncAction.ALL || action == CmsReplicationUserSyncAction.ADD || action == CmsReplicationUserSyncAction.ADDANDUPDATE) {
         users = new TreeSet(remoteUsers);
         users.removeAll(localUsers);
         changedIter = users.iterator();

         while(changedIter.hasNext()) {
            changed = (CmsUser)changedIter.next();
            if (synch.needsUpdate((CmsUser)null, changed)) {
               syncUsers.add(new CmsReplicationUserData(changed, settings, CmsReplicationUserSyncAction.ADD));
            }
         }
      }

      if (action == CmsReplicationUserSyncAction.ALL || action == CmsReplicationUserSyncAction.DELETE) {
         users = new TreeSet(localUsers);
         users.removeAll(remoteUsers);
         changedIter = users.iterator();

         while(changedIter.hasNext()) {
            changed = (CmsUser)changedIter.next();
            if (synch.needsUpdate(changed, (CmsUser)null)) {
               syncUsers.add(new CmsReplicationUserData(changed, settings, CmsReplicationUserSyncAction.DELETE));
            }
         }
      }

      if (action == CmsReplicationUserSyncAction.ALL || action == CmsReplicationUserSyncAction.UPDATE || action == CmsReplicationUserSyncAction.ADDANDUPDATE) {
         users = new TreeSet(remoteUsers);
         users.retainAll(localUsers);
         changedIter = users.iterator();

         while(changedIter.hasNext()) {
            changed = (CmsUser)changedIter.next();
            CmsUser localUser = (CmsUser)localUsers.get(localUsers.indexOf(changed));
            if (synch.needsUpdate(localUser, changed)) {
               syncUsers.add(new CmsReplicationUserData(changed, settings, CmsReplicationUserSyncAction.UPDATE));
            }
         }
      }

      return syncUsers;
   }

   public List getSynchronizeUsers(CmsObject cms, CmsReplicationUserSyncAction action) throws CmsException {
      List result = new ArrayList();
      Iterator setIter = this.getConfiguration().getUserReplications().iterator();

      while(setIter.hasNext()) {
         CmsReplicationUserSettings settings = (CmsReplicationUserSettings)setIter.next();
         result.addAll(this.getSynchronizeUsers(cms, settings, action));
      }

      return result;
   }

   public void handleAfterPublishReplication(CmsDbContext dbc, CmsObject cms, I_CmsReport report, CmsUUID publishHistoryId) {
      if (!this.isInitialized()) {
         if (report != null) {
            report.println(Messages.get().container("ERR_MANAGER_NOT_INITIALIZED_0"), 5);
         }

      } else {
         try {
            List publishedResources = getProjectDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, dbc).readPublishedResources(dbc, publishHistoryId);
            List serverIds = new ArrayList();
            Iterator it = this.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (server.getMode() == CmsReplicationMode.AUTO && "opencms:default".equals(server.getOrgServerPoolUrl())) {
                  serverIds.add(server.getServerId());
               }
            }

            if (!serverIds.isEmpty() && !publishedResources.isEmpty()) {
               this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(dbc, cms, report, (List)null, serverIds, publishedResources, false);
            }
         } catch (CmsException var9) {
            CmsMessageContainer message = Messages.get().container("RPT_WRITE_DATA_FAILED_0");
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(message.key(), var9);
            report.println(message, 1);
            report.println(var9);
         }

      }
   }

   public void handleFullReplication(CmsDbContext dbc, CmsObject cms, I_CmsReport report, List handlerNames, String serverId, List resourceNames) {
      if (report == null) {
         if (dbc.getRequestContext() != null) {
            report = new CmsLogReport(dbc.getRequestContext().getLocale(), this.getClass());
         } else if (cms.getRequestContext() != null) {
            report = new CmsLogReport(cms.getRequestContext().getLocale(), this.getClass());
         } else {
            report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), this.getClass());
         }
      }

      if (!this.isInitialized()) {
         ((I_CmsReport)report).println(Messages.get().container("ERR_MANAGER_NOT_INITIALIZED_0"), 5);
      } else {
         try {
            List publishedResources = new ArrayList();
            Iterator itHandlers = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getReplicationHandlers().iterator();

            label239:
            while(true) {
               I_CmsReplicationHandler handler;
               do {
                  if (!itHandlers.hasNext()) {
                     break label239;
                  }

                  handler = (I_CmsReplicationHandler)itHandlers.next();
               } while(handlerNames != null && !handlerNames.contains(handler.getClass().getName()));

               if (handler.needResources()) {
                  List resources = resourceNames;
                  if (resourceNames == null || resourceNames.isEmpty()) {
                     resources = Collections.singletonList("/");
                  }

                  CmsProject oldProject = null;

                  try {
                     oldProject = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.readProject(dbc, cms.getRequestContext().currentProject().getUuid());
                     cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                     publishedResources = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(dbc, (I_CmsReport)report, resources, serverId);
                     break;
                  } finally {
                     if (oldProject != null) {
                        cms.getRequestContext().setCurrentProject(oldProject);
                     }

                  }
               }
            }

            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.3")) {
               OpenCms.getPublishManager().disablePublishing();
            }

            List serverIds = new ArrayList();
            serverIds.add(new CmsUUID(serverId));
            this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(dbc, cms, (I_CmsReport)report, handlerNames, serverIds, (List)publishedResources, true);
         } catch (Exception var21) {
            CmsMessageContainer message = Messages.get().container("RPT_REPLICATION_FULL_FAILED_0");
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(message.key(), var21);
            ((I_CmsReport)report).println(message, 1);
            ((I_CmsReport)report).println(var21);
         } finally {
            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.3")) {
               OpenCms.getPublishManager().enablePublishing();
            }

         }

      }
   }

   public void handleIncrementalReplication(CmsDbContext dbc, CmsObject cms, I_CmsReport report, List<String> handlerNames, List<String> serverIds, List<String> resourceNames) {
      if (!this.isInitialized()) {
         if (report != null) {
            report.println(Messages.get().container("ERR_MANAGER_NOT_INITIALIZED_0"), 5);
         }

      } else {
         try {
            Iterator serverIter = serverIds.iterator();

            while(serverIter.hasNext()) {
               CmsUUID serverId = new CmsUUID((String)serverIter.next());
               List publishedResources = new ArrayList();
               List replRes = this.getReplicationResources(cms, serverId);
               replRes = this.filterIncrementalReplicationResources(replRes);
               Set moved = new HashSet();
               Iterator i$ = replRes.iterator();

               while(i$.hasNext()) {
                  CmsPublishedResource res = (CmsPublishedResource)i$.next();
                  if (res.isMoved()) {
                     moved.add(res.getStructureId());
                  }
               }

               Map histories = this.getPublishedResourceHistories(replRes);
               if (resourceNames == null) {
                  publishedResources.addAll(replRes);
               } else {
                  Iterator itPubRes = replRes.iterator();

                  while(itPubRes.hasNext()) {
                     CmsPublishedResource res = (CmsPublishedResource)itPubRes.next();
                     if (resourceNames.contains(res.getRootPath())) {
                        publishedResources.add(res);
                        itPubRes.remove();
                     }
                  }

                  List newRes = new ArrayList();
                  Iterator itRes = publishedResources.iterator();

                  label207:
                  while(true) {
                     CmsPublishedResource res;
                     String hist;
                     do {
                        if (!itRes.hasNext()) {
                           publishedResources.addAll(newRes);
                           break label207;
                        }

                        res = (CmsPublishedResource)itRes.next();
                        hist = (String)histories.get(res);
                        if (hist.endsWith("D") || moved.contains(res.getStructureId())) {
                           String path = res.getRootPath();
                           if (!path.endsWith("/")) {
                              path = path + "/";
                           }

                           Iterator itPubRes2 = replRes.iterator();

                           while(itPubRes2.hasNext()) {
                              CmsPublishedResource res2 = (CmsPublishedResource)itPubRes2.next();
                              if (res2.getRootPath().startsWith(path)) {
                                 newRes.add(res2);
                                 itPubRes2.remove();
                              }
                           }
                        }
                     } while(hist.endsWith("D"));

                     Iterator itPubRes2 = replRes.iterator();

                     while(itPubRes2.hasNext()) {
                        CmsPublishedResource res2 = (CmsPublishedResource)itPubRes2.next();
                        String path = res2.getRootPath();
                        if (!path.endsWith("/")) {
                           path = path + "/";
                        }

                        if (res.getRootPath().startsWith(path)) {
                           newRes.add(res2);
                        }
                     }
                  }
               }

               OpenCms.getPublishManager().disablePublishing();
               List servIds = new ArrayList();
               servIds.add(serverId);
               this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(dbc, cms, report, handlerNames, servIds, publishedResources, false);
            }
         } catch (Exception var24) {
            CmsMessageContainer message = Messages.get().container("RPT_REPLICATION_INCREMENTAL_FAILED_0");
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(message.key(), var24);
            report.println(message, 1);
            report.println(var24);
         } finally {
            OpenCms.getPublishManager().enablePublishing();
         }

      }
   }

   public void initConfiguration(CmsReplicationConfiguration configuration) {
      this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = configuration;
   }

   public void initialize(CmsDriverManager driverManager) {
      this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = driverManager;
      if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object == null && Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().getBundle().key("ERR_CONFIGURATION_MISSING_0"));
      }

      if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object != null) {
         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.setDriverManager(driverManager);
      }

      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object != null;
   }

   public boolean isConfigured() {
      return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object != null;
   }

   public boolean isInitialized() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public void lazyInitialization(CmsObject cms) {
      if (!this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String) {
         this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = true;
         if (cms != null) {
            Iterator itHandlers = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getReplicationHandlers().iterator();

            while(itHandlers.hasNext()) {
               I_CmsReplicationHandler handler = (I_CmsReplicationHandler)itHandlers.next();
               handler.initialize(cms, this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object, this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class);
            }
         }

         CmsDbContext dbc = new CmsDbContext();
         CmsProject onlineProject = null;

         try {
            onlineProject = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.readProject(dbc, CmsProject.ONLINE_PROJECT_ID);
         } catch (CmsDataAccessException var13) {
            if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
               Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(var13);
            }
         } finally {
            dbc.clear();
         }

         List servers = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getReplicationServersAsList();
         int i = 0;

         //int i;
         for(i = servers.size(); i < i; ++i) {
            CmsReplicationServer server = (CmsReplicationServer)servers.get(i);
            if (onlineProject != null) {
               CmsProject replicationOnlineProject = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(onlineProject, server.getDestServerName());
               server.setReplicationProject(replicationOnlineProject);
               this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(replicationOnlineProject, server.getDestServerPoolUrl());
               server.reservePool(replicationOnlineProject, server.getDestServerPoolUrl());
               String originPoolUrl = server.getOrgServerPoolUrl();
               if (!originPoolUrl.equals("opencms:default")) {
                  CmsProject originOnlineProject = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(onlineProject, server.getOrgServerName());
                  server.setOrgReplicationProject(originOnlineProject);
                  this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(originOnlineProject, originPoolUrl);
                  server.reservePool(originOnlineProject, originPoolUrl);
               } else {
                  server.setOrgReplicationProject(onlineProject);
               }
            }
         }

         List userReplications = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getUserReplications();
         i = 0;

         for(int n = userReplications.size(); i < n; ++i) {
            CmsReplicationUserSettings userReplication = (CmsReplicationUserSettings)userReplications.get(i);
            if (onlineProject != null) {
               CmsProject replicationProject = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(onlineProject, userReplication.getName());
               userReplication.setReplicationProject(replicationProject);
               CmsReplicationServer server = this.getConfiguration().getReplicationServerByDestServerName(userReplication.getServerName());
               if (server != null) {
                  server.reservePool(replicationProject, userReplication.getPoolUrl());
               }

               this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(replicationProject, userReplication.getPoolUrl());
            }
         }

         this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = cms != null && onlineProject != null;
      }

   }

   public void queuePublishHistory(CmsDbContext dbc, CmsUUID publishHistoryId, I_CmsReport report) {
      try {
         Iterator it = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.readPublishedResources(dbc, publishHistoryId).iterator();

         while(it.hasNext()) {
            CmsPublishedResource res = (CmsPublishedResource)it.next();
            List servers = new ArrayList();
            Iterator itServers = this.getConfiguration().getReplicationServersAsList().iterator();

            while(itServers.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)itServers.next();
               if (server.getMode() == CmsReplicationMode.MANUAL && server.hasResource(res.getRootPath(), true, false) && "opencms:default".equals(server.getOrgServerPoolUrl())) {
                  servers.add(server);
               }
            }

            Iterator itServer = servers.iterator();

            while(itServer.hasNext()) {
               CmsReplicationServer repServer = (CmsReplicationServer)itServer.next();
               getProjectDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, dbc).writePublishHistory(dbc, repServer.getServerId(), res);
            }
         }
      } catch (CmsException var10) {
         if (report == null) {
            if (dbc.getRequestContext() != null) {
               report = new CmsLogReport(dbc.getRequestContext().getLocale(), this.getClass());
            } else {
               report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), this.getClass());
            }
         } else {
            ((I_CmsReport)report).resetRuntime();
         }

         CmsMessageContainer message = Messages.get().container("RPT_WRITE_DATA_FAILED_0");
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(message.key(), var10);
         ((I_CmsReport)report).println(message, 1);
         ((I_CmsReport)report).println(var10);
      }

   }

   public void synchronizeUsers(CmsObject cms, List<CmsReplicationUserSyncData> userSyncData, CmsReplicationUserSyncMode mode, I_CmsReport report) {
      try {
         boolean discard = mode == CmsReplicationUserSyncMode.DISCARD;
         report.println(Messages.get().container(discard ? "RPT_REPLICATION_USER_DISCARD_BEGIN_0" : "RPT_REPLICATION_USER_UPDATE_BEGIN_0"), 2);
         if (userSyncData == null) {
            List replUserData = this.getSynchronizeUsers(cms);
            List userSync = new ArrayList(replUserData.size());
            Iterator i$ = replUserData.iterator();

            while(i$.hasNext()) {
               CmsReplicationUserData data = (CmsReplicationUserData)i$.next();
               userSync.add(data.getSyncData());
            }

            userSyncData = userSync;
         }

         Iterator iter = ((List)userSyncData).iterator();

         label90:
         while(iter.hasNext()) {
            CmsReplicationUserSyncData userEntry = (CmsReplicationUserSyncData)iter.next();
            CmsReplicationUserSettings settings = this.getConfiguration().getUserReplication(userEntry.getSettingsName());

            try {
               CmsUser updatedUser;
               if (!discard) {
                  if (userEntry.getAction() == CmsReplicationUserSyncAction.ADD) {
                     updatedUser = this.getReplicationUser(cms, userEntry.getSettingsName(), userEntry.getUserId());
                     report.print(Messages.get().container("RPT_REPLICATION_USER_ADD_0"), 3);
                     report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", updatedUser.getName()));
                     report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
                     this.createUser(cms, updatedUser, settings);
                  } else if (userEntry.getAction() == CmsReplicationUserSyncAction.DELETE) {
                     updatedUser = cms.readUser(userEntry.getUserId());
                     report.print(Messages.get().container("RPT_REPLICATION_USER_DELETE_0"), 3);
                     report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", updatedUser.getName()));
                     report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
                     cms.deleteUser(updatedUser.getId());
                  } else if (userEntry.getAction() == CmsReplicationUserSyncAction.UPDATE) {
                     updatedUser = this.getReplicationUser(cms, userEntry.getSettingsName(), userEntry.getUserId());
                     report.print(Messages.get().container("RPT_REPLICATION_USER_UPDATE_0"), 3);
                     report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", updatedUser.getName()));
                     report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
                     this.updateUser(cms, updatedUser, settings);
                  }
               } else if (userEntry.getAction() == CmsReplicationUserSyncAction.ADD) {
                  updatedUser = this.getReplicationUser(cms, userEntry.getSettingsName(), userEntry.getUserId());
                  report.print(Messages.get().container("RPT_REPLICATION_USER_DELETE_0"), 3);
                  report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", updatedUser.getName()));
                  report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
                  this.deleteReplicatedUser(cms, updatedUser, settings);
               } else if (userEntry.getAction() == CmsReplicationUserSyncAction.DELETE) {
                  updatedUser = cms.readUser(userEntry.getUserId());
                  report.print(Messages.get().container("RPT_REPLICATION_USER_ADD_0"), 3);
                  report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", updatedUser.getName()));
                  report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
                  this.createReplicatedUser(cms, updatedUser, settings);
               } else if (userEntry.getAction() == CmsReplicationUserSyncAction.UPDATE) {
                  updatedUser = this.getReplicationUser(cms, userEntry.getSettingsName(), userEntry.getUserId());
                  report.print(Messages.get().container("RPT_REPLICATION_USER_UPDATE_0"), 3);
                  report.print(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", updatedUser.getName()));
                  report.print(org.opencms.report.Messages.get().container("RPT_DOTS_0"));
                  this.updateReplicatedUser(cms, updatedUser, settings);
               }

               report.println(org.opencms.report.Messages.get().container("RPT_OK_0"), 4);
            } catch (CmsDbSqlException var16) {
               Throwable cause = var16.getCause();
               if (cause != null) {
                  StackTraceElement[] stackEl = cause.getStackTrace();
                  StackTraceElement[] arr$ = stackEl;
                  int len$ = stackEl.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     StackTraceElement el = arr$[i$];
                     if (el.getClassName().equals(DriverManager.class.getName()) && el.getMethodName().equals("getConnection")) {
                        report.println(Messages.get().container("ERR_CONNECTION_LOST_0"), 5);
                        report.println(var16);
                        report.addError(var16);
                        Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().getBundle().key("ERR_CONNECTION_LOST_0"), var16);
                        break label90;
                     }
                  }
               }
            } catch (CmsException var17) {
               report.println(org.opencms.report.Messages.get().container("RPT_FAILED_0"), 5);
               report.addError(var17);
               if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
                  Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(Messages.get().getBundle().key(discard ? "ERR_REPLICATION_USER_DISCARD_1" : "ERR_REPLICATION_USER_UPDATE_1", userEntry.getUserId()), var17);
               }
            }
         }

         report.println(Messages.get().container(discard ? "RPT_REPLICATION_USER_DISCARD_END_0" : "RPT_REPLICATION_USER_UPDATE_END_0"), 2);
      } catch (Throwable var18) {
         report.println(var18);
         report.addError(var18);
      }

   }

   public void updateReplicatedUser(CmsObject cms, CmsUser user, CmsReplicationUserSettings settings) throws CmsException {
      if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isDebugEnabled()) {
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.debug(Messages.get().getBundle().key("LOG_UPDATING_REPLICATION_USER_REMOTE_2", user.getName(), settings.getServerName()));
      }

      CmsDbContext remoteDbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), settings.getReplicationProject());
      CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
      CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), onlineProject);
      CmsUser localUser = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.readUser(dbc, user.getId());
      I_CmsReplicationUserSynchronization synch = getInstance().getConfiguration().getUserSynchronizationHandler();

      try {
         CmsUser synchUser = synch.commit(localUser, user);
         Set hasGroups = new HashSet(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(remoteDbc, synchUser.getName(), false));
         List shouldHaveGroups = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(dbc, synchUser.getName(), false);
         Set hasRoles = new HashSet(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(remoteDbc, synchUser.getName(), true));
         List shouldHaveRoles = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(dbc, synchUser.getName(), true);
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.writeUser(remoteDbc, synchUser);
         remoteDbc.getRequestContext().setAttribute("DONT_DIGEST_PASSWORD", Boolean.TRUE);
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.setPassword(remoteDbc, synchUser.getName(), synchUser.getPassword());
         this.updateGroupsAndRoles(remoteDbc, synchUser, hasGroups, shouldHaveGroups, hasRoles, shouldHaveRoles);
      } finally {
         remoteDbc.clear();
         dbc.clear();
      }

   }

   public void updateUser(CmsObject cms, CmsUser user, CmsReplicationUserSettings settings) throws CmsException {
      if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isDebugEnabled()) {
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.debug(Messages.get().getBundle().key("LOG_UPDATING_REPLICATION_USER_LOCAL_2", user.getName(), settings.getServerName()));
      }

      CmsDbContext remoteDbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), settings.getReplicationProject());
      CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
      CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), onlineProject);
      CmsUser localUser = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.readUser(dbc, user.getId());
      I_CmsReplicationUserSynchronization synch = getInstance().getConfiguration().getUserSynchronizationHandler();
      dbc.getRequestContext().setAttribute("REPLICATION.UPDATE", Boolean.TRUE);

      try {
         CmsUser synchUser = synch.update(localUser, user);
         Set hasGroups = new HashSet(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(dbc, synchUser.getName(), false));
         List shouldHaveGroups = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(remoteDbc, synchUser.getName(), false);
         Set hasRoles = new HashSet(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(dbc, synchUser.getName(), true));
         List shouldHaveRoles = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getGroupsOfUser(remoteDbc, synchUser.getName(), true);
         dbc.getRequestContext().setAttribute("DONT_DIGEST_PASSWORD", Boolean.TRUE);

         try {
            this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.writeUser(remoteDbc, synchUser);
         } catch (Exception var18) {
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(var18.getLocalizedMessage(), var18);
         }

         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.writeUser(dbc, synchUser);
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.setPassword(dbc, synchUser.getName(), synchUser.getPassword());
         this.updateGroupsAndRoles(dbc, synchUser, hasGroups, shouldHaveGroups, hasRoles, shouldHaveRoles);
      } finally {
         dbc.clear();
         remoteDbc.clear();
      }

   }

   protected void addUserToGroupsAndRoles(CmsDbContext toDbc, CmsUser user, List<CmsGroup> shouldHaveGroups, List<CmsGroup> shouldHaveRoles) throws CmsException {
      Iterator i$ = shouldHaveGroups.iterator();

      CmsGroup role;
      while(i$.hasNext()) {
         role = (CmsGroup)i$.next();
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.addUserToGroup(toDbc, user.getName(), role.getName(), false);
      }

      i$ = shouldHaveRoles.iterator();

      while(i$.hasNext()) {
         role = (CmsGroup)i$.next();
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.addUserToGroup(toDbc, user.getName(), role.getName(), true);
      }

   }

   protected List<CmsPublishedResource> filterIncrementalReplicationResources(List<CmsPublishedResource> resources) {
      Map firstMoveDeletion = new HashMap();
      Map lastEntries = new HashMap();
      Iterator i$ = resources.iterator();

      while(true) {
         while(i$.hasNext()) {
            CmsPublishedResource pubRes = (CmsPublishedResource)i$.next();
            if (pubRes.isMoved() && pubRes.getState().isDeleted() && !firstMoveDeletion.containsKey(pubRes.getStructureId())) {
               firstMoveDeletion.put(pubRes.getStructureId(), pubRes);
            } else {
               lastEntries.put(pubRes.getStructureId(), pubRes);
            }
         }

         List result = new ArrayList();
         result.addAll(firstMoveDeletion.values());
         result.addAll(lastEntries.values());
         return result;
      }
   }

   protected Map<CmsPublishedResource, String> getPublishedResourceHistories(List<CmsPublishedResource> sortedRes) {
      Map resources = new HashMap();
      Map histories = new HashMap();
      Iterator i$ = sortedRes.iterator();

      while(i$.hasNext()) {
         CmsPublishedResource res = (CmsPublishedResource)i$.next();
         String history = "";
         if (histories.containsKey(res.getStructureId())) {
            history = ((String)histories.get(res.getStructureId())).toString();
         }

         history = history + res.getState().getAbbreviation();
         histories.put(res.getStructureId(), history);
         resources.put(res.getStructureId(), res);
      }

      Map ret = new HashMap();
      i$ = resources.keySet().iterator();

      while(i$.hasNext()) {
         CmsUUID id = (CmsUUID)i$.next();
         ret.put(resources.get(id), histories.get(id));
      }

      return ret;
   }

   protected List<CmsPublishedResource> getSortedReplicationResources(CmsObject cms, CmsReplicationServer server) throws CmsDataAccessException {
      CmsDbContext dbc = CmsReplicationServer.getDbContext(cms.getRequestContext(), server.getOrgReplicationProject());
      List sortedRes = getProjectDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, dbc).readPublishedResources(dbc, server.getServerId());
      Comparator comparator = new Comparator<CmsPublishedResource>() {
         public int compare(CmsPublishedResource res1, CmsPublishedResource res2) {
            int publishTagDifference = res1.getPublishTag() - res2.getPublishTag();
            if (publishTagDifference != 0) {
               return publishTagDifference;
            } else if (!res1.getStructureId().equals(res2.getStructureId())) {
               return res1.getRootPath().compareTo(res2.getRootPath());
            } else {
               if (res1.isMoved() && res2.isMoved()) {
                  if (res1.getState().isDeleted()) {
                     return -1;
                  }

                  if (res2.getState().isDeleted()) {
                     return 1;
                  }
               }

               return 0;
            }
         }

      };
      Collections.sort(sortedRes, comparator);
      return sortedRes;
   }

   protected void updateGroupsAndRoles(CmsDbContext toDbc, CmsUser user, Set<CmsGroup> hasGroups, List<CmsGroup> shouldHaveGroups, Set<CmsGroup> hasRoles, List<CmsGroup> shouldHaveRoles) throws CmsException {
      Iterator i$;
      CmsGroup group;
      for(i$ = shouldHaveGroups.iterator(); i$.hasNext(); hasGroups.remove(group)) {
         group = (CmsGroup)i$.next();
         if (!hasGroups.contains(group)) {
            this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.addUserToGroup(toDbc, user.getName(), group.getName(), false);
         }
      }

      i$ = hasGroups.iterator();

      while(i$.hasNext()) {
         group = (CmsGroup)i$.next();
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.removeUserFromGroup(toDbc, user.getName(), group.getName(), false);
      }

      for(i$ = shouldHaveRoles.iterator(); i$.hasNext(); hasRoles.remove(group)) {
         group = (CmsGroup)i$.next();
         if (!hasRoles.contains(group)) {
            this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.addUserToGroup(toDbc, user.getName(), group.getName(), true);
         }
      }

      i$ = hasRoles.iterator();

      while(i$.hasNext()) {
         group = (CmsGroup)i$.next();
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.removeUserFromGroup(toDbc, user.getName(), group.getName(), true);
      }

   }

   private CmsProject o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsProject onlineProject, String id) {
      return new CmsProject(CmsUUID.getConstantUUID(id), "replication project", "Alkacon OCEE replication project", onlineProject.getOwnerId(), onlineProject.getGroupId(), onlineProject.getManagerGroupId(), onlineProject.getFlags(), onlineProject.getDateCreated(), onlineProject.getType());
   }

   private void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(List serverIds, int event) {
      Iterator iter = serverIds.iterator();

      while(iter.hasNext()) {
         CmsUUID id = (CmsUUID)iter.next();
         CmsReplicationServer server = this.getConfiguration().getReplicationServer(id);
         if (server != null) {
            Map data = new HashMap();
            data.put("destinationServer", server.getDestServerName());
            data.put("originServer", server.getOrgServerName());
            OpenCms.fireCmsEvent(event, data);
         }
      }

   }

   private void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsDbContext dbc, CmsObject cms, I_CmsReport report, List handlerNames, List serverIds, List publishedResources, boolean full) {
      if (serverIds == null) {
         serverIds = new ArrayList();
      }

      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super((List)serverIds, 2131);
      if (!CmsClusterManager.getInstance().isInitialized()) {
         CmsClusterManager.getInstance().reInitializeCluster();
      }

      CmsProject oldProject = null;

      try {
         if (report == null) {
            if (dbc.getRequestContext() != null) {
               report = new CmsLogReport(dbc.getRequestContext().getLocale(), this.getClass());
            } else if (cms.getRequestContext() != null) {
               report = new CmsLogReport(cms.getRequestContext().getLocale(), this.getClass());
            } else {
               report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), this.getClass());
            }
         }

         oldProject = this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.readProject(dbc, cms.getRequestContext().currentProject().getUuid());
         if (!CmsClusterManager.getInstance().isInitialized()) {
            throw new CmsIllegalArgumentException(Messages.get().container("GUI_REPLICATION_CLUSTER_NOT_INITIALIZED_0"));
         }

         cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
         this.lazyInitialization(cms);
         List pubServers = new ArrayList();
         Iterator itHandlers = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getReplicationHandlers().iterator();

         label697:
         while(true) {
            I_CmsReplicationHandler handler;
            do {
               if (!itHandlers.hasNext()) {
                  ListIterator itServers = pubServers.listIterator();

                  while(itServers.hasNext()) {
                     CmsReplicationServer server = (CmsReplicationServer)itServers.next();
                     CmsDbContext orgServerDbc = CmsReplicationServer.getDbContext(dbc.getRequestContext(), server.getOrgReplicationProject());
                     boolean commit = false;

                     Iterator it;
                     CmsPublishedResource res;
                     try {
                        it = this.getDriverManager().readPublishedResources(orgServerDbc, server.getServerId()).iterator();

                        while(it.hasNext()) {
                           res = (CmsPublishedResource)it.next();
                           if (publishedResourcesListContains(publishedResources, res.getRootPath())) {
                              getProjectDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, orgServerDbc).deletePublishHistoryEntry(orgServerDbc, server.getServerId(), res);
                              commit = true;
                           }
                        }
                     } finally {
                        if (commit) {
                           orgServerDbc.clear();
                        }

                     }

                     it = publishedResources.iterator();

                     while(it.hasNext()) {
                        res = (CmsPublishedResource)it.next();
                        Iterator iter = this.getConfiguration().getReplicationServersAsList().iterator();

                        while(iter.hasNext()) {
                           CmsReplicationServer repServer = (CmsReplicationServer)iter.next();
                           if (repServer.getMode() == CmsReplicationMode.MANUAL && repServer.hasResource(res.getRootPath(), true, full) && server.hasResource(res.getRootPath(), true, full) && repServer.getOrgServerPoolUrl().equals(server.getDestServerPoolUrl())) {
                              CmsDbContext serverDbc = CmsReplicationServer.getDbContext(dbc.getRequestContext(), repServer.getOrgReplicationProject());

                              try {
                                 getProjectDriver(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class, serverDbc).writePublishHistory(serverDbc, repServer.getServerId(), res);
                              } finally {
                                 serverDbc.clear();
                              }
                           }
                        }
                     }

                     Iterator iter = this.getConfiguration().getReplicationServersAsList().iterator();

                     while(iter.hasNext()) {
                        CmsReplicationServer repServer = (CmsReplicationServer)iter.next();
                        if (repServer.getMode() == CmsReplicationMode.AUTO && repServer.getOrgServerPoolUrl().equals(server.getDestServerPoolUrl())) {
                           CmsDbContext serverDbc = CmsReplicationServer.getDbContext(dbc.getRequestContext(), repServer.getOrgReplicationProject());
                           List servIds = new ArrayList();
                           servIds.add(repServer.getServerId());
                           this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(serverDbc, cms, (I_CmsReport)report, handlerNames, servIds, publishedResources, full);
                        }
                     }
                  }

                  OpenCms.writeConfiguration(CmsReplicationConfiguration.class);
                  return;
               }

               handler = (I_CmsReplicationHandler)itHandlers.next();
            } while(handlerNames != null && !handlerNames.contains(handler.getClass().getName()));

            ((I_CmsReport)report).resetRuntime();
            if (full) {
               ((I_CmsReport)report).println(Messages.get().container("RPT_BEGIN_FULL_REPLICATION_1", handler.getName().key(((I_CmsReport)report).getLocale())), 2);
            } else {
               ((I_CmsReport)report).println(Messages.get().container("RPT_BEGIN_INCREMENTAL_REPLICATION_1", handler.getName().key(((I_CmsReport)report).getLocale())), 2);
            }

            ListIterator itServers = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getReplicationServersAsList().listIterator();

            while(true) {
               CmsReplicationServer server;
               do {
                  if (!itServers.hasNext()) {
                     if (full) {
                        ((I_CmsReport)report).println(Messages.get().container("RPT_END_FULL_REPLICATION_1", handler.getName().key(((I_CmsReport)report).getLocale())), 2);
                     } else {
                        ((I_CmsReport)report).println(Messages.get().container("RPT_END_INCREMENTAL_REPLICATION_1", handler.getName().key(((I_CmsReport)report).getLocale())), 2);
                     }
                     continue label697;
                  }

                  server = (CmsReplicationServer)itServers.next();
               } while(serverIds != null && !((List)serverIds).contains(server.getServerId()));

               CmsDbContext serverDbc = CmsReplicationServer.getDbContext(dbc.getRequestContext(), server.getOrgReplicationProject());
               if (!pubServers.contains(server)) {
                  pubServers.add(server);
               }

               try {
                  if (full) {
                     handler.cleanUp(cms, serverDbc, (I_CmsReport)report, server, publishedResources);
                  }

                  handler.replicate(cms, serverDbc, (I_CmsReport)report, server, publishedResources, full);
               } catch (Throwable var39) {
                  CmsMessageContainer message = Messages.get().container("RPT_REPLICATION_FAILED_0");
                  Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(message.key(), var39);
                  ((I_CmsReport)report).println(message, 1);
                  ((I_CmsReport)report).println(var39);
                  ((I_CmsReport)report).println();
               }
            }
         }
      } catch (Throwable var41) {
         CmsMessageContainer message = Messages.get().container("RPT_REPLICATION_FAILED_0");
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(message.key(), var41);
         ((I_CmsReport)report).println(message, 1);
         ((I_CmsReport)report).println(var41);
      } finally {
         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super((List)serverIds, 2132);
         if (oldProject != null) {
            cms.getRequestContext().setCurrentProject(oldProject);
         }

      }

   }

   private void Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(CmsProject replicationProject, String poolUrl) {
      CmsSqlManager sqlManager = null;
      getOceeSqlManager(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getVfsDriver().getSqlManager()).setReservedPoolUrl(replicationProject.getUuid(), poolUrl);
      getOceeSqlManager(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getUserDriver().getSqlManager()).setReservedPoolUrl(replicationProject.getUuid(), poolUrl);
      getOceeSqlManager(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getProjectDriver().getSqlManager()).setReservedPoolUrl(replicationProject.getUuid(), poolUrl);
      getOceeSqlManager(this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class.getHistoryDriver().getSqlManager()).setReservedPoolUrl(replicationProject.getUuid(), poolUrl);
   }

   private List o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsDbContext dbc, I_CmsReport report, List resourceNames, String serverId) throws CmsReplicationException {
      CmsReplicationServer server = this.getConfiguration().getReplicationServer(new CmsUUID(serverId));
      CmsDbContext serverDbc = CmsReplicationServer.getDbContext(dbc.getRequestContext(), server.getOrgReplicationProject());
      HashMap ret = new HashMap();

      try {
         Iterator it = resourceNames.iterator();

         label45:
         while(true) {
            boolean needed;
            String rootPath;
            do {
               if (!it.hasNext()) {
                  break label45;
               }

               rootPath = (String)it.next();
               needed = server.hasResource(rootPath, true, true);
            } while(!needed);

            CmsResource rootResource = this.getDriverManager().readResource(serverDbc, rootPath, CmsResourceFilter.DEFAULT);
            List resources = new ArrayList();
            if (rootResource.isFolder()) {
               resources.addAll(this.getDriverManager().readResources(serverDbc, rootResource, CmsResourceFilter.DEFAULT, true,null,null));
            }

            if (!resources.contains(rootResource)) {
               resources.add(rootResource);
            }

            Iterator itResources = resources.iterator();

            while(itResources.hasNext()) {
               CmsResource resource = (CmsResource)itResources.next();
               needed = server.hasResource(resource.getRootPath(), true, true);
               if (resource.isFolder()) {
                  report.println(Messages.get().container("RPT_CHECKING_1", resource.getRootPath()));
               }

               if (needed) {
                  int backupTagId = getHistoryDriver(this.getDriverManager(), serverDbc).readMaxPublishTag(serverDbc, resource.getResourceId());
                  ret.put(resource.getRootPath(), new CmsPublishedResource(resource.getStructureId(), resource.getResourceId(), backupTagId, resource.getRootPath(), resource.getTypeId(), resource.isFolder(), CmsResource.STATE_NEW, resource.getSiblingCount()));
               }
            }
         }
      } catch (CmsException var16) {
         CmsMessageContainer message = Messages.get().container("ERR_FILTERING_VFS_RESOURCES_0");
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(message.key(), var16);
         report.println(var16);
         throw new CmsReplicationException(message, var16);
      }

      List retList = new ArrayList(ret.values());
      Collections.sort(retList, PUBRES_COMPARATOR);
      return retList;
   }
}
