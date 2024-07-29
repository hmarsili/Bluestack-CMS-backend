/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.jsp.CmsJspActionElement
 *  org.opencms.ocee.base.CmsStatisticalCounter
 *  org.opencms.workplace.CmsDialog
 *  org.opencms.workplace.CmsWorkplace
 *  org.opencms.workplace.tools.CmsToolManager
 */
package org.opencms.ocee.cache;

import java.util.HashMap;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.base.CmsStatisticalCounter;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.CmsCacheStatistics;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.vfs.CmsCacheFiles;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsToolManager;

public class CmsCacheStatisticalCounter
extends CmsStatisticalCounter {
    private boolean onlineProject;
    private I_CmsCacheInstanceType cacheInstanceType;

    public CmsCacheStatisticalCounter(String name, String group, float position, I_CmsCacheInstanceType type, boolean onlineProject) {
        super(name, group, position);
        this.cacheInstanceType = type;
        this.onlineProject = onlineProject;
    }

    public String toString(CmsDialog wp) {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null) {
            return super.toString();
        }
        I_CmsCacheInstance cache = manager.getCacheInstance(this.cacheInstanceType);
        CmsCacheStatistics stats = cache.getStatistics();
        long total = this.onlineProject ? stats.getOnlineMatches() + stats.getOnlineMisses() : stats.getOfflineMatches() + stats.getOfflineMisses();
        String output = CmsStatisticalCounter.percentageOutput((long)this.getValue(), (long)total).key(wp.getLocale());
        if (this.getName().endsWith("matches")) {
            int cacheSize;
            int cacheCap;
            int memoryUsage;
            if (this.onlineProject) {
                cacheSize = cache.getCacheOnline().size();
                cacheCap = cache.getCapacityOnline();
                memoryUsage = cache.getMemoryUsageOnline();
            } else if (cache.getCacheOffline() != null) {
                cacheSize = cache.getCacheOffline().size();
                cacheCap = cache.getCapacityOffline();
                memoryUsage = cache.getMemoryUsageOffline();
            } else {
                return "";
            }
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("online", Boolean.valueOf(this.onlineProject).toString());
            params.put("cache", this.cacheInstanceType.getType());
            String link = CmsToolManager.linkForToolPath((CmsJspActionElement)wp.getJsp(), (String)(wp.getToolManager().getCurrentToolPath((CmsWorkplace)wp) + "/contents"), params);
            output = Messages.get().getBundle(wp.getLocale()).key("GUI_CACHE_STAT_COUNTER_3", new Object[]{output, "<a href='" + link + "'>" + CmsStatisticalCounter.percentageOutput((long)cacheSize, (long)cacheCap).key(wp.getLocale()) + "</a>", new Integer(memoryUsage)});
        } else if (cache instanceof CmsCacheFiles) {
            CmsCacheFiles cacheFiles = (CmsCacheFiles)cache;
            long tooBigs = this.onlineProject ? cacheFiles.getTooBigOnline() : cacheFiles.getTooBigOffline();
            output = Messages.get().getBundle(wp.getLocale()).key("GUI_CACHE_STAT_TOO_BIG_FILES_2", new Object[]{output, CmsStatisticalCounter.percentageOutput((long)tooBigs, (long)this.getValue()).key(wp.getLocale())});
        }
        return output;
    }
}

