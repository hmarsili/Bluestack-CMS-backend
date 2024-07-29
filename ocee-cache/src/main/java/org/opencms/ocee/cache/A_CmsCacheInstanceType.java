/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.i18n.CmsMessageContainer
 *  org.opencms.main.CmsIllegalArgumentException
 */
package org.opencms.ocee.cache;

import java.util.ArrayList;
import java.util.List;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class A_CmsCacheInstanceType
implements I_CmsCacheInstanceType {
    protected static final List<I_CmsCacheInstanceType> cacheInstanceType = new ArrayList<I_CmsCacheInstanceType>();
    private final boolean projectAware;
    private final String type;
    private final String defaultClass;

    protected A_CmsCacheInstanceType(String type, String defaultClass, boolean projectAware) {
        this.type = type;
        this.defaultClass = defaultClass;
        this.projectAware = projectAware;
        cacheInstanceType.add(this);
    }

    public static I_CmsCacheInstanceType valueOf(String value) {
        return A_CmsCacheInstanceType.valueOf(cacheInstanceType, value);
    }

    protected static I_CmsCacheInstanceType valueOf(List<? extends I_CmsCacheInstanceType> values, String value) {
        for (I_CmsCacheInstanceType type : values) {
            if (!value.equalsIgnoreCase(type.getType())) continue;
            return type;
        }
        throw new CmsIllegalArgumentException(Messages.get().container("ERR_CACHE_VFSCACHE_TYPE_PARSE_1", (Object)value));
    }

    @Override
    public String getDefaultImplementationClass() {
        return this.defaultClass;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public boolean isProjectAware() {
        return this.projectAware;
    }

    public String toString() {
        return this.type;
    }
}

