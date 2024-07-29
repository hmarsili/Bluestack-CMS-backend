/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache;

public interface I_CmsCacheInstanceType {
    public String getDefaultImplementationClass();

    public int getOrder();

    public String getType();

    public boolean isProjectAware();
}

