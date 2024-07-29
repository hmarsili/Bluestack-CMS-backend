/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache;

import java.util.Map;
import org.opencms.ocee.cache.CmsCacheStatistics;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;

public interface I_CmsCacheInstance {
    public Map<String, Object> getCacheOffline();

    public Map<String, Object> getCacheOnline();

    public int getCapacityOffline();

    public int getCapacityOnline();

    public int getMemoryUsageOffline();

    public int getMemoryUsageOnline();

    public String getName();

    public String getParameters();

    public CmsCacheStatistics getStatistics();

    public I_CmsCacheInstanceType getType();

    public void initialize(int var1, int var2, String var3);

    public Object lookupOffline(String var1);

    public Object lookupOnline(String var1);

    public void setCacheOffline(String var1, Object var2);

    public void setCacheOnline(String var1, Object var2);

    public void setCapacityOffline(int var1);

    public void setCapacityOnline(int var1);
}

