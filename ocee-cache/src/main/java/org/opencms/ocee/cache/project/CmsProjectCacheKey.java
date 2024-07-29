/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache.project;

import org.opencms.util.CmsUUID;

public final class CmsProjectCacheKey {
    public static final String C_CACHE_KEY_PROJECT_RESOURCES = "projectResources";
    public static final String C_CACHE_KEY_PUBLISHED_RESOURCES = "publishedResources";
    public static final String C_CACHE_KEY_STATIC_EXPORT_RESOURCE_NAME = "staticExportResourceName";

    private CmsProjectCacheKey() {
    }

    public static String getCacheKeyForProjectResources(CmsUUID projectId) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append("projectResources");
        cacheKey.append('_');
        cacheKey.append(projectId.toString());
        return cacheKey.toString();
    }

    public static String getCacheKeyForPublishedResources(CmsUUID id) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append("publishedResources");
        cacheKey.append('_');
        cacheKey.append(id.toString());
        return cacheKey.toString();
    }

    public static String getCacheKeyStaticEportResourceName(String resourceName) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append("staticExportResourceName");
        cacheKey.append('_');
        cacheKey.append(resourceName);
        return cacheKey.toString();
    }
}

