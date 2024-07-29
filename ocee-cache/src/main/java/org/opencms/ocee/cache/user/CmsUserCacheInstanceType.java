/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opencms.ocee.cache.A_CmsCacheInstanceType;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.user.CmsCacheGroup;
import org.opencms.ocee.cache.user.CmsCacheGroupList;
import org.opencms.ocee.cache.user.CmsCacheUser;
import org.opencms.ocee.cache.user.CmsCacheUserList;

public final class CmsUserCacheInstanceType
extends A_CmsCacheInstanceType {
    public static final CmsUserCacheInstanceType GROUP = new CmsUserCacheInstanceType("group", CmsCacheGroup.class.getName());
    public static final CmsUserCacheInstanceType GROUP_LIST = new CmsUserCacheInstanceType("groupList", CmsCacheGroupList.class.getName());
    public static final CmsUserCacheInstanceType USER = new CmsUserCacheInstanceType("user", CmsCacheUser.class.getName());
    public static final CmsUserCacheInstanceType USER_LIST = new CmsUserCacheInstanceType("userList", CmsCacheUserList.class.getName());
    private static final CmsUserCacheInstanceType[] \u00d5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = new CmsUserCacheInstanceType[]{USER, USER_LIST, GROUP, GROUP_LIST};
    public static final List<CmsUserCacheInstanceType> VALUES = Collections.unmodifiableList(Arrays.asList(\u00d5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class));

    private CmsUserCacheInstanceType(String type, String defaultClass) {
        super(type, defaultClass, false);
    }

    public static I_CmsCacheInstanceType valueOf(String value) {
        return CmsUserCacheInstanceType.valueOf(VALUES, value);
    }

    public int getOrder() {
        return VALUES.indexOf(this);
    }
}

