/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache.user;

import org.opencms.ocee.cache.A_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.user.CmsUserCacheInstanceType;

public class CmsCacheGroupList
extends A_CmsCacheInstance {
    public static final CmsUserCacheInstanceType TYPE = CmsUserCacheInstanceType.GROUP_LIST;

    public CmsCacheGroupList() {
        super(TYPE);
    }
}

