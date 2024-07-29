/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.jsp.CmsJspActionElement
 *  org.opencms.ocee.base.A_CmsStatisticsDialog
 *  org.opencms.ocee.base.CmsStatisticalCounterCollection
 *  org.opencms.ocee.base.I_CmsStatisticalCounter
 */
package org.opencms.ocee.cache.admin;

import java.util.List;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.base.A_CmsStatisticsDialog;
import org.opencms.ocee.base.CmsStatisticalCounterCollection;
import org.opencms.ocee.base.I_CmsStatisticalCounter;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.CmsCacheStatistics;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.admin.Messages;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class A_CmsCacheStatisticsDialog
extends A_CmsStatisticsDialog {
    protected A_CmsCacheStatisticsDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    protected void initMessages() {
        this.addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected CmsStatisticalCounterCollection getCounters() {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null) {
            return new CmsStatisticalCounterCollection();
        }
        CmsStatisticalCounterCollection statistics = new CmsStatisticalCounterCollection();
        for (I_CmsCacheInstanceType cacheType : this.getTypes()) {
            I_CmsCacheInstance cache = manager.getCacheInstance(cacheType);
            statistics.addAll(cache.getStatistics().getCounters());
        }
        return statistics;
    }

    protected abstract List<? extends I_CmsCacheInstanceType> getTypes();
}

