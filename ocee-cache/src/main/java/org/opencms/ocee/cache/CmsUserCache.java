/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.opencms.configuration.CmsConfigurationManager
 *  org.opencms.db.CmsDbContext
 *  org.opencms.db.CmsDbEntryNotFoundException
 *  org.opencms.db.CmsDriverManager
 *  org.opencms.db.I_CmsDriver
 *  org.opencms.db.I_CmsUserDriver
 *  org.opencms.db.generic.CmsSqlManager
 *  org.opencms.file.CmsDataAccessException
 *  org.opencms.file.CmsGroup
 *  org.opencms.file.CmsProject
 *  org.opencms.file.CmsResource
 *  org.opencms.file.CmsUser
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.main.CmsException
 *  org.opencms.main.CmsInitException
 *  org.opencms.main.CmsLog
 *  org.opencms.main.I_CmsEventListener
 *  org.opencms.main.OpenCms
 *  org.opencms.monitor.CmsMemoryMonitor
 *  org.opencms.monitor.CmsMemoryMonitor$CacheType
 *  org.opencms.ocee.base.CmsOceeManager
 *  org.opencms.security.CmsAccessControlEntry
 *  org.opencms.security.CmsOrganizationalUnit
 *  org.opencms.security.CmsPasswordEncryptionException
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserExternalProvider;
import org.opencms.file.CmsUserToken;
import org.opencms.file.CmsUserTwoFactor;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.user.CmsUserCacheEventHandler;
import org.opencms.ocee.cache.user.CmsUserCacheInstanceType;
import org.opencms.ocee.cache.user.CmsUserCacheKey;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsVfsCacheKey;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.util.CmsUUID;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CmsUserCache
implements I_CmsDriver,
I_CmsUserDriver {
    protected I_CmsUserDriver userDriver;
    private CmsUserCacheEventHandler userCacheEventHandler;

    public void addResourceToOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource) throws CmsDataAccessException {
        this.userDriver.addResourceToOrganizationalUnit(dbc, orgUnit, resource);
    }

    public void createAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) throws CmsDataAccessException {
        this.userDriver.createAccessControlEntry(dbc, project, resource, principal, allowed, denied, flags);
    }

    public CmsGroup createGroup(CmsDbContext dbc, CmsUUID groupId, String groupName, String description, int flags, String parentGroupName) throws CmsDataAccessException {
        return this.userDriver.createGroup(dbc, groupId, groupName, description, flags, parentGroupName);
    }

    public CmsOrganizationalUnit createOrganizationalUnit(CmsDbContext dbc, String name, String description, int flags, CmsOrganizationalUnit parent, String associationRootPath) throws CmsDataAccessException {
        CmsOrganizationalUnit orgUnit = this.userDriver.createOrganizationalUnit(dbc, name, description, flags, parent, associationRootPath);
        return orgUnit;
    }

    public void createRootOrganizationalUnit(CmsDbContext dbc) {
        this.userDriver.createRootOrganizationalUnit(dbc);
    }

    public CmsUser createUser(CmsDbContext dbc, CmsUUID id, String userFqn, String password, String firstname, String lastname, String email, long lastlogin, int flags, long dateCreated, Map additionalInfos) throws CmsDataAccessException {
        CmsUser user = this.userDriver.createUser(dbc, id, userFqn, password, firstname, lastname, email, lastlogin, flags, dateCreated, additionalInfos);
        return user;
    }

    public void createUserInGroup(CmsDbContext dbc, CmsUUID userid, CmsUUID groupid) throws CmsDataAccessException {
        this.userDriver.createUserInGroup(dbc, userid, groupid);
    }

    @Deprecated
    public void deleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsDataAccessException {
        this.userDriver.deleteAccessControlEntries(dbc, project, resource);
    }

    public void deleteGroup(CmsDbContext dbc, String name) throws CmsDataAccessException {
        this.userDriver.deleteGroup(dbc, name);
    }

    public void deleteOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
        this.userDriver.deleteOrganizationalUnit(dbc, organizationalUnit);
    }

    public void deleteUser(CmsDbContext dbc, String userName) throws CmsDataAccessException {
        this.userDriver.deleteUser(dbc, userName);
    }

    public void deleteUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
        this.userDriver.deleteUserInfos(dbc, userId);
    }

    public void deleteUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId) throws CmsDataAccessException {
        this.userDriver.deleteUserInGroup(dbc, userId, groupId);
    }

    public void destroy() throws Throwable, CmsException {
        this.userDriver.destroy();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_CACHE_SHUTDOWN_OK_1", new Object[]{this.getClass().getName()}));
        }
        this.finalize();
    }

    public boolean existsGroup(CmsDbContext dbc, String groupName) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.existsGroup(dbc, groupName);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForGroupByName = CmsUserCacheKey.forGroupByName(groupName);
        CmsGroup group = (CmsGroup)manager.cacheLookup(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupByName);
        if (group == null) {
            try {
                group = this.userDriver.readGroup(dbc, groupName);
                manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupByName, (Object)group);
                String cacheKeyForGroupById = CmsUserCacheKey.forGroupById(group.getId());
                manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupById, (Object)group);
            }
            catch (CmsDbEntryNotFoundException e) {
                // empty catch block
            }
        }
        return group != null;
    }

    public boolean existsUser(CmsDbContext dbc, String userName) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.existsUser(dbc, userName);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(userName);
        CmsUser user = (CmsUser)manager.cacheLookup(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName);
        if (user == null) {
            try {
                user = this.userDriver.readUser(dbc, userName);
                manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)user);
                String cacheKeyForUserById = CmsUserCacheKey.forUserById(user.getId());
                manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)user);
            }
            catch (CmsDbEntryNotFoundException e) {
                // empty catch block
            }
        }
        return user != null;
    }

    public void fillDefaults(CmsDbContext dbc) throws CmsInitException {
        this.userDriver.fillDefaults(dbc);
    }

    public List<CmsGroup> getGroups(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean includeSubOus, boolean readRoles) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.getGroups(dbc, orgUnit, includeSubOus, readRoles);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForOrgUnitGroups = CmsUserCacheKey.forGroupListOrgUnit(orgUnit.getName(), includeSubOus, readRoles);
        HashMap<String, CmsGroup> cachedOrgUnitGroups = (HashMap<String, CmsGroup>)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitGroups);
        if (cachedOrgUnitGroups == null) {
            List<CmsGroup> dbGroups = this.userDriver.getGroups(dbc, orgUnit, includeSubOus, readRoles);
            cachedOrgUnitGroups = new HashMap<String, CmsGroup>();
            for (CmsGroup groupOfOrgUnit : dbGroups) {
                String cacheKeyForGroupById = CmsUserCacheKey.forGroupById(groupOfOrgUnit.getId());
                manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupById, (Object)groupOfOrgUnit);
                String cacheKeyForGroupByName = CmsUserCacheKey.forGroupByName(groupOfOrgUnit.getName());
                manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupByName, (Object)groupOfOrgUnit);
                cachedOrgUnitGroups.put(groupOfOrgUnit.getName(), groupOfOrgUnit);
            }
            manager.cacheSet(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitGroups, cachedOrgUnitGroups);
        }
        ArrayList<CmsGroup> groups = new ArrayList<CmsGroup>();
        groups.addAll(cachedOrgUnitGroups.values());
        return groups;
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

    public List<CmsUser> getUsers(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean recursive) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.getUsers(dbc, orgUnit, recursive);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForOrgUnitUsers = CmsUserCacheKey.forUserListOrgUnit(orgUnit.getName(), recursive);
        HashMap<String, CmsUser> cachedOrgUnitUsers = (HashMap<String, CmsUser>)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitUsers);
        if (cachedOrgUnitUsers == null) {
            List<CmsUser> dbUsers = this.userDriver.getUsers(dbc, orgUnit, recursive);
            cachedOrgUnitUsers = new HashMap<String, CmsUser>();
            for (CmsUser userOfOrgUnit : dbUsers) {
                String cacheKeyForUserById = CmsUserCacheKey.forUserById(userOfOrgUnit.getId());
                manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)userOfOrgUnit);
                String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(userOfOrgUnit.getName());
                manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)userOfOrgUnit);
                cachedOrgUnitUsers.put(userOfOrgUnit.getName(), userOfOrgUnit);
            }
            manager.cacheSet(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitUsers, cachedOrgUnitUsers);
        }
        ArrayList<CmsUser> users = new ArrayList<CmsUser>();
        users.addAll(cachedOrgUnitUsers.values());
        return users;
    }

    public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List<String> successiveDrivers, CmsDriverManager driverManager) throws CmsInitException {
        Map configuration = configurationManager.getConfiguration();
        String driverName = (String)configuration.get(successiveDrivers.get(0) + ".user.driver");
        successiveDrivers = successiveDrivers.size() > 1 ? successiveDrivers.subList(1, successiveDrivers.size()) : null;
        this.userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurationManager, driverName, successiveDrivers);
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !manager.isInitialized()) {
            return;
        }
        manager.setUserDriver(this);
        if (!CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return;
        }
        OpenCms.getMemoryMonitor().disableCache(new CmsMemoryMonitor.CacheType[]{CmsMemoryMonitor.CacheType.USER});
        OpenCms.getMemoryMonitor().disableCache(new CmsMemoryMonitor.CacheType[]{CmsMemoryMonitor.CacheType.GROUP});
        this.userCacheEventHandler = new CmsUserCacheEventHandler(this.userDriver);
        OpenCms.addCmsEventListener((I_CmsEventListener)this.userCacheEventHandler, (int[])new int[]{2, 11, 29, 30, 31, 5, 6});
    }

    public CmsSqlManager initSqlManager(String classname) {
        return this.userDriver.initSqlManager(classname);
    }

    public void publishAccessControlEntries(CmsDbContext dbc, CmsProject offlineProject, CmsProject onlineProject, CmsUUID offlineId, CmsUUID onlineId) throws CmsDataAccessException {
        this.userDriver.publishAccessControlEntries(dbc, offlineProject, onlineProject, offlineId, onlineId);
    }

    public List<CmsAccessControlEntry> readAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource, boolean inheritedOnly) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.0.0")) {
            return this.userDriver.readAccessControlEntries(dbc, project, resource, inheritedOnly);
        }
        String listCacheKey = CmsVfsCacheKey.forACEList("aceList", resource, inheritedOnly);
        List entries = (List)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_ACE_LIST, project.getUuid(), listCacheKey);
        if (entries != null) {
            return this.userDriver(entries);
        }
        entries = this.userDriver.readAccessControlEntries(dbc, project, resource, inheritedOnly);
        manager.cacheSet(CmsVfsCacheInstanceType.VFS_ACE_LIST, project.getUuid(), listCacheKey, entries);
        return this.userDriver(entries);
    }

    public CmsAccessControlEntry readAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.0.0")) {
            return this.userDriver.readAccessControlEntry(dbc, project, resource, principal);
        }
        String cacheKey = CmsVfsCacheKey.forACE("ace", resource);
        HashMap<String, CmsAccessControlEntry> entries = (HashMap<String, CmsAccessControlEntry>)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_ACE, project.getUuid(), cacheKey);
        if (entries != null) {
            if (entries.containsKey(principal.toString())) {
                CmsAccessControlEntry entry = (CmsAccessControlEntry)entries.get(principal.toString());
                return new CmsAccessControlEntry(entry.getResource(), entry);
            }
        } else {
            entries = new HashMap<String, CmsAccessControlEntry>();
        }
        CmsAccessControlEntry entry = this.userDriver.readAccessControlEntry(dbc, project, resource, principal);
        entries.put(principal.toString(), entry);
        manager.cacheSet(CmsVfsCacheInstanceType.VFS_ACE, project.getUuid(), cacheKey, entries);
        return new CmsAccessControlEntry(entry.getResource(), entry);
    }

    public List<CmsGroup> readChildGroups(CmsDbContext dbc, String groupname) throws CmsDataAccessException {
        return this.userDriver.readChildGroups(dbc, groupname);
    }

    public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.readGroup(dbc, groupId);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForGroupById = CmsUserCacheKey.forGroupById(groupId);
        CmsGroup group = (CmsGroup)manager.cacheLookup(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupById);
        if (group == null) {
            group = this.userDriver.readGroup(dbc, groupId);
            manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupById, (Object)group);
            String cacheKeyForGroupByName = CmsUserCacheKey.forGroupByName(group.getName());
            manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupByName, (Object)group);
        }
        return group;
    }

    public CmsGroup readGroup(CmsDbContext dbc, String groupName) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.readGroup(dbc, groupName);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForGroupByName = CmsUserCacheKey.forGroupByName(groupName);
        CmsGroup group = (CmsGroup)manager.cacheLookup(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupByName);
        if (group == null) {
            group = this.userDriver.readGroup(dbc, groupName);
            manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupByName, (Object)group);
            String cacheKeyForGroupById = CmsUserCacheKey.forGroupById(group.getId());
            manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupById, (Object)group);
        }
        return group;
    }

    public List<CmsGroup> readGroupsOfUser(CmsDbContext dbc, CmsUUID userId, String ouFqn, boolean includeChildOus, String remoteAddress, boolean readRoles) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.readGroupsOfUser(dbc, userId, ouFqn, includeChildOus, remoteAddress, readRoles);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        List<CmsGroup> groups = new ArrayList<CmsGroup>();
        CmsUser user = this.readUser(dbc, userId);
        String cacheKeyForUserGroups = CmsUserCacheKey.forGroupListUserGroups(user.getName(), ouFqn, includeChildOus, readRoles);
        HashMap<String, CmsGroup> usergroups = (HashMap<String, CmsGroup>)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserGroups);
        if (usergroups == null) {
            groups = this.userDriver.readGroupsOfUser(dbc, userId, ouFqn, includeChildOus, remoteAddress, readRoles);
            usergroups = new HashMap<String, CmsGroup>();
            for (CmsGroup groupOfUser : groups) {
                String cacheKeyForGroupsById = CmsUserCacheKey.forGroupById(groupOfUser.getId());
                manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupsById, (Object)groupOfUser);
                String cacheKeyForGroupsByName = CmsUserCacheKey.forGroupByName(groupOfUser.getName());
                manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupsByName, (Object)groupOfUser);
                usergroups.put(groupOfUser.getName(), groupOfUser);
            }
            manager.cacheSet(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserGroups, usergroups);
        } else {
            groups.addAll(usergroups.values());
        }
        return groups;
    }

    public CmsOrganizationalUnit readOrganizationalUnit(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException {
        return this.userDriver.readOrganizationalUnit(dbc, ouFqn);
    }

    public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.readUser(dbc, id);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForUserById = CmsUserCacheKey.forUserById(id);
        CmsUser user = (CmsUser)manager.cacheLookup(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById);
        if (user == null) {
            user = this.userDriver.readUser(dbc, id);
            manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)user);
            String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(user.getName());
            manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)user);
        }
        return user;
    }

    public CmsUser readUser(CmsDbContext dbc, String name) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.readUser(dbc, name);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(name);
        CmsUser user = (CmsUser)manager.cacheLookup(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName);
        if (user == null) {
            user = this.userDriver.readUser(dbc, name);
            manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)user);
            String cacheKeyForUserById = CmsUserCacheKey.forUserById(user.getId());
            manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)user);
        }
        return user;
    }

    public CmsUser readUser(CmsDbContext dbc, String name, String password, String remoteAddress) throws CmsDataAccessException, CmsPasswordEncryptionException {
        return this.userDriver.readUser(dbc, name, password, remoteAddress);
    }

    public Map<String, Object> readUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
        return this.userDriver.readUserInfos(dbc, userId);
    }

    public List<CmsUser> readUsersOfGroup(CmsDbContext dbc, String groupFqn, boolean includeOtherOuUsers) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return this.userDriver.readUsersOfGroup(dbc, groupFqn, includeOtherOuUsers);
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForGroupUsers = CmsUserCacheKey.forUserListGroupUsers(groupFqn, includeOtherOuUsers);
        HashMap<String, CmsUser> cachedGroupUsers = (HashMap<String, CmsUser>)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupUsers);
        List<CmsUser> users = new ArrayList<CmsUser>();
        if (cachedGroupUsers != null) {
            users.addAll(cachedGroupUsers.values());
        } else {
            users = this.userDriver.readUsersOfGroup(dbc, groupFqn, includeOtherOuUsers);
            cachedGroupUsers = new HashMap<String, CmsUser>();
            for (CmsUser dbUser : users) {
                String cacheKeyForUserById = CmsUserCacheKey.forUserById(dbUser.getId());
                manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)dbUser);
                String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(dbUser.getName());
                manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)dbUser);
                cachedGroupUsers.put(dbUser.getName(), dbUser);
            }
            manager.cacheSet(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupUsers, cachedGroupUsers);
        }
        return users;
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
    }

    public void setUsersOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsUser user) throws CmsDataAccessException {
        this.userDriver.setUsersOrganizationalUnit(dbc, orgUnit, user);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + this.getClass().getName() + "]:\n");
        buffer.append("No driver specific info available\n");
        buffer.append(this.userDriver.toString());
        return buffer.toString();
    }

    public void writeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsAccessControlEntry acEntry) throws CmsDataAccessException {
        this.userDriver.writeAccessControlEntry(dbc, project, acEntry);
    }

    public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {
        this.userDriver.writeGroup(dbc, group);
    }

    public void writeOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
        this.userDriver.writeOrganizationalUnit(dbc, organizationalUnit);
    }

    public void writePassword(CmsDbContext dbc, String userFqn, String oldPassword, String newPassword) throws CmsDataAccessException, CmsPasswordEncryptionException {
        this.userDriver.writePassword(dbc, userFqn, oldPassword, newPassword);
    }

    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {
    	//System.out.println(" writeUser - ocee cache"); 
    	
    	CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
        	//System.out.println(" entro en opcion manager == null");
        	this.userDriver.writeUser(dbc, user);
            return;
        }
        this.userCacheEventHandler.commitChanges(dbc, manager);
        String cacheKeyForUserById = CmsUserCacheKey.forUserById(user.getId());
       
        this.userDriver.writeUser(dbc, user);
        manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)user);
        String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(user.getName());
        manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)user);
   
    	
       
    }

    public void writeUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value) throws CmsDataAccessException {
        this.userDriver.writeUserInfo(dbc, userId, key, value);
    }

    private List<CmsAccessControlEntry> userDriver(List<CmsAccessControlEntry> entries) {
        ArrayList<CmsAccessControlEntry> result = new ArrayList<CmsAccessControlEntry>(entries.size());
        for (CmsAccessControlEntry entry : entries) {
            result.add(new CmsAccessControlEntry(entry.getResource(), entry));
        }
        return result;
    }

	@Override
	public CmsUserExternalProvider readExternalUserProvider(CmsDbContext dbc, String providerName, String id)
			throws CmsDataAccessException {
		
		return this.userDriver.readExternalUserProvider(dbc, providerName, id);
	}

	@Override
	public void createExternalProvider(CmsDbContext dbc, CmsUserExternalProvider externalProvider)
			throws CmsDataAccessException {
		this.userDriver.createExternalProvider(dbc, externalProvider);
		
	}

	@Override
	public void updateExternalProvider(CmsDbContext dbc, CmsUserExternalProvider userProv)
			throws CmsDataAccessException {
		this.userDriver.updateExternalProvider(dbc, userProv);
		
	}

	@Override
	public void writeTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
		this.userDriver.writeTwoFactor(dbc,twoFactor);
	}

	@Override
	public void updateTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
		this.userDriver.updateTwoFactor(dbc,twoFactor);
	}

	@Override
	public CmsUserTwoFactor readTwoFactor(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {

		return this.userDriver.readTwoFactor(dbc,id);
	}

	@Override
	public void deleteTwoFactor(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
		this.userDriver.deleteTwoFactor(dbc,userId);
		
	}

	@Override
	public CmsUserTwoFactor readTwoFactorByTemp(CmsDbContext dbc, String tempId) throws CmsDataAccessException {
		
		return this.userDriver.readTwoFactorByTemp(dbc,tempId);
	}
}

