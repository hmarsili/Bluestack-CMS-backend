/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache.user;

import org.opencms.util.CmsUUID;

public final class CmsUserCacheKey {
    public static final String C_CACHE_KEY_GROUP_ID = "groupId";
    public static final String C_CACHE_KEY_GROUP_NAME = "groupName";
    public static final String C_CACHE_KEY_GROUP_USER_GROUPS = "userGroups";
    public static final String C_CACHE_KEY_USER_GROUP_USERS = "userGroupUsers";
    public static final String C_CACHE_KEY_USER_ID = "userId";
    public static final String C_CACHE_KEY_USER_NAME = "userName";
    public static final String C_CACHE_KEY_USERS = "users";
    public static final String C_CACHE_KEY_GROUPS = "groups";
    private static final String under = "_";

    private CmsUserCacheKey() {
    }

    public static String forGroupById(CmsUUID groupId) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("groupId");
        cacheBuffer.append("_");
        cacheBuffer.append((Object)groupId);
        return cacheBuffer.toString();
    }

    public static String forGroupByName(String groupFqn) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("groupName");
        cacheBuffer.append("_");
        cacheBuffer.append(groupFqn);
        return cacheBuffer.toString();
    }

    public static String forGroupListUserGroups(String userFqn, String postFix) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("userGroups");
        cacheBuffer.append("_");
        cacheBuffer.append(userFqn);
        cacheBuffer.append("_");
        cacheBuffer.append(postFix);
        return cacheBuffer.toString();
    }

    public static String forGroupListUserGroups(String userFqn, String ouFqn, boolean includeChildOus, boolean readRoles) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("userGroups");
        cacheBuffer.append("_");
        cacheBuffer.append(userFqn);
        cacheBuffer.append("_");
        cacheBuffer.append(ouFqn);
        cacheBuffer.append("_");
        cacheBuffer.append(includeChildOus);
        cacheBuffer.append("_");
        cacheBuffer.append(readRoles);
        return cacheBuffer.toString();
    }

    public static String forUserListOrgUnit(String ouFqn, boolean recursive) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("users");
        cacheBuffer.append("_");
        cacheBuffer.append(ouFqn);
        cacheBuffer.append("_");
        cacheBuffer.append(recursive);
        return cacheBuffer.toString();
    }

    public static String forGroupListOrgUnit(String ouFqn, boolean recursive, boolean readRoles) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("groups");
        cacheBuffer.append("_");
        cacheBuffer.append(ouFqn);
        cacheBuffer.append("_");
        cacheBuffer.append(recursive);
        cacheBuffer.append("_");
        cacheBuffer.append(readRoles);
        return cacheBuffer.toString();
    }

    public static String forUserById(CmsUUID id) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("userId");
        cacheBuffer.append("_");
        cacheBuffer.append((Object)id);
        return cacheBuffer.toString();
    }

    public static String forUserByName(String userFqn) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("userName");
        cacheBuffer.append("_");
        cacheBuffer.append(userFqn);
        return cacheBuffer.toString();
    }

    public static String forUserListGroupUsers(String groupFqn, boolean includeOtherOuUsers) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("userGroupUsers");
        cacheBuffer.append("_");
        cacheBuffer.append(groupFqn);
        cacheBuffer.append("_");
        cacheBuffer.append(includeOtherOuUsers);
        return cacheBuffer.toString();
    }

    public static String matchGroupListUserGroups(String key, String userFqn) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("userGroups");
        cacheBuffer.append("_");
        cacheBuffer.append(userFqn);
        cacheBuffer.append("_");
        if (!key.startsWith(cacheBuffer.toString())) {
            return null;
        }
        return key.substring(cacheBuffer.length());
    }

    public static boolean matchUserListGroupUsers(String key, String ouFqn, Boolean includeOtherOuUsers) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append("userGroupUsers");
        cacheBuffer.append("_");
        if (ouFqn != null) {
            cacheBuffer.append(ouFqn);
            if (!ouFqn.endsWith("/")) {
                cacheBuffer.append("/");
            }
        }
        String postFix = "_" + includeOtherOuUsers;
        return key.startsWith(cacheBuffer.toString()) && (includeOtherOuUsers == null || key.endsWith(postFix));
    }
}

