package org.opencms.ocee.replication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserExternalProvider;
import org.opencms.file.CmsUserTwoFactor;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.util.CmsUUID;

public class CmsReplicationUserDriver implements I_CmsDriver, I_CmsUserDriver {
   public static final String REQ_ATTR_REPLICATION_SKIP_UPDATE = "REPLICATION.UPDATE";
   public static final String USER_INFO_LAST_MODIFICATION = "USER_LASTMODIFIED";
   private static final Log LOG = CmsLog.getLog(CmsReplicationUserDriver.class);
   protected I_CmsUserDriver userDriver;
   private CmsDriverManager driverManager;

   public void addResourceToOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource) throws CmsDataAccessException {
      this.userDriver.addResourceToOrganizationalUnit(dbc, orgUnit, resource);
      if (dbc.getProjectId() == null || dbc.getProjectId().isNullUUID()) {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("addResourceToOrganizationalUnit(" + server.getName() + "): replicating organizational unit: " + orgUnit.getName() + ", resource: " + resource.getName());
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).addResourceToOrganizationalUnit(remoteDbc, orgUnit, resource);
               } catch (CmsDataAccessException var13) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_ORGUNIT_2", orgUnit.getName(), server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var13);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   public void createAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) throws CmsDataAccessException {
      this.userDriver.createAccessControlEntry(dbc, project, resource, principal, allowed, denied, flags);
   }

   public CmsGroup createGroup(CmsDbContext dbc, CmsUUID groupId, String groupFqn, String description, int flags, String parentGroupName) throws CmsDataAccessException {
      CmsGroup group = this.userDriver.createGroup(dbc, groupId, groupFqn, description, flags, parentGroupName);
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null && dbc.getProjectId().isNullUUID()) {
         Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

         while(it.hasNext()) {
            CmsReplicationServer server = (CmsReplicationServer)it.next();
            if (LOG.isDebugEnabled()) {
               LOG.debug("createGroup(" + server.getName() + "): replicating group " + groupFqn);
            }

            CmsDbContext remoteDbc = null;

            try {
               remoteDbc = server.getDbContext(dbc.getRequestContext());
               CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).createGroup(remoteDbc, groupId, groupFqn, description, flags, parentGroupName);
            } catch (CmsDataAccessException var17) {
               CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_GROUP_2", groupFqn, server.getName());
               if (LOG.isWarnEnabled()) {
                  LOG.warn(message.key(), var17);
               }
            } finally {
               if (remoteDbc != null) {
                  remoteDbc.clear();
               }

            }
         }
      }

      return group;
   }

   public CmsOrganizationalUnit createOrganizationalUnit(CmsDbContext dbc, String name, String description, int flags, CmsOrganizationalUnit parent, String associationRootPath) throws CmsDataAccessException {
      CmsOrganizationalUnit orgUnit = this.userDriver.createOrganizationalUnit(dbc, name, description, flags, parent, associationRootPath);
      if (dbc.getProjectId() != null && !dbc.getProjectId().isNullUUID()) {
         return orgUnit;
      } else {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("createOrganizationalUnit(" + server.getName() + "): Replicating organizational unit " + name);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).createOrganizationalUnit(remoteDbc, name, description, flags, parent, associationRootPath);
               } catch (CmsDataAccessException var16) {
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(Messages.get().container("ERR_ERROR_REPLICATING_ORGUNIT_2", name, server.getName()).key(), var16);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

         return orgUnit;
      }
   }

   public void createRootOrganizationalUnit(CmsDbContext dbc) {
      this.userDriver.createRootOrganizationalUnit(dbc);
   }

   public CmsUser createUser(CmsDbContext dbc, CmsUUID id, String userFqn, String password, String firstname, String lastname, String email, long lastlogin, int flags, long dateCreated, Map additionalInfos) throws CmsDataAccessException {
      CmsUser user = this.userDriver.createUser(dbc, id, userFqn, password, firstname, lastname, email, lastlogin, flags, dateCreated, additionalInfos);
      if (dbc.getProjectId() != null && !dbc.getProjectId().isNullUUID()) {
         return user;
      } else {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null && dbc.getProjectId().isNullUUID()) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("createUser(" + server.getName() + "): Replicating user " + userFqn);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).createUser(remoteDbc, user.getId(), userFqn, password, firstname, lastname, email, lastlogin, flags, dateCreated, additionalInfos);
               } catch (CmsDataAccessException var23) {
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(Messages.get().container("ERR_ERROR_REPLICATING_USER_2", userFqn, server.getName()).key(), var23);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

         return user;
      }
   }

   public void createUserInGroup(CmsDbContext dbc, CmsUUID userid, CmsUUID groupid) throws CmsDataAccessException {
      this.userDriver.createUserInGroup(dbc, userid, groupid);
      if (dbc.getProjectId() == null || dbc.getProjectId().isNullUUID()) {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null && dbc.getProjectId().isNullUUID()) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("createUserInGroup(" + server.getName() + "): replicating membership user: " + userid + ", group: " + groupid);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());

                  try {
                     CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).readGroup(remoteDbc, groupid);
                  } catch (CmsDbEntryNotFoundException var20) {
                     CmsGroup group = this.readGroup(dbc, groupid);
                     ArrayList groupsToReplicate = new ArrayList();

                     while(true) {
                        while(group != null) {
                           groupsToReplicate.add(group);
                           if (group.getParentId() != null && !group.getParentId().isNullUUID()) {
                              try {
                                 CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).readGroup(remoteDbc, group.getParentId());
                                 group = null;
                              } catch (CmsDbEntryNotFoundException var19) {
                                 group = this.readGroup(dbc, group.getParentId());
                              }
                           } else {
                              group = null;
                           }
                        }

                        Collections.reverse(groupsToReplicate);
                        Iterator itGroups = groupsToReplicate.iterator();

                        while(itGroups.hasNext()) {
                           group = (CmsGroup)itGroups.next();
                           CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).createGroup(remoteDbc, groupid, group.getName(), group.getDescription(), group.getFlags(), this.readGroup(dbc, group.getParentId()).getName());
                        }
                        break;
                     }
                  }

                  try {
                     CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).readUser(remoteDbc, userid);
                  } catch (CmsDbEntryNotFoundException var18) {
                     CmsUser user = this.readUser(dbc, userid);
                     CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).createUser(remoteDbc, userid, user.getName(), user.getPassword(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getLastlogin(), user.getFlags(), user.getDateCreated(), user.getAdditionalInfo());
                  }

                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).createUserInGroup(remoteDbc, userid, groupid);
               } catch (CmsDataAccessException var21) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_USERINGROUP_3", userid, groupid, server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var21);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   /** @deprecated */
   public void deleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsDataAccessException {
      this.userDriver.deleteAccessControlEntries(dbc, project, resource);
   }

   public void deleteGroup(CmsDbContext dbc, String name) throws CmsDataAccessException {
      this.userDriver.deleteGroup(dbc, name);
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null) {
         Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

         while(it.hasNext()) {
            CmsReplicationServer server = (CmsReplicationServer)it.next();
            if (LOG.isDebugEnabled()) {
               LOG.debug("deleteGroup(" + server.getName() + "): replicate group " + name);
            }

            CmsDbContext remoteDbc = null;

            try {
               remoteDbc = server.getDbContext(dbc.getRequestContext());
               CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).deleteGroup(remoteDbc, name);
            } catch (CmsDataAccessException var12) {
               CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_GROUP_2", name, server.getName());
               if (LOG.isWarnEnabled()) {
                  LOG.warn(message.key(), var12);
               }
            } finally {
               if (remoteDbc != null) {
                  remoteDbc.clear();
               }

            }
         }
      }

   }

   public void deleteOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
      this.userDriver.deleteOrganizationalUnit(dbc, organizationalUnit);
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null) {
         Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

         while(it.hasNext()) {
            CmsReplicationServer server = (CmsReplicationServer)it.next();
            if (LOG.isDebugEnabled()) {
               LOG.debug("deleteOrganizationalUnit(" + server.getName() + "): replicating organizational unit " + organizationalUnit.getName());
            }

            CmsDbContext remoteDbc = null;

            try {
               remoteDbc = server.getDbContext(dbc.getRequestContext());
               CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).deleteOrganizationalUnit(remoteDbc, organizationalUnit);
            } catch (CmsDataAccessException var12) {
               CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_ORGUNIT_2", organizationalUnit.getName(), server.getName());
               if (LOG.isWarnEnabled()) {
                  LOG.warn(message.key(), var12);
               }
            } finally {
               if (remoteDbc != null) {
                  remoteDbc.clear();
               }

            }
         }
      }

   }

   public void deleteUser(CmsDbContext dbc, String userName) throws CmsDataAccessException {
      this.userDriver.deleteUser(dbc, userName);
      if (dbc.getProjectId() == null || dbc.getProjectId().isNullUUID()) {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("deleteUser(" + server.getName() + "): replicating user " + userName);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).deleteUser(remoteDbc, userName);
               } catch (CmsDataAccessException var12) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_USER_2", userName, server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var12);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   public void deleteUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
      this.userDriver.deleteUserInfos(dbc, userId);
      if (dbc.getProjectId() == null || dbc.getProjectId().isNullUUID()) {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("deleteUserInfos(" + server.getName() + "): replicating user id " + userId);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).deleteUserInfos(remoteDbc, userId);
               } catch (CmsDataAccessException var12) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_USER_2", userId, server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var12);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   public void deleteUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId) throws CmsDataAccessException {
      this.userDriver.deleteUserInGroup(dbc, userId, groupId);
      if (dbc.getProjectId() == null || dbc.getProjectId().isNullUUID()) {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("deleteUserInGroup(" + server.getName() + "): replicating membership user: " + userId + ", group: " + groupId);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).deleteUserInGroup(remoteDbc, userId, groupId);
               } catch (CmsDataAccessException var13) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_USERINGROUP_3", userId, groupId, server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var13);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   public void destroy() throws Throwable, CmsException {
      this.userDriver.destroy();
      if (CmsLog.INIT.isInfoEnabled()) {
         CmsLog.INIT.info(org.opencms.db.generic.Messages.get().getBundle().key("INIT_SHUTDOWN_DRIVER_1", new Object[]{this.getClass().getName()}));
      }

      this.finalize();
   }

   public boolean existsGroup(CmsDbContext dbc, String groupName) throws CmsDataAccessException {
      return this.userDriver.existsGroup(dbc, groupName);
   }

   public boolean existsUser(CmsDbContext dbc, String userName) throws CmsDataAccessException {
      return this.userDriver.existsUser(dbc, userName);
   }

   public void fillDefaults(CmsDbContext dbc) throws CmsInitException {
      this.userDriver.fillDefaults(dbc);
   }

   public List getGroups(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean includeSubOus, boolean readRoles) throws CmsDataAccessException {
      return this.userDriver.getGroups(dbc, orgUnit, includeSubOus, readRoles);
   }

   public List getOrganizationalUnits(CmsDbContext dbc, CmsOrganizationalUnit parent, boolean includeChilds) throws CmsDataAccessException {
      return this.userDriver.getOrganizationalUnits(dbc, parent, includeChilds);
   }

   public List getResourcesForOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit) throws CmsDataAccessException {
      return this.userDriver.getResourcesForOrganizationalUnit(dbc, orgUnit);
   }

   public CmsSqlManager getSqlManager() {
      return this.userDriver.getSqlManager();
   }

   public List getUsers(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean recursive) throws CmsDataAccessException {
      return this.userDriver.getUsers(dbc, orgUnit, recursive);
   }

   public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List successiveDrivers, CmsDriverManager driverManager) throws CmsInitException {
      Map configuration = configurationManager.getConfiguration();
      String driverName = (String)configuration.get((String)successiveDrivers.get(0) + ".user.driver");
      successiveDrivers = successiveDrivers.size() > 1 ? successiveDrivers.subList(1, successiveDrivers.size()) : null;
      this.userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurationManager, driverName, successiveDrivers);
      this.driverManager = driverManager;
      if (CmsReplicationManager.getInstance() != null && !CmsReplicationManager.getInstance().isConfigured()) {
         throw new CmsIllegalStateException(Messages.get().container("ERR_MANAGER_NOT_INITIALIZED_0"));
      }
   }

   public CmsSqlManager initSqlManager(String classname) {
      return this.userDriver.initSqlManager(classname);
   }

   public void publishAccessControlEntries(CmsDbContext dbc, CmsProject offlineProject, CmsProject onlineProject, CmsUUID offlineId, CmsUUID onlineId) throws CmsDataAccessException {
      this.userDriver.publishAccessControlEntries(dbc, offlineProject, onlineProject, offlineId, onlineId);
   }

   public List readAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource, boolean inheritedOnly) throws CmsDataAccessException {
      return this.userDriver.readAccessControlEntries(dbc, project, resource, inheritedOnly);
   }

   public CmsAccessControlEntry readAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal) throws CmsDataAccessException {
      return this.userDriver.readAccessControlEntry(dbc, project, resource, principal);
   }

   public List readChildGroups(CmsDbContext dbc, String groupname) throws CmsDataAccessException {
      return this.userDriver.readChildGroups(dbc, groupname);
   }

   public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsDataAccessException {
      return this.userDriver.readGroup(dbc, groupId);
   }

   public CmsGroup readGroup(CmsDbContext dbc, String groupName) throws CmsDataAccessException {
      return this.userDriver.readGroup(dbc, groupName);
   }

   public List readGroupsOfUser(CmsDbContext dbc, CmsUUID userId, String ouFqn, boolean includeChildOus, String remoteAddress, boolean readRoles) throws CmsDataAccessException {
      return this.userDriver.readGroupsOfUser(dbc, userId, ouFqn, includeChildOus, remoteAddress, readRoles);
   }

   public CmsOrganizationalUnit readOrganizationalUnit(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException {
      return this.userDriver.readOrganizationalUnit(dbc, ouFqn);
   }

   public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {
      return this.userDriver.readUser(dbc, id);
   }

   public CmsUser readUser(CmsDbContext dbc, String name) throws CmsDataAccessException {
      return this.userDriver.readUser(dbc, name);
   }

   public CmsUser readUser(CmsDbContext dbc, String userFqn, String password, String remoteAddress) throws CmsDataAccessException, CmsPasswordEncryptionException {
      return this.userDriver.readUser(dbc, userFqn, password, remoteAddress);
   }

   public Map readUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
      return this.userDriver.readUserInfos(dbc, userId);
   }

   public List readUsersOfGroup(CmsDbContext dbc, String name, boolean includeOtherOuUsers) throws CmsDataAccessException {
      return this.userDriver.readUsersOfGroup(dbc, name, includeOtherOuUsers);
   }

   public void removeAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsDataAccessException {
      this.userDriver.removeAccessControlEntries(dbc, project, resource);
   }

   public void removeAccessControlEntriesForPrincipal(CmsDbContext dbc, CmsProject project, CmsProject onlineProject, CmsUUID principal) throws CmsDataAccessException {
      this.userDriver.removeAccessControlEntriesForPrincipal(dbc, project, onlineProject, principal);
   }

   public void removeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal) throws CmsDataAccessException {
      this.userDriver.removeAccessControlEntry(dbc, project, resource, principal);
   }

   public void removeResourceFromOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource) throws CmsDataAccessException {
      this.userDriver.removeResourceFromOrganizationalUnit(dbc, orgUnit, resource);
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null) {
         Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

         while(it.hasNext()) {
            CmsReplicationServer server = (CmsReplicationServer)it.next();
            if (LOG.isDebugEnabled()) {
               LOG.debug("removeResourceFromOrganizationalUnit(" + server.getName() + "): replicating organizational unit " + orgUnit.getName() + "; resource " + resource.getRootPath());
            }

            CmsDbContext remoteDbc = null;

            try {
               remoteDbc = server.getDbContext(dbc.getRequestContext());
               CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).removeResourceFromOrganizationalUnit(remoteDbc, orgUnit, resource);
            } catch (CmsDataAccessException var13) {
               CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_ORGUNIT_2", orgUnit.getName(), server.getName());
               if (LOG.isWarnEnabled()) {
                  LOG.warn(message.key(), var13);
               }
            } finally {
               if (remoteDbc != null) {
                  remoteDbc.clear();
               }

            }
         }
      }

   }

   public void setUsersOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsUser user) throws CmsDataAccessException {
      this.userDriver.setUsersOrganizationalUnit(dbc, orgUnit, user);
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null) {
         Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

         while(it.hasNext()) {
            CmsReplicationServer server = (CmsReplicationServer)it.next();
            if (LOG.isDebugEnabled()) {
               LOG.debug("setUsersOrganizationalUnit(" + server.getName() + "): replicating user: " + user.getName() + ", orgUnit: " + orgUnit.getName());
            }

            CmsDbContext remoteDbc = null;

            try {
               remoteDbc = server.getDbContext(dbc.getRequestContext());
               CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).setUsersOrganizationalUnit(remoteDbc, orgUnit, user);
            } catch (CmsDataAccessException var13) {
               CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_USER_2", user.getName(), server.getName());
               if (LOG.isWarnEnabled()) {
                  LOG.warn(message.key(), var13);
               }
            } finally {
               if (remoteDbc != null) {
                  remoteDbc.clear();
               }

            }
         }
      }

   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[" + this.getClass().getName() + "]:\n");
      buffer.append(this.userDriver.toString());
      return buffer.toString();
   }

   public void writeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsAccessControlEntry acEntry) throws CmsDataAccessException {
      this.userDriver.writeAccessControlEntry(dbc, project, acEntry);
   }

   public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {
      CmsGroup oldGroup = this.userDriver.readGroup(dbc, group.getId());
      if (oldGroup != null && (group.getName() != oldGroup.getName() && !group.getName().equals(oldGroup.getName()) || group.getDescription() != oldGroup.getDescription() && !group.getDescription().equals(oldGroup.getDescription()) || group.getFlags() != oldGroup.getFlags() || group.getParentId() != oldGroup.getParentId() && !group.getParentId().equals(oldGroup.getParentId()))) {
         this.userDriver.writeGroup(dbc, group);
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("writeGroup(" + server.getName() + "): replicate group " + group.getName());
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).writeGroup(remoteDbc, group);
               } catch (CmsDataAccessException var13) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_GROUP_2", group.getName(), server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var13);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   public void writeOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
      this.userDriver.writeOrganizationalUnit(dbc, organizationalUnit);
      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      if (manager != null) {
         Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

         while(it.hasNext()) {
            CmsReplicationServer server = (CmsReplicationServer)it.next();
            if (LOG.isDebugEnabled()) {
               LOG.debug("writeOrganizationalUnit(" + server.getName() + "): replicate organizational unit " + organizationalUnit.getName());
            }

            CmsDbContext remoteDbc = null;

            try {
               remoteDbc = server.getDbContext(dbc.getRequestContext());
               CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).writeOrganizationalUnit(remoteDbc, organizationalUnit);
            } catch (CmsDataAccessException var12) {
               CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_ORGUNIT_2", organizationalUnit.getName(), server.getName());
               if (LOG.isWarnEnabled()) {
                  LOG.warn(message.key(), var12);
               }
            } finally {
               if (remoteDbc != null) {
                  remoteDbc.clear();
               }

            }
         }
      }

   }

   public void writePassword(CmsDbContext dbc, String userFqn, String oldPassword, String newPassword) throws CmsDataAccessException, CmsPasswordEncryptionException {
      boolean pwdChanged;
      try {
         this.userDriver.readUser(dbc, userFqn, newPassword, (String)null);
         pwdChanged = false;
      } catch (Throwable var16) {
         pwdChanged = true;
      }

      if (pwdChanged) {
         this.userDriver.writePassword(dbc, userFqn, oldPassword, newPassword);
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("writePassword(" + server.getName() + "): replicate user " + userFqn);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).writePassword(remoteDbc, userFqn, oldPassword, newPassword);
               } catch (CmsDataAccessException var17) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_GROUP_2", userFqn, server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var17);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {
      if (dbc.getProjectId() != null && !dbc.getProjectId().isNullUUID()) {
         user.deleteAdditionalInfo("USER_LASTMODIFIED");
         this.userDriver.writeUser(dbc, user);
      } else {
         CmsUser oldUser = this.userDriver.readUser(dbc, user.getId());
         if (dbc.getRequestContext() != null && dbc.getRequestContext().getAttribute("REPLICATION.UPDATE") != null) {
            user = this.LOG(dbc.getRequestContext(), user);
         }

         this.userDriver.writeUser(dbc, user);
         if (this.LOG(oldUser, user) || oldUser.getLastlogin() == 0L && user.getLastlogin() != oldUser.getLastlogin()) {
            user.deleteAdditionalInfo("USER_LASTMODIFIED");
            CmsReplicationManager manager = CmsReplicationManager.getInstance();
            if (manager != null) {
               Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

               while(it.hasNext()) {
                  CmsReplicationServer server = (CmsReplicationServer)it.next();
                  if (LOG.isDebugEnabled()) {
                     LOG.debug("WriteUser(" + server.getName() + "): replicate user " + user.getName());
                  }

                  CmsDbContext remoteDbc = null;

                  try {
                     remoteDbc = server.getDbContext(dbc.getRequestContext());
                     CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).writeUser(remoteDbc, user);
                  } catch (CmsDataAccessException var12) {
                     if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().container("ERR_ERROR_REPLICATING_USER_2", user.getName(), server.getName()).key(), var12);
                     }
                  } finally {
                     if (remoteDbc != null) {
                        remoteDbc.clear();
                     }

                  }
               }
            }

         }
      }
   }

   public void writeUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value) throws CmsDataAccessException {
      this.userDriver.writeUserInfo(dbc, userId, key, value);
      if (dbc.getProjectId() == null || dbc.getProjectId().isNullUUID()) {
         CmsReplicationManager manager = CmsReplicationManager.getInstance();
         if (manager != null) {
            Iterator it = manager.getConfiguration().getReplicationServersAsList().iterator();

            while(it.hasNext()) {
               CmsReplicationServer server = (CmsReplicationServer)it.next();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("writeUserInfo(" + server.getName() + "): replicating user: " + userId + ", " + key + "=" + value);
               }

               CmsDbContext remoteDbc = null;

               try {
                  remoteDbc = server.getDbContext(dbc.getRequestContext());
                  CmsReplicationManager.getUserDriver(this.driverManager, remoteDbc, this.userDriver).writeUserInfo(remoteDbc, userId, key, value);
               } catch (CmsDataAccessException var14) {
                  CmsMessageContainer message = Messages.get().container("ERR_ERROR_REPLICATING_USER_2", userId, server.getName());
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(message.key(), var14);
                  }
               } finally {
                  if (remoteDbc != null) {
                     remoteDbc.clear();
                  }

               }
            }
         }

      }
   }

   private CmsUser LOG(CmsRequestContext requestContext, CmsUser user) {
      if (LOG.isDebugEnabled()) {
         LOG.debug(Messages.get().container("LOG_UPDATING_REPLICATION_USER_SERVERS_1", user.getName()));
      }

      CmsReplicationManager manager = CmsReplicationManager.getInstance();
      I_CmsReplicationUserSynchronization synch = CmsReplicationManager.getInstance().getConfiguration().getUserSynchronizationHandler();
      if (manager != null) {
         Iterator setIter = manager.getConfiguration().getUserReplications().iterator();

         while(setIter.hasNext()) {
            CmsReplicationUserSettings settings = (CmsReplicationUserSettings)setIter.next();
            CmsDbContext dbc = CmsReplicationServer.getDbContext(requestContext, settings.getReplicationProject());

            try {
               CmsUser remoteUser = this.readUser(dbc, user.getId());
               user = synch.commit(user, remoteUser);
            } catch (CmsException var9) {
               if (LOG.isWarnEnabled()) {
                  LOG.warn(Messages.get().container("ERR_READING_REPLICATED_USER_2", user.getName(), settings.getServerName()), var9);
               }
            }
         }
      }

      return user;
   }

   private final boolean LOG(CmsUser oldUser, CmsUser newUser) {
      if (!oldUser.getName().equals(newUser.getName()) && !oldUser.getFirstname().equals(newUser.getFirstname()) && !oldUser.getLastname().equals(newUser.getLastname()) && !oldUser.getEmail().equals(newUser.getEmail())) {
         return true;
      } else if (oldUser.getDescription() != null && newUser.getDescription() != null && !oldUser.getDescription().equals(newUser.getDescription())) {
         return true;
      } else if (oldUser.getAddress() != null && newUser.getAddress() != null && !oldUser.getAddress().equals(newUser.getAddress())) {
         return true;
      } else {
         return !oldUser.getAdditionalInfo().equals(newUser.getAdditionalInfo());
      }
   }

