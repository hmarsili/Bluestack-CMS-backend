/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache.vfs;

import org.opencms.ocee.cache.A_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;

public class CmsCacheResourceLists
extends A_CmsCacheInstance {
    public static final CmsVfsCacheInstanceType TYPE = CmsVfsCacheInstanceType.VFS_RESOURCELISTS;

    public CmsCacheResourceLists() {
        super(TYPE);
    }
}

