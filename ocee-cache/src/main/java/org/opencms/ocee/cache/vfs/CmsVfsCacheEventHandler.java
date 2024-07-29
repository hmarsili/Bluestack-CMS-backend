/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.opencms.db.CmsPublishedResource
 *  org.opencms.file.CmsProperty
 *  org.opencms.file.CmsPropertyDefinition
 *  org.opencms.file.CmsResource
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.main.CmsEvent
 *  org.opencms.main.CmsLog
 *  org.opencms.main.I_CmsEventListener
 *  org.opencms.ocee.base.CmsBaseModuleAction
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache.vfs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.ocee.base.CmsBaseModuleAction;
import org.opencms.ocee.cache.CmsCacheConfiguration;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsVfsCacheKey;
import org.opencms.ocee.cache.vfs.Messages;
import org.opencms.util.CmsUUID;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class CmsVfsCacheEventHandler
implements I_CmsEventListener {
    private static final Log o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = CmsLog.getLog(CmsVfsCacheEventHandler.class);

    public void cmsEvent(CmsEvent event) {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null) {
            return;
        }
        CmsProperty property = null;
        CmsResource resource = null;
        List resources = null;
        CmsPropertyDefinition propertyDefinition = null;
        CmsPublishedResource pubRes = null;
        switch (event.getType()) {
            case 14: {
                resource = (CmsResource)event.getData().get("resource");
                property = (CmsProperty)event.getData().get("property");
                this.uncacheProperty(manager, resource, property);
                this.uncacheResource(manager, new CmsPublishedResource(resource), false);
                break;
            }
            case 15: 
            case 23: {
                resource = (CmsResource)event.getData().get("resource");
                pubRes = new CmsPublishedResource(resource);
                this.uncacheProperties(manager, pubRes, false);
                this.uncacheResource(manager, pubRes, false);
                this.uncacheRelations(manager, false);
                break;
            }
            case 27: {
                resources = (List)event.getData().get("resources");
                this.uncacheResourcesAndPropertiesOffline(manager, resources);
                this.uncacheRelations(manager, false);
                break;
            }
            case 11: {
                resource = (CmsResource)event.getData().get("resource");
                pubRes = new CmsPublishedResource(resource);
                this.uncacheResource(manager, pubRes, false);
                this.uncacheResourceACE(manager, resource.getResourceId(), false);
                this.uncacheRelations(manager, false);
                break;
            }
            case 12: 
            case 22: 
            case 24: 
            case 25: {
                resources = (List)event.getData().get("resources");
                this.uncacheResources(manager, resources, false);
                this.uncacheParentFoldersOffline(manager, resources);
                this.uncacheRelations(manager, false);
                break;
            }
            case 17: {
                for (CmsVfsCacheInstanceType cacheType : CmsVfsCacheInstanceType.VALUES) {
                    manager.getCacheInstance(cacheType).getCacheOnline().clear();
                }
                break;
            }
            case 2: {
                List publishedResources = null;
                try {
                    CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get("publishHistoryId"));
                    if (CmsBaseModuleAction.getCms() != null) {
                        publishedResources = CmsBaseModuleAction.getCms().readPublishedResources(publishHistoryId);
                    }
                }
                catch (Throwable e) {
                    if (o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.isErrorEnabled()) {
                        o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)e.getLocalizedMessage(), e);
                    }
                    return;
                }
                if (publishedResources == null || publishedResources.size() > manager.getConfiguration().getOnlineFlushResourceLimit()) {
                    this.cmsEvent(new CmsEvent(17, null));
                    return;
                }
                this.uncacheNonExistentResourcesOnline(manager, publishedResources);
                this.uncacheParentFoldersOnline(manager, publishedResources);
                this.uncacheResourcesAndPropertiesOnline(manager, publishedResources);
                this.uncacheResourcesACEOnline(manager, publishedResources);
                this.uncacheResourceTreesOnline(manager, publishedResources);
                this.uncacheRelations(manager, true);
                break;
            }
            case 5: {
                for (CmsVfsCacheInstanceType cacheType : CmsVfsCacheInstanceType.VALUES) {
                    manager.getCacheInstance(cacheType).getCacheOffline().clear();
                    manager.getCacheInstance(cacheType).getCacheOnline().clear();
                }
                break;
            }
            case 16: {
                for (CmsVfsCacheInstanceType cacheType : CmsVfsCacheInstanceType.VALUES) {
                    manager.getCacheInstance(cacheType).getCacheOffline().clear();
                }
                break;
            }
            case 26: {
                propertyDefinition = (CmsPropertyDefinition)event.getData().get("propertyDefinition");
                this.uncachePropertyDefinition(manager, propertyDefinition);
                break;
            }
            case 28: {
                propertyDefinition = (CmsPropertyDefinition)event.getData().get("propertyDefinition");
                this.uncachePropertyDefinitionList(manager);
                break;
            }
        }
    }

    public void uncacheRelations(CmsCacheManager manager, boolean online) {
        I_CmsCacheInstance cache = manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_RESOURCERELATIONS);
        if (online) {
            cache.getCacheOnline().clear();
        } else {
            cache.getCacheOffline().clear();
        }
    }

    protected Map<String, Object> getCacheContent(CmsCacheManager manager, CmsVfsCacheInstanceType type, boolean online) {
        if (online) {
            return manager.getCacheInstance(type).getCacheOnline();
        }
        return manager.getCacheInstance(type).getCacheOffline();
    }

    protected void uncacheResourcesACEOnline(CmsCacheManager manager, List<CmsPublishedResource> resources) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        if (resources.isEmpty()) {
            return;
        }
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsPublishedResource resource = resources.get(i);
            this.uncacheResourceACE(manager, resource.getResourceId(), true);
        }
    }

    protected void uncacheResourceACE(CmsCacheManager manager, CmsUUID resourceId, boolean online) {
        String cacheKeyEntry = CmsVfsCacheKey.forACE("ace", resourceId);
        String cacheKeyListInheritedOnly = CmsVfsCacheKey.forACEList("aceList", resourceId, true);
        String cacheKeyListAll = CmsVfsCacheKey.forACEList("aceList", resourceId, false);
        Map<String, Object> cacheEntries = null;
        Map<String, Object> cacheLists = null;
        if (online) {
            cacheEntries = manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_ACE).getCacheOnline();
            cacheLists = manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_ACE_LIST).getCacheOnline();
        } else {
            cacheEntries = manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_ACE).getCacheOffline();
            cacheLists = manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_ACE_LIST).getCacheOffline();
        }
        cacheEntries.remove(cacheKeyEntry);
        cacheLists.remove(cacheKeyListInheritedOnly);
        cacheLists.remove(cacheKeyListAll);
    }

    protected void uncacheNonExistentResource(CmsCacheManager manager, CmsPublishedResource pubRes, boolean online) {
        if (pubRes == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForResource("resources", pubRes.getRootPath());
        Map<String, Object> nonExistentResource = this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_NONEXISTENT_RESOURCES, online);
        nonExistentResource.remove(cacheKey);
        nonExistentResource.remove(cacheKey + "/");
    }

    protected void uncacheNonExistentResourcesOnline(CmsCacheManager manager, List<CmsPublishedResource> resources) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsPublishedResource resource = resources.get(i);
            this.uncacheNonExistentResource(manager, resource, true);
        }
    }

    protected void uncacheParentFoldersOffline(CmsCacheManager manager, List<CmsResource> resources) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        if (resources.isEmpty()) {
            return;
        }
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsResource resource = resources.get(i);
            if (resource == null) {
                o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
                continue;
            }
            this.uncacheParentFolder(manager, resource.getStructureId(), false);
        }
    }

    protected void uncacheParentFoldersOnline(CmsCacheManager manager, List<CmsPublishedResource> resources) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        if (resources.isEmpty()) {
            return;
        }
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsPublishedResource resource = resources.get(i);
            if (resource == null) {
                o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
                continue;
            }
            this.uncacheParentFolder(manager, resource.getStructureId(), true);
        }
    }

    protected void uncacheParentFolder(CmsCacheManager manager, CmsUUID structureId, boolean online) {
        String cacheKeyParent = CmsVfsCacheKey.getCacheKeyForParentFolder("parent_folder", structureId);
        Map<String, Object> cache = this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_PARENTFOLDER, online);
        cache.remove(cacheKeyParent);
    }

    protected void uncacheProperties(CmsCacheManager manager, CmsPublishedResource resource, boolean online) {
        if (resource == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        Map<String, Object> propertyCache = this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_PROPERTIES, online);
        Object[] keys = propertyCache.keySet().toArray();
        String structureId = resource.getStructureId().toString();
        String resourceId = resource.getResourceId().toString();
        int n = keys.length;
        for (int i = 0; i < n; ++i) {
            String cacheKey = (String)keys[i];
            if (cacheKey.indexOf(structureId) == -1 && cacheKey.indexOf(resourceId) == -1) continue;
            propertyCache.remove(cacheKey);
        }
    }

    protected void uncacheProperty(CmsCacheManager manager, CmsResource resource, CmsProperty property) {
        if (resource == null || property == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        String propertyKey = property.getName();
        Map<String, Object> offlinePropertyCache = manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_PROPERTIES).getCacheOffline();
        Object[] keys = offlinePropertyCache.keySet().toArray();
        String structureId = resource.getStructureId().toString();
        String resourceId = resource.getResourceId().toString();
        int n = keys.length;
        for (int i = 0; i < n; ++i) {
            String cacheKey = (String)keys[i];
            if (cacheKey.startsWith("property")) {
                if (cacheKey.indexOf(propertyKey) == -1 || cacheKey.indexOf(structureId) == -1 && cacheKey.indexOf(resourceId) == -1) continue;
                offlinePropertyCache.remove(cacheKey);
                continue;
            }
            if (cacheKey.startsWith("properties")) {
                if (cacheKey.indexOf(structureId) == -1 && cacheKey.indexOf(resourceId) == -1) continue;
                offlinePropertyCache.remove(cacheKey);
                continue;
            }
            if (!o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.isErrorEnabled()) continue;
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("LOG_UNKNOWN_CACHEKEY_1", new Object[]{cacheKey}));
        }
        if (o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.isDebugEnabled()) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.debug((Object)Messages.get().getBundle().key("LOG_REMOVE_PROP_2", new Object[]{propertyKey, resource.getName()}));
        }
    }

    protected void uncachePropertyDefinition(CmsCacheManager manager, CmsPropertyDefinition propertyDefinition) {
        if (propertyDefinition == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        Map<String, Object> offlinePropertyDefinitionCache = manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_PROPERTYDEFINITIONS).getCacheOffline();
        Object[] keys = offlinePropertyDefinitionCache.keySet().toArray();
        String propertyDefinitionName = propertyDefinition.getName();
        int n = keys.length;
        for (int i = 0; i < n; ++i) {
            String cacheKey = (String)keys[i];
            if (cacheKey.indexOf(propertyDefinitionName) == -1) continue;
            offlinePropertyDefinitionCache.remove(cacheKey);
        }
        this.uncachePropertyDefinitionList(manager);
    }

    protected void uncachePropertyDefinitionList(CmsCacheManager manager) {
        manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_PROPERTYDEFINITIONS).getCacheOffline().remove(CmsVfsCacheKey.getCacheKeyForAllPropertyDefinitions());
    }

    protected void uncacheResource(CmsCacheManager manager, CmsPublishedResource resource, boolean online) {
        if (resource == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        if (resource.isFile()) {
            int i;
            int n;
            CmsPublishedResource sibling;
            List<CmsResource> siblings = this.uncacheResource(manager, resource, online, false);
            if (siblings != null) {
                n = siblings.size();
                for (i = 0; i < n; ++i) {
                    sibling = new CmsPublishedResource(siblings.get(i));
                    this.uncacheResource(manager, sibling, online);
                }
            }
            if ((siblings = this.uncacheResource(manager, resource, online, true)) != null) {
                n = siblings.size();
                for (i = 0; i < n; ++i) {
                    sibling = new CmsPublishedResource(siblings.get(i));
                    this.uncacheResource(manager, sibling, online);
                }
            }
        } else {
            this.uncacheResource(manager, resource, online, false);
            this.uncacheResource(manager, resource, online, true);
        }
        String parentFolder = CmsResource.getParentFolder((String)resource.getRootPath());
        if (parentFolder != null) {
            this.uncacheSubResourceLists(manager, parentFolder, online);
        }
    }

    protected List<CmsResource> uncacheResource(CmsCacheManager manager, CmsPublishedResource resource, boolean online, boolean includeDeleted) {
        if (resource == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return Collections.emptyList();
        }
        Map<String, Object> resourceCache = this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_RESOURCES, online);
        String cacheKey = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getStructureId());
        resourceCache.remove(cacheKey);
        cacheKey = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getRootPath());
        resourceCache.remove(cacheKey);
        if (resource.isFile()) {
            cacheKey = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getResourceId());
            this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_FILES, online).remove(cacheKey);
        }
        cacheKey = CmsVfsCacheKey.getCacheKeyForResourceList("siblings", resource.getResourceId().toString() + Boolean.toString(includeDeleted));
        List siblings = (List)this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_RESOURCELISTS, online).remove(cacheKey);
        if (o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.isDebugEnabled()) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.debug((Object)Messages.get().getBundle().key("LOG_REMOVE_RESOURCES_2", new Object[]{resource.getRootPath(), online}));
        }
        return siblings;
    }

    protected void uncacheResources(CmsCacheManager manager, List<CmsResource> resources, boolean online) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsPublishedResource resource = new CmsPublishedResource(resources.get(i));
            this.uncacheResource(manager, resource, online);
        }
    }

    protected void uncacheResourcesAndPropertiesOffline(CmsCacheManager manager, List<CmsResource> resources) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsPublishedResource pubRes = new CmsPublishedResource(resources.get(i));
            this.uncacheResource(manager, pubRes, false);
            this.uncacheProperties(manager, pubRes, false);
        }
    }

    protected void uncacheResourcesAndPropertiesOnline(CmsCacheManager manager, List<CmsPublishedResource> resources) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsPublishedResource pubRes = resources.get(i);
            this.uncacheResource(manager, pubRes, true);
            this.uncacheProperties(manager, pubRes, true);
        }
    }

    protected void uncacheResourceTreesOnline(CmsCacheManager manager, List<CmsPublishedResource> resources) {
        if (resources == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        if (manager.getCacheInstance(CmsVfsCacheInstanceType.VFS_RESOURCETREE).getCapacityOnline() <= 0) {
            return;
        }
        HashSet<String> pathsToRemove = new HashSet<String>();
        int n = resources.size();
        for (int i = 0; i < n; ++i) {
            CmsPublishedResource pubRes = resources.get(i);
            String parentPath = pubRes.isFolder() ? pubRes.getRootPath() : CmsResource.getFolderPath((String)pubRes.getRootPath());
            while (!parentPath.equals("/")) {
                pathsToRemove.add(parentPath);
                parentPath = CmsResource.getParentFolder((String)parentPath);
            }
            pathsToRemove.add(parentPath);
        }
        Map<String, Object> resourceCache = this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_RESOURCETREE, true);
        for (String path : pathsToRemove) {
            String cacheKey = CmsVfsCacheKey.getCacheKeyForResourceTree("resource_tree", path);
            resourceCache.remove(cacheKey);
        }
    }

    protected void uncacheSubResourceLists(CmsCacheManager manager, String resourcePath, boolean online) {
        if (resourcePath == null) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.error((Object)Messages.get().getBundle().key("ERR_UNCACHE_NULL_1", (Object)this.getClass().getName()), (Throwable)new Exception());
            return;
        }
        Map<String, Object> resourceListCache = this.getCacheContent(manager, CmsVfsCacheInstanceType.VFS_RESOURCELISTS, online);
        String cacheKey = CmsVfsCacheKey.getCacheKeyForResourceList("_all_", resourcePath);
        resourceListCache.remove(cacheKey);
        cacheKey = CmsVfsCacheKey.getCacheKeyForResourceList("_folders_", resourcePath);
        resourceListCache.remove(cacheKey);
        cacheKey = CmsVfsCacheKey.getCacheKeyForResourceList("_files_", resourcePath);
        resourceListCache.remove(cacheKey);
        if (o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.isDebugEnabled()) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.debug((Object)Messages.get().getBundle().key("LOG_REMOVE_RESOURCES_2", (Object)new Object[]{resourcePath}, (Object)online));
        }
    }
}