@Override
public void createExternalProvider(CmsDbContext dbc, CmsUserExternalProvider externalProvider)
		throws CmsDataAccessException {
	userDriver.createExternalProvider(dbc, externalProvider);
	
}

@Override
public CmsUserExternalProvider readExternalUserProvider(CmsDbContext dbc, String providerName, String id)
		throws CmsDataAccessException {
	
	return userDriver.readExternalUserProvider(dbc, providerName, id);
}

@Override
public void updateExternalProvider(CmsDbContext dbc, CmsUserExternalProvider userProv) throws CmsDataAccessException {
	userDriver.updateExternalProvider(dbc, userProv);
	
}

@Override
public void writeTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
	userDriver.writeTwoFactor(dbc, twoFactor);
	
}

@Override
public void updateTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
	userDriver.updateTwoFactor(dbc, twoFactor);
	
}

@Override
public CmsUserTwoFactor readTwoFactor(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {
	return userDriver.readTwoFactor(dbc, id);
}

@Override
public CmsUserTwoFactor readTwoFactorByTemp(CmsDbContext dbc, String tempId) throws CmsDataAccessException {
	return userDriver.readTwoFactorByTemp(dbc, tempId);
}

@Override
public void deleteTwoFactor(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
	userDriver.deleteTwoFactor(dbc, userId);
	
}
}
