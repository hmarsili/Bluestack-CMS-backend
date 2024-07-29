
package org.opencms.ocee.cache.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.user.CmsUserCacheInstanceType;
import org.opencms.ocee.cache.user.CmsUserCacheKey;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

public class CmsUserCacheEventHandler
implements I_CmsEventListener {
    private static final Log LOG = CmsLog.getLog(CmsUserCacheEventHandler.class);
    protected I_CmsUserDriver groupId;
    private List<CmsCacheUserEntry> cachedUsers = new ArrayList<CmsCacheUserEntry>();

    public CmsUserCacheEventHandler(I_CmsUserDriver userDriver) {
        this.groupId = userDriver;
    }

    public void cmsEvent(CmsEvent event) {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null) {
            return;
        }
        String action = "";
        if (event.getData() != null) {
            action = (String)event.getData().get("userAction");
        }
        if (event.getType() == 5 || event.getType() == 6) {
            this.flushAll(manager);
            return;
        }
        if (action == null) {
            return;
        }
        try {
            switch (event.getType()) {
                case 29: {
                    if (action.equals("writeUser")) {
                        String userId = (String)event.getData().get("userId");
                        String oldUserName = (String)event.getData().get("userName");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.writeUser);
                        change.setUserId(new CmsUUID(userId));
                        change.setUserName(oldUserName);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("deleteUser")) {
                        String userId = (String)event.getData().get("userId");
                        String userName = (String)event.getData().get("userName");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.deleteUser);
                        change.setUserId(new CmsUUID(userId));
                        change.setUserName(userName);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("setOu")) {
                        String userId = (String)event.getData().get("userId");
                        String ouName = (String)event.getData().get("ouName");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.setOu);
                        change.setUserId(new CmsUUID(userId));
                        change.setOuName(ouName);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("addUserToGroup")) {
                    	CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.addUserToGroup);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("createUser")) {
                        String userId = (String)event.getData().get("userId");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.createUser);
                        change.setUserId(new CmsUUID(userId));
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("removeUserFromGroup")) {
                    	CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.removeUserFromGroup);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("resetPassword")) {
                        String userId = (String)event.getData().get("userId");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.createUser);
                        change.setUserId(new CmsUUID(userId));
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (!LOG.isDebugEnabled()) break;
                    LOG.debug("Unknown action " + action);
                    break;
                }
                case 30: {
                    if (action.equals("createOu")) {
                        String ouName = (String)event.getData().get("ouName");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.createOu);
                        change.setOuName(ouName);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("deleteOu")) {
                        String ouName = (String)event.getData().get("ouName");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.deleteOu);
                        change.setOuName(ouName);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (!LOG.isDebugEnabled()) break;
                    LOG.debug("Unknown action " + action);
                    break;
                }
                case 31: {
                    if (action.equals("createGroup")) {
                        String groupId = (String)event.getData().get("groupId");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.createGroup);
                        change.setGroupId(new CmsUUID(groupId));
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("writeGroup")) {
                        String groupId = (String)event.getData().get("groupId");
                        String oldGroupName = (String)event.getData().get("groupName");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.writeGroup);
                        change.setGroupId(new CmsUUID(groupId));
                        change.setGroupName(oldGroupName);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (action.equals("deleteGroup")) {
                        String groupId = (String)event.getData().get("groupId");
                        String groupName = (String)event.getData().get("groupName");
                        CmsCacheUserEntry change = new CmsCacheUserEntry(ChangeType.deleteGroup);
                        change.setGroupId(new CmsUUID(groupId));
                        change.setGroupName(groupName);
                        this.cachedUsers.add(change);
                        break;
                    }
                    if (!LOG.isDebugEnabled()) break;
                    LOG.debug((Object)("Unknown action " + action));
                    break;
                }
            }
        }
        catch (Throwable e) {
            LOG.error((Object)e.getLocalizedMessage(), e);
        }
    }

    public void commitChanges(CmsDbContext dbc, CmsCacheManager manager) {
        if (this.cachedUsers.isEmpty()) {
            return;
        }
        ArrayList<CmsCacheUserEntry> laundry = new ArrayList<CmsCacheUserEntry>(this.cachedUsers);
        this.cachedUsers.clear();
        for (CmsCacheUserEntry change : laundry) {
            try {
                switch (change.userId()) {
                    case createGroup: {
                        CmsUUID groupId = change.getGroupId();
                        CmsGroup group = this.groupId.readGroup(dbc, groupId);
                        this.addGroup(manager, group);
                        break;
                    }
                    case createOu: {
                        String ouName = change.getOuName();
                        this.addOu(manager, ouName);
                        break;
                    }
                    case createUser: {
                        CmsUUID userId = change.getUserId();
                        CmsUser user = this.groupId.readUser(dbc, userId);
                        this.addUser(manager, user);
                        break;
                    }
                    case addUserToGroup: {
                        this.addUserToGroup(manager);
                        break;
                    }
                    case deleteGroup: {
                        CmsUUID flushGroupId = change.getGroupId();
                        String groupName = change.getGroupName();
                        this.flushGroup(manager, flushGroupId, groupName);
                        break;
                    }
                    case deleteOu: {
                        String flushOuName = change.getOuName();
                        this.flushOu(manager, flushOuName);
                        break;
                    }
                    case deleteUser: {
                        CmsUUID flushUserId = change.getUserId();
                        String userName = change.getUserName();
                        this.flushUser(manager, flushUserId, userName);
                        break;
                    }
                    case setOu: {
                        CmsUUID moveUserId = change.getUserId();
                        CmsUser moveUser = this.groupId.readUser(dbc, moveUserId);
                        String moveOuName = change.getOuName();
                        this.moveUserToOu(manager, moveOuName, moveUser);
                        break;
                    }
                    case removeUserFromGroup: {
                        this.removeUserFromGroup(manager);
                        break;
                    }
                    case writeGroup: {
                        CmsUUID updateGroupId = change.getGroupId();
                        CmsGroup updatGroup = this.groupId.readGroup(dbc, updateGroupId);
                        String oldGroupName = change.getGroupName();
                        this.updateGroup(manager, updatGroup, oldGroupName);
                        break;
                    }
                    case writeUser: {
                        CmsUUID updateUserId = change.getUserId();
                        CmsUser updateUser = this.groupId.readUser(dbc, updateUserId);
                        String oldUserName = change.getUserName();
                        this.updateUser(manager, updateUser, oldUserName);
                        break;
                    }
                    default: {
                        if (!LOG.isDebugEnabled()) continue;
                        LOG.debug("Unknown laundry type " + change.userId());
                        break;
                    }
                }
            }
            catch (Throwable e) {
                LOG.warn((Object)e.getLocalizedMessage(), e);
            }
        }
    }

    protected void addGroup(CmsCacheManager manager, CmsGroup group) {
        String cacheKeyForGroupsById = CmsUserCacheKey.forGroupById(group.getId());
        manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupsById, (Object)group);
        String cacheKeyForGroupsByName = CmsUserCacheKey.forGroupByName(group.getName());
        manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupsByName, (Object)group);
        String ou = group.getOuFqn();
        String cacheKeyForOrgUnit = CmsUserCacheKey.forGroupListOrgUnit(ou, false, group.isRole());
        Map cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
        if (cachedOrgUnit != null) {
            cachedOrgUnit.put(group.getName(), group);
        }
        while (ou != null) {
            cacheKeyForOrgUnit = CmsUserCacheKey.forGroupListOrgUnit(ou, true, group.isRole());
            cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
            if (cachedOrgUnit != null) {
                cachedOrgUnit.put(group.getName(), group);
            }
            ou = CmsOrganizationalUnit.getParentFqn((String)ou);
        }
    }

    protected void addOu(CmsCacheManager manager, String ouName) {
        String ou = CmsOrganizationalUnit.getParentFqn((String)ouName);
        while (ou != null) {
            String cacheKeyForOrgUnit = CmsUserCacheKey.forGroupListOrgUnit(ou, true, true);
            manager.cacheRemove(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit);
            ou = CmsOrganizationalUnit.getParentFqn((String)ou);
        }
    }

    protected void addUser(CmsCacheManager manager, CmsUser user) {
        String cacheKeyForUserById = CmsUserCacheKey.forUserById(user.getId());
        manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)user);
        String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(user.getName());
        manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)user);
        String ou = user.getOuFqn();
        String cacheKeyForOrgUnit = CmsUserCacheKey.forUserListOrgUnit(ou, false);
        Map cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
        if (cachedOrgUnit != null) {
            cachedOrgUnit.put(user.getName(), user);
        }
        while (ou != null) {
            cacheKeyForOrgUnit = CmsUserCacheKey.forUserListOrgUnit(ou, true);
            cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
            if (cachedOrgUnit != null) {
                cachedOrgUnit.put(user.getName(), user);
            }
            ou = CmsOrganizationalUnit.getParentFqn((String)ou);
        }
    }

    protected void addUserToGroup(CmsCacheManager manager) {
        Map<String, Object> all = manager.getCacheInstance(CmsUserCacheInstanceType.USER_LIST).getCacheOnline();
        Iterator<Map.Entry<String, Object>> it = all.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            if (!CmsUserCacheKey.matchUserListGroupUsers(entry.getKey(), null, null)) continue;
            it.remove();
        }
        manager.getCacheInstance(CmsUserCacheInstanceType.GROUP_LIST).getCacheOnline().clear();
    }

    protected void flushAll(CmsCacheManager manager) {
        manager.getCacheInstance(CmsUserCacheInstanceType.USER).getCacheOnline().clear();
        manager.getCacheInstance(CmsUserCacheInstanceType.USER_LIST).getCacheOnline().clear();
        manager.getCacheInstance(CmsUserCacheInstanceType.GROUP).getCacheOnline().clear();
        manager.getCacheInstance(CmsUserCacheInstanceType.GROUP_LIST).getCacheOnline().clear();
        this.cachedUsers.clear();
    }

    protected void flushGroup(CmsCacheManager manager, CmsUUID groupId, String groupName) {
        String cacheKeyForGroupById = CmsUserCacheKey.forGroupById(groupId);
        manager.cacheRemove(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupById);
        String cacheKeyForGroupByName = CmsUserCacheKey.forGroupByName(groupName);
        manager.cacheRemove(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupByName);
        String ouFqn = CmsOrganizationalUnit.getParentFqn((String)groupName);
        boolean readRoles = true;
        while (readRoles) {
            String cacheKeyForOrgUnit = CmsUserCacheKey.forGroupListOrgUnit(ouFqn, false, readRoles);
            Map cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
            if (cachedOrgUnit != null) {
                cachedOrgUnit.remove(groupName);
            }
            while (ouFqn != null) {
                cacheKeyForOrgUnit = CmsUserCacheKey.forGroupListOrgUnit(ouFqn, true, readRoles);
                cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
                if (cachedOrgUnit != null) {
                    cachedOrgUnit.remove(groupName);
                }
                ouFqn = CmsOrganizationalUnit.getParentFqn((String)ouFqn);
            }
            readRoles = !readRoles;
        }
        String cacheKeyForGroupUsers = CmsUserCacheKey.forUserListGroupUsers(groupName, false);
        manager.cacheRemove(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupUsers);
        cacheKeyForGroupUsers = CmsUserCacheKey.forUserListGroupUsers(groupName, true);
        manager.cacheRemove(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupUsers);
        Map<String, Object> all = manager.getCacheInstance(CmsUserCacheInstanceType.GROUP_LIST).getCacheOnline();
        for (Map.Entry<String, Object> entry : all.entrySet()) {
            Map cachedUserGroups = CmsCollectionsGenericWrapper.map((Object)entry.getValue());
            cachedUserGroups.remove(groupName);
        }
    }

    protected void flushOu(CmsCacheManager manager, String ouName) {
        String cacheKeyForOrgUnitUsers = CmsUserCacheKey.forUserListOrgUnit(ouName, false);
        manager.cacheRemove(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitUsers);
        String ouFqn = ouName;
        while (ouFqn != null) {
            cacheKeyForOrgUnitUsers = CmsUserCacheKey.forUserListOrgUnit(ouFqn, true);
            manager.cacheRemove(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitUsers);
            ouFqn = CmsOrganizationalUnit.getParentFqn((String)ouFqn);
        }
        boolean readRoles = true;
        while (readRoles) {
            String cacheKeyForOrgUnitGroups = CmsUserCacheKey.forGroupListOrgUnit(ouName, false, readRoles);
            manager.cacheRemove(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitGroups);
            ouFqn = ouName;
            while (ouFqn != null) {
                cacheKeyForOrgUnitGroups = CmsUserCacheKey.forGroupListOrgUnit(ouFqn, true, readRoles);
                manager.cacheRemove(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnitGroups);
                ouFqn = CmsOrganizationalUnit.getParentFqn((String)ouFqn);
            }
            readRoles = !readRoles;
        }
    }

    protected void flushUser(CmsCacheManager manager, CmsUUID userId, String userName) {
        String cacheKeyForUserById = CmsUserCacheKey.forUserById(userId);
        manager.cacheRemove(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById);
        String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(userName);
        manager.cacheRemove(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName);
        String ouFqn = CmsOrganizationalUnit.getParentFqn((String)userName);
        String cacheKeyForOrgUnit = CmsUserCacheKey.forUserListOrgUnit(ouFqn, false);
        Map cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
        if (cachedOrgUnit != null) {
            cachedOrgUnit.remove(userName);
        }
        while (ouFqn != null) {
            cacheKeyForOrgUnit = CmsUserCacheKey.forUserListOrgUnit(ouFqn, true);
            cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
            if (cachedOrgUnit != null) {
                cachedOrgUnit.remove(userName);
            }
            boolean readRoles = true;
            while (readRoles) {
                String cacheKeyForUserGroups = CmsUserCacheKey.forGroupListUserGroups(userName, ouFqn, true, readRoles);
                manager.cacheRemove(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserGroups);
                readRoles = !readRoles;
            }
            ouFqn = CmsOrganizationalUnit.getParentFqn((String)ouFqn);
        }
        boolean includeChildOUs = false;
        boolean readRoles = true;
        while (readRoles) {
            String cacheKeyForUserGroups = CmsUserCacheKey.forGroupListUserGroups(userName, ouFqn, includeChildOUs, readRoles);
            manager.cacheRemove(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserGroups);
            readRoles = !readRoles;
        }
        Map<String, Object> all = manager.getCacheInstance(CmsUserCacheInstanceType.USER_LIST).getCacheOnline();
        for (Map.Entry<String, Object> entry : all.entrySet()) {
            if (!CmsUserCacheKey.matchUserListGroupUsers(entry.getKey(), null, null)) continue;
            Map cachedGroupUsers = CmsCollectionsGenericWrapper.map((Object)entry.getValue());
            cachedGroupUsers.remove(userName);
        }
    }

    protected void moveUserToOu(CmsCacheManager manager, String oldOu, CmsUser user) {
        this.flushUser(manager, user.getId(), (CmsStringUtil.isEmptyOrWhitespaceOnly((String)oldOu) ? "" : new StringBuilder().append(oldOu).append("/").toString()) + user.getSimpleName());
        this.addUser(manager, user);
    }

    protected void removeUserFromGroup(CmsCacheManager manager) {
        Map<String, Object> all = manager.getCacheInstance(CmsUserCacheInstanceType.USER_LIST).getCacheOnline();
        Iterator<Map.Entry<String, Object>> it = all.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            if (!CmsUserCacheKey.matchUserListGroupUsers(entry.getKey(), null, null)) continue;
            it.remove();
        }
        manager.getCacheInstance(CmsUserCacheInstanceType.GROUP_LIST).getCacheOnline().clear();
    }

    protected void updateGroup(CmsCacheManager manager, CmsGroup group, String oldGroupName) {
        boolean diffName;
        String cacheKeyForGroupsById = CmsUserCacheKey.forGroupById(group.getId());
        manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupsById, (Object)group);
        String cacheKeyForGroupsByName = CmsUserCacheKey.forGroupByName(group.getName());
        manager.cacheSet(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupsByName, (Object)group);
        boolean bl = diffName = !oldGroupName.equals(group.getName());
        if (diffName) {
            String newKey;
            cacheKeyForGroupsByName = CmsUserCacheKey.forGroupByName(oldGroupName);
            manager.cacheRemove(CmsUserCacheInstanceType.GROUP, CmsProject.ONLINE_PROJECT_ID, cacheKeyForGroupsByName);
            String keyForUserGroupUsers = CmsUserCacheKey.forUserListGroupUsers(oldGroupName, false);
            Map cachedGroupUsers = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, keyForUserGroupUsers));
            if (cachedGroupUsers != null) {
                manager.cacheRemove(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, keyForUserGroupUsers);
                newKey = CmsUserCacheKey.forUserListGroupUsers(group.getName(), false);
                manager.cacheSet(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, newKey, cachedGroupUsers);
            }
            if ((cachedGroupUsers = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, keyForUserGroupUsers = CmsUserCacheKey.forUserListGroupUsers(oldGroupName, true)))) != null) {
                manager.cacheRemove(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, keyForUserGroupUsers);
                newKey = CmsUserCacheKey.forUserListGroupUsers(group.getName(), true);
                manager.cacheSet(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, newKey, cachedGroupUsers);
            }
        }
        String ouFqn = group.getOuFqn();
        boolean readRoles = true;
        while (readRoles) {
            String cacheKeyForOrgUnit = CmsUserCacheKey.forGroupListOrgUnit(ouFqn, false, readRoles);
            Map cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
            if (cachedOrgUnit != null) {
                if (diffName) {
                    cachedOrgUnit.remove(oldGroupName);
                }
                cachedOrgUnit.put(group.getName(), group);
            }
            while (ouFqn != null) {
                cacheKeyForOrgUnit = CmsUserCacheKey.forGroupListOrgUnit(ouFqn, true, readRoles);
                cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
                if (cachedOrgUnit != null) {
                    if (diffName) {
                        cachedOrgUnit.remove(oldGroupName);
                    }
                    cachedOrgUnit.put(group.getName(), group);
                }
                ouFqn = CmsOrganizationalUnit.getParentFqn((String)ouFqn);
            }
            readRoles = !readRoles;
        }
        HashMap<String, Object> allUserGroups = new HashMap<String, Object>(manager.getCacheInstance(CmsUserCacheInstanceType.GROUP_LIST).getCacheOnline());
        for (Map.Entry<String, Object> entry : allUserGroups.entrySet()) {
            Map cachedUserGroups = CmsCollectionsGenericWrapper.map((Object)entry.getValue());
            if (!cachedUserGroups.containsKey(oldGroupName)) continue;
            if (diffName) {
                cachedUserGroups.remove(oldGroupName);
            }
            cachedUserGroups.put(group.getName(), group);
        }
    }

    protected void updateUser(CmsCacheManager manager, CmsUser user, String oldUserName) {
        boolean diffName;
        Map cachedOrgUnit;
        String cacheKeyForOrgUnit;
        String ouFqn;
        String cacheKeyForUserById = CmsUserCacheKey.forUserById(user.getId());
        manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserById, (Object)user);
        String cacheKeyForUserByName = CmsUserCacheKey.forUserByName(user.getName());
        manager.cacheSet(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName, (Object)user);
        boolean bl = diffName = !oldUserName.equals(user.getName());
        if (diffName) {
            cacheKeyForUserByName = CmsUserCacheKey.forUserByName(oldUserName);
            manager.cacheRemove(CmsUserCacheInstanceType.USER, CmsProject.ONLINE_PROJECT_ID, cacheKeyForUserByName);
            HashMap<String, Object> allUserGroups = new HashMap<String, Object>(manager.getCacheInstance(CmsUserCacheInstanceType.GROUP_LIST).getCacheOnline());
            for (Map.Entry<String, Object> entry : allUserGroups.entrySet()) {
                String postFix = CmsUserCacheKey.matchGroupListUserGroups(entry.getKey(), oldUserName);
                if (postFix == null) continue;
                Map cachedUserGroups = CmsCollectionsGenericWrapper.map((Object)entry.getValue());
                manager.cacheRemove(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, entry.getKey());
                String newKey = CmsUserCacheKey.forGroupListUserGroups(user.getName(), postFix);
                manager.cacheSet(CmsUserCacheInstanceType.GROUP_LIST, CmsProject.ONLINE_PROJECT_ID, newKey, cachedUserGroups);
            }
        }
        if ((cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit = CmsUserCacheKey.forUserListOrgUnit(ouFqn = user.getOuFqn(), false)))) != null) {
            if (diffName) {
                cachedOrgUnit.remove(oldUserName);
            }
            cachedOrgUnit.put(user.getName(), user);
        }
        while (ouFqn != null) {
            cacheKeyForOrgUnit = CmsUserCacheKey.forUserListOrgUnit(ouFqn, true);
            cachedOrgUnit = CmsCollectionsGenericWrapper.map((Object)manager.cacheLookup(CmsUserCacheInstanceType.USER_LIST, CmsProject.ONLINE_PROJECT_ID, cacheKeyForOrgUnit));
            if (cachedOrgUnit != null) {
                if (diffName) {
                    cachedOrgUnit.remove(oldUserName);
                }
                cachedOrgUnit.put(user.getName(), user);
            }
            ouFqn = CmsOrganizationalUnit.getParentFqn((String)ouFqn);
        }
        Map<String, Object> allGroupUsers = manager.getCacheInstance(CmsUserCacheInstanceType.USER_LIST).getCacheOnline();
        for (Map.Entry<String, Object> entry : allGroupUsers.entrySet()) {
            Map cachedGroupUsers = CmsCollectionsGenericWrapper.map((Object)entry.getValue());
            if (!cachedGroupUsers.containsKey(oldUserName)) continue;
            if (diffName) {
                cachedGroupUsers.remove(oldUserName);
            }
            cachedGroupUsers.put(user.getName(), user);
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static enum ChangeType {
    	createGroup,
        createOu,
        createUser,
        addUserToGroup,
        deleteGroup,
        deleteOu,
        deleteUser,
        setOu,
        removeUserFromGroup,
        writeGroup,
        writeUser;
        

        private ChangeType() {
        }
    }

    class CmsCacheUserEntry {
        private ChangeType changeType;
        private CmsUUID groupId;
        private String groupName;
        private String ouName;
        private CmsUUID userId;
        private String userName;

        public CmsCacheUserEntry(ChangeType changeType) {
            this.changeType = changeType;
        }

        public ChangeType userId() {
            return this.changeType;
        }

        public CmsUUID getGroupId() {
            return this.groupId;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getOuName() {
            return this.ouName;
        }

        public CmsUUID getUserId() {
            return this.userId;
        }

        public String getUserName() {
            return this.userName;
        }

        public void setGroupId(CmsUUID groupId) {
            this.groupId = groupId;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public void setOuName(String ouName) {
            this.ouName = ouName;
        }

        public void setUserId(CmsUUID userId) {
            this.userId = userId;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

}

