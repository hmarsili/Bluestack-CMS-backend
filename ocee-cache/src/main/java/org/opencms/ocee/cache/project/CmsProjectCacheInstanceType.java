/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache.project;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opencms.ocee.cache.A_CmsCacheInstanceType;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.project.CmsCacheProjectResources;
import org.opencms.ocee.cache.project.CmsCachePublishedResources;
import org.opencms.ocee.cache.project.CmsCacheStaticExportResourceName;

public final class CmsProjectCacheInstanceType
extends A_CmsCacheInstanceType {
    public static final CmsProjectCacheInstanceType PROJECT_RESOURCES = new CmsProjectCacheInstanceType("projectResources", CmsCacheProjectResources.class.getName());
    public static final CmsProjectCacheInstanceType PUBLISHED_RESOURCES = new CmsProjectCacheInstanceType("publishedResources", CmsCachePublishedResources.class.getName());
    public static final CmsProjectCacheInstanceType STATIC_EXPORT_RESOURCE_NAME = new CmsProjectCacheInstanceType("staticExportResourceName", CmsCacheStaticExportResourceName.class.getName());
    private static final CmsProjectCacheInstanceType[] \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = new CmsProjectCacheInstanceType[]{PROJECT_RESOURCES, PUBLISHED_RESOURCES, STATIC_EXPORT_RESOURCE_NAME};
    public static final List<CmsProjectCacheInstanceType> VALUES = Collections.unmodifiableList(Arrays.asList(\u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return));

    private CmsProjectCacheInstanceType(String type, String defaultClass) {
        super(type, defaultClass, false);
    }

    public static I_CmsCacheInstanceType valueOf(String value) {
        return CmsProjectCacheInstanceType.valueOf(VALUES, value);
    }

    public int getOrder() {
        return VALUES.indexOf(this);
    }
}

