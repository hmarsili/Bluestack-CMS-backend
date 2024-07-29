/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache.project;

import org.opencms.ocee.cache.A_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.project.CmsProjectCacheInstanceType;

public class CmsCacheStaticExportResourceName
extends A_CmsCacheInstance {
    public static final CmsProjectCacheInstanceType TYPE = CmsProjectCacheInstanceType.STATIC_EXPORT_RESOURCE_NAME;

    public CmsCacheStaticExportResourceName() {
        super(TYPE);
    }
}

