/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.opencms.file.CmsProject
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.main.CmsEvent
 *  org.opencms.main.CmsLog
 *  org.opencms.main.I_CmsEventListener
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache.project;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.project.CmsProjectCacheInstanceType;
import org.opencms.ocee.cache.project.CmsProjectCacheKey;
import org.opencms.util.CmsUUID;

public class CmsProjectCacheEventHandler
implements I_CmsEventListener {
    private static final Log o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = CmsLog.getLog(CmsProjectCacheEventHandler.class);

    public void cmsEvent(CmsEvent event) {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null) {
            return;
        }
        switch (event.getType()) {
            case 5: 
            case 16: 
            case 17: {
                manager.getCacheInstance(CmsProjectCacheInstanceType.PROJECT_RESOURCES).getCacheOnline().clear();
                manager.getCacheInstance(CmsProjectCacheInstanceType.PUBLISHED_RESOURCES).getCacheOnline().clear();
                manager.getCacheInstance(CmsProjectCacheInstanceType.STATIC_EXPORT_RESOURCE_NAME).getCacheOnline().clear();
                break;
            }
            case 18: {
                CmsProject project = (CmsProject)event.getData().get("project");
                this.uncacheProjectResources(manager, project.getUuid());
                break;
            }
            case 2: 
            case 22: 
            case 25: {
                manager.getCacheInstance(CmsProjectCacheInstanceType.PROJECT_RESOURCES).getCacheOnline().clear();
                break;
            }
        }
    }

    protected void uncacheProjectResources(CmsCacheManager manager, CmsUUID projectId) {
        String cacheKey = CmsProjectCacheKey.getCacheKeyForProjectResources(projectId);
        manager.cacheRemove(CmsProjectCacheInstanceType.PROJECT_RESOURCES, CmsProject.ONLINE_PROJECT_ID, cacheKey);
        if (o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.isDebugEnabled()) {
            o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_REMOVED_RESOURCES_FOR_PROJECT_1", new Object[]{projectId}));
        }
    }
}

