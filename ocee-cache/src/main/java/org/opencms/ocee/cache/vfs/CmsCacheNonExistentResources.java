/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.main.CmsLog
 */
package org.opencms.ocee.cache.vfs;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.ocee.cache.A_CmsCacheInstance;
import org.opencms.ocee.cache.CmsCacheStatistics;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;

public class CmsCacheNonExistentResources
extends A_CmsCacheInstance {
    public static final CmsVfsCacheInstanceType TYPE = CmsVfsCacheInstanceType.VFS_NONEXISTENT_RESOURCES;
    private static final Log LOG = CmsLog.getLog(CmsCacheNonExistentResources.class);

    public CmsCacheNonExistentResources() {
        super(TYPE);
    }

    public Object lookupOffline(String cacheKey) {
        Object retValue = this.cacheOffline.get(cacheKey);
        if (retValue == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_CACHE_MISSED_OFFLINE_2", new Object[]{this.getName(), cacheKey}));
            }
        } else {
            this.getStatistics().incrementOfflineMatches();
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_CACHE_MATCHED_OFFLINE_3", new Object[]{this.getName(), cacheKey, retValue}));
            }
        }
        return retValue;
    }

    public Object lookupOnline(String cacheKey) {
        Object retValue = this.cacheOnline.get(cacheKey);
        if (retValue == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_CACHE_MISSED_ONLINE_2", new Object[]{this.getName(), cacheKey}));
            }
        } else {
            this.getStatistics().incrementOnlineMatches();
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_CACHE_MATCHED_ONLINE_3", new Object[]{this.getName(), cacheKey, retValue}));
            }
        }
        return retValue;
    }

    public void setCacheOffline(String cacheKey, Object data) {
    }

    public void setCacheOnline(String cacheKey, Object data) {
        if (this.onlineCapacity  == 0) {
            return;
        }
        this.cacheOnline.put(cacheKey, data);
        this.getStatistics().incrementOnlineMisses();
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_CACHE_SET_ONLINE_3", new Object[]{this.getName(), cacheKey, data}));
        }
    }
}

