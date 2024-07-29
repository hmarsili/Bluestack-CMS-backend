/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.map.LRUMap
 *  org.apache.commons.logging.Log
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.main.CmsLog
 *  org.opencms.main.OpenCms
 *  org.opencms.monitor.CmsMemoryMonitor
 */
package org.opencms.ocee.cache;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.ocee.cache.CmsCacheStatistics;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class A_CmsCacheInstance
implements I_CmsCacheInstance {
    protected static final String valueIndicator = ":";
    protected static final String valueSeparator = ",";
    private static final Log LOG = CmsLog.getLog(A_CmsCacheInstance.class);
    protected Map<String, Object> cacheOffline;
    protected int offlineCapacity;
    protected Map<String, Object> cacheOnline;
    protected int onlineCapacity;
    protected String parameters;
    protected CmsCacheStatistics cacheStatistics;
    protected final I_CmsCacheInstanceType cacheInstanceType;

    protected A_CmsCacheInstance(I_CmsCacheInstanceType type) {
        this.cacheInstanceType = type;
    }

    @Override
    public Map<String, Object> getCacheOffline() {
        return this.cacheOffline;
    }

    @Override
    public Map<String, Object> getCacheOnline() {
        return this.cacheOnline;
    }

    @Override
    public int getCapacityOffline() {
        return this.offlineCapacity;
    }

    @Override
    public int getCapacityOnline() {
        return this.onlineCapacity;
    }

    @Override
    public int getMemoryUsageOffline() {
        return this.getMapMemorySize(this.cacheOffline);
    }

    @Override
    public int getMemoryUsageOnline() {
        return this.getMapMemorySize(this.cacheOnline);
    }

    @Override
    public String getName() {
        return this.cacheInstanceType.getType();
    }

    @Override
    public String getParameters() {
        return this.parameters;
    }

    @Override
    public CmsCacheStatistics getStatistics() {
        return this.cacheStatistics;
    }

    @Override
    public I_CmsCacheInstanceType getType() {
        return this.cacheInstanceType;
    }

    @Override
    public void initialize(int onlineCapacity, int offlineCapacity, String parameters) {
        this.onlineCapacity = onlineCapacity;
        this.offlineCapacity = offlineCapacity;
        this.parameters = parameters;
        this.cacheStatistics = new CmsCacheStatistics(this.cacheInstanceType);
        if (this.getCapacityOffline() > 0) {
            LRUMap offCache = new LRUMap(this.getCapacityOffline());
            this.cacheOffline = Collections.synchronizedMap(offCache);
            if (OpenCms.getMemoryMonitor() != null && OpenCms.getMemoryMonitor().enabled()) {
                OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + this.getName() + ".offline", (Object)offCache);
            }
        } else if (this.getCapacityOffline() == 0) {
            this.cacheOffline = Collections.emptyMap();
        }
        if (this.getCapacityOnline() > 0) {
            LRUMap onCache = new LRUMap(this.getCapacityOnline());
            this.cacheOnline = Collections.synchronizedMap(onCache);
            if (OpenCms.getMemoryMonitor() != null && OpenCms.getMemoryMonitor().enabled()) {
                OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + this.getName() + ".online", (Object)onCache);
            }
        } else {
            this.cacheOnline = Collections.emptyMap();
        }
    }

    @Override
    public Object lookupOffline(String cacheKey) {
        Object retValue = this.cacheOffline.get(cacheKey);
        if (retValue == null) {
            this.getStatistics().incrementOfflineMisses();
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

    @Override
    public Object lookupOnline(String cacheKey) {
        Object retValue = this.cacheOnline.get(cacheKey);
        if (retValue == null) {
            this.getStatistics().incrementOnlineMisses();
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

    @Override
    public void setCacheOffline(String cacheKey, Object data) {
        if (this.offlineCapacity == 0) {
            return;
        }
        this.cacheOffline.put(cacheKey, data);
        if (LOG.isDebugEnabled()) {
        	LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_CACHE_SET_OFFLINE_3", new Object[]{this.getName(), cacheKey, data}));
        }
    }

    @Override
    public void setCacheOnline(String cacheKey, Object data) {
        if (this.onlineCapacity == 0) {
            return;
        }
        this.cacheOnline.put(cacheKey, data);
        if (LOG.isDebugEnabled()) {
        	LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_CACHE_SET_ONLINE_3", new Object[]{this.getName(), cacheKey, data}));
        }
    }

    @Override
    public void setCapacityOffline(int capacity) {
        this.offlineCapacity = capacity;
        if (!this.getType().isProjectAware() || this.offlineCapacity == 0) {
            this.cacheOffline = Collections.emptyMap();
        } else {
            LRUMap tmpCache = new LRUMap(this.getCapacityOffline());
            tmpCache.putAll(this.getCacheOffline());
            this.cacheOffline = Collections.synchronizedMap(tmpCache);
        }
        if (OpenCms.getMemoryMonitor() != null && OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + this.getName() + ".offline", this.cacheOffline);
        }
    }

    @Override
    public void setCapacityOnline(int capacity) {
        this.onlineCapacity = capacity;
        if (this.onlineCapacity == 0) {
            this.cacheOnline = Collections.emptyMap();
        } else {
            LRUMap tmpCache = new LRUMap(this.getCapacityOnline());
            tmpCache.putAll(this.getCacheOnline());
            this.cacheOnline = Collections.synchronizedMap(tmpCache);
        }
        if (OpenCms.getMemoryMonitor() != null && OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + this.getName() + ".online", this.cacheOnline);
        }
    }

    protected int getMapMemorySize(Map<String, Object> cache) {
        if (cache == null || cache.isEmpty()) {
            return 0;
        }
        String key = cache.keySet().iterator().next();
        int keySize = CmsMemoryMonitor.getMemorySize((Object)key);
        Object value = cache.values().iterator().next();
        int valueSize = value instanceof Map ? this.getMapMemorySize((Map)value) : CmsMemoryMonitor.getMemorySize((Object)value);
        return 1 + cache.size() * (keySize + valueSize) / 1024;
    }
}

