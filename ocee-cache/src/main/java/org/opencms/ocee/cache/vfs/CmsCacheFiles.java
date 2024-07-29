/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.i18n.CmsMessageContainer
 *  org.opencms.main.CmsIllegalArgumentException
 *  org.opencms.monitor.CmsMemoryMonitor
 *  org.opencms.ocee.base.CmsOceeManager
 *  org.opencms.util.CmsStringUtil
 */
package org.opencms.ocee.cache.vfs;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cache.A_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;
import org.opencms.util.CmsStringUtil;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CmsCacheFiles
extends A_CmsCacheInstance {
    public static final CmsVfsCacheInstanceType TYPE = CmsVfsCacheInstanceType.VFS_FILES;
    private static final String maxlength = "maxlength";
    private static final String showlength = "showlength";
    private int maxLength;
    private int showLength = 512;
    private long tooBigOffline;
    private long tooBigOnline;

    public CmsCacheFiles() {
        super(TYPE);
    }

    @Override
    public void initialize(int onlineCapacity, int offlineCapacity, String parameters) {
        String value;
        if (parameters == null) {
            throw new CmsIllegalArgumentException(Messages.get().container("ERR_FILE_CACHE_MAXLENGTH_PARAM_MISSING_1", (Object)parameters));
        }
        Map params = CmsStringUtil.splitAsMap((String)parameters, (String)",", (String)":");
        if (params.containsKey(maxlength)) {
            value = (String)params.get(maxlength);
            try {
                this.maxLength = Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                throw new CmsIllegalArgumentException(Messages.get().container("ERR_SET_FILE_CACHE_MAXLENGTH_PARAM_1", (Object)value));
            }
            if (this.maxLength <= 0) {
                throw new CmsIllegalArgumentException(Messages.get().container("ERR_SET_FILE_CACHE_MAXLENGTH_PARAM_1", (Object)value));
            }
        } else {
            throw new CmsIllegalArgumentException(Messages.get().container("ERR_FILE_CACHE_MAXLENGTH_PARAM_MISSING_1", (Object)parameters));
        }
        if (params.containsKey("showlength")) {
            value = (String)params.get("showlength");
            int showLength = -1;
            try {
                showLength = Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                throw new CmsIllegalArgumentException(Messages.get().container("ERR_SET_FILE_CACHE_SHOWLENGTH_PARAM_1", (Object)value));
            }
            if (showLength < 0) {
                throw new CmsIllegalArgumentException(Messages.get().container("ERR_SET_FILE_CACHE_SHOWLENGTH_PARAM_1", (Object)value));
            }
            this.showLength = showLength;
        }
        super.initialize(onlineCapacity, offlineCapacity, parameters);
    }

    public int getShowLength() {
        return this.showLength;
    }

    public long getTooBigOffline() {
        return this.tooBigOffline;
    }

    public long getTooBigOnline() {
        return this.tooBigOnline;
    }

    @Override
    public void setCacheOffline(String cacheKey, Object data) {
        byte[] content = (byte[])data;
        if (content.length < this.maxLength) {
            super.setCacheOffline(cacheKey, data);
        } else {
            ++this.tooBigOffline;
        }
    }

    @Override
    public void setCacheOnline(String cacheKey, Object data) {
        byte[] content = (byte[])data;
        if (content.length < this.maxLength) {
            super.setCacheOnline(cacheKey, data);
        } else {
            ++this.tooBigOnline;
        }
    }

    @Override
    protected int getMapMemorySize(Map<String, Object> cache) {
        if (cache == null || cache.isEmpty()) {
            return 0;
        }
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            return 1 + (int)(CmsMemoryMonitor.getValueSize(cache, (int)0) / 1024);
        }
        int totalSize = 0;
        Iterator<Map.Entry<String, Object>> it = cache.entrySet().iterator();
        try {
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                totalSize += CmsMemoryMonitor.getMemorySize((Object)entry.getKey());
                totalSize += CmsMemoryMonitor.getMemorySize((Object)entry.getValue());
            }
        }
        catch (Throwable t) {
            // empty catch block
        }
        return 1 + totalSize / 1024;
    }
}

