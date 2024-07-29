/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.db.CmsResourceState
 *  org.opencms.file.CmsResource
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache.vfs;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

public final class CmsVfsCacheKey {
    public static final String C_CACHE_KEY_ACE = "ace";
    public static final String C_CACHE_KEY_ACE_LIST = "aceList";
    public static final String C_CACHE_KEY_PARENT_FOLDER = "parent_folder";
    public static final String C_CACHE_KEY_PROPERTIES = "properties";
    public static final String C_CACHE_KEY_PROPERTY = "property";
    public static final String C_CACHE_KEY_PROPERTY_DEFINITION = "property_definition";
    public static final String C_CACHE_KEY_PROPERTY_DEFINITION_LIST = "property_definition_list";
    public static final String C_CACHE_KEY_RELATIONS = "resource_relations";
    public static final String C_CACHE_KEY_RESOURCES = "resources";
    public static final String C_CACHE_KEY_RESOURCE_TREE = "resource_tree";
    public static final String C_CACHE_KEY_SIBLINGS = "siblings";

    private CmsVfsCacheKey() {
    }

    public static String forACE(String prefix, CmsUUID resourceId) {
        StringBuffer cacheKey = new StringBuffer(64);
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append((Object)resourceId);
        return cacheKey.toString();
    }

    public static String forACEList(String prefix, CmsUUID resourceId, boolean inheritedOnly) {
        StringBuffer cacheKey = new StringBuffer(64);
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append((Object)resourceId);
        cacheKey.append('_').append(inheritedOnly);
        return cacheKey.toString();
    }

    public static String getCacheKeyForAllPropertyDefinitions() {
        return "property_definition_list";
    }

    public static String getCacheKeyForParentFolder(String prefix, CmsUUID structureId) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append(structureId.toString());
        return cacheKey.toString();
    }

    public static String getCacheKeyForProperty(String prefix, CmsResource resource, String key) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append(key);
        cacheKey.append('_');
        cacheKey.append((Object)resource.getStructureId());
        cacheKey.append('_');
        cacheKey.append((Object)resource.getResourceId());
        return cacheKey.toString();
    }

    public static String getCacheKeyForPropertyDefinition(String prefix, String key) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append(key);
        return cacheKey.toString();
    }

    public static String getCacheKeyForPropertyList(String prefix, CmsResource resource) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append((Object)resource.getStructureId());
        cacheKey.append('_');
        cacheKey.append((Object)resource.getResourceId());
        return cacheKey.toString();
    }

    public static String getCacheKeyForRelation(String prefix, CmsUUID structureId) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append(prefix);
        cacheBuffer.append('_');
        cacheBuffer.append(structureId.toString());
        return cacheBuffer.toString();
    }

    public static String getCacheKeyForResource(String prefix, CmsUUID structureId) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append(structureId.toString());
        return cacheKey.toString();
    }

    public static String getCacheKeyForResource(String prefix, String resourcePath) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append(prefix);
        cacheKey.append('_');
        if (resourcePath != null && resourcePath.endsWith("/")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        }
        cacheKey.append(resourcePath);
        return cacheKey.toString();
    }

    public static String getCacheKeyForNonExistentResource(String prefix, String resourcePath) {
        StringBuffer cacheKey = new StringBuffer();
        cacheKey.append(prefix);
        cacheKey.append('_');
        cacheKey.append(resourcePath);
        return cacheKey.toString();
    }

    public static String getCacheKeyForResourceList(String prefix, String resourcePath) {
        return CmsVfsCacheKey.getCacheKeyForResource(prefix, resourcePath);
    }

    public static String getCacheKeyForResourceTree(String prefix, String resourcePath) {
        return CmsVfsCacheKey.getCacheKeyForResource(prefix, resourcePath);
    }

    public static String getCacheKeyForResourceTreeParams(int type, CmsResourceState state, long startTime, long endTime, long releasedAfter, long releasedBefore, long expiredAfter, long expiredBefore, int mode, Integer size, Integer offset) {
        StringBuffer paramsBuffer = new StringBuffer();
        paramsBuffer.append(type).append('_').append((Object)state).append('_').append(startTime).append('_');
        paramsBuffer.append(endTime).append('_').append(releasedAfter).append('_').append(releasedBefore).append('_');
        paramsBuffer.append(expiredAfter).append('_').append(expiredBefore).append('_').append(mode);
        if (size!=null) {
        	paramsBuffer.append('_').append(size).append('_').append(offset);
        }
        return paramsBuffer.toString();
    }
}

