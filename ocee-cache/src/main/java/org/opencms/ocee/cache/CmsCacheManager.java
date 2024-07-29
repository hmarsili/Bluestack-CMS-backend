/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.db.I_CmsProjectDriver
 *  org.opencms.db.I_CmsUserDriver
 *  org.opencms.db.I_CmsVfsDriver
 *  org.opencms.file.CmsProject
 *  org.opencms.i18n.CmsMessageContainer
 *  org.opencms.main.CmsIllegalStateException
 *  org.opencms.ocee.base.CmsOceeManager
 *  org.opencms.ocee.base.CmsReloadingClassLoader
 *  org.opencms.util.CmsUUID
 *  org.opencms.workplace.tools.CmsIdentifiableObjectContainer
 */
package org.opencms.ocee.cache;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.base.CmsReloadingClassLoader;
import org.opencms.ocee.cache.A_CmsCacheInstanceType;
import org.opencms.ocee.cache.CmsCacheConfiguration;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.project.CmsProjectCacheInstanceType;
import org.opencms.ocee.cache.user.CmsUserCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.tools.CmsIdentifiableObjectContainer;

public final class CmsCacheManager {
    public static final int OFFLINE_DEFAULT_CAPACITY = 512;
    public static final int ONLINE_DEFAULT_CAPACITY = 512;
    public static final int ONLINE_DEFAULT_RESOURCE_LIMIT = 50;
    private CmsIdentifiableObjectContainer \u00d3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = new CmsIdentifiableObjectContainer(true, false);
    private boolean \u00d4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;
    private I_CmsProjectDriver \u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
    private I_CmsUserDriver o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
    private I_CmsVfsDriver \u00d5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
    private CmsCacheConfiguration \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return;

    public static CmsCacheManager getInstance() {
        try {
            return (CmsCacheManager)CmsOceeManager.getInstance().getClassLoader().loadObject(CmsCacheManager.class.getName());
        }
        catch (Exception e) {
            return null;
        }
    }

    public Object cacheLookup(I_CmsCacheInstanceType type, CmsUUID projectId, String cacheKey) {
        Object retValue = null;
        I_CmsCacheInstance cacheInstance = this.getCacheInstance(type);
        retValue = CmsProject.isOnlineProject((CmsUUID)projectId) ? cacheInstance.lookupOnline(cacheKey) : cacheInstance.lookupOffline(cacheKey);
        return retValue;
    }

    public void cacheRemove(I_CmsCacheInstanceType type, CmsUUID projectId, String cacheKey) {
        if (CmsProject.isOnlineProject((CmsUUID)projectId)) {
            this.getCacheInstance(type).getCacheOnline().remove(cacheKey);
        } else {
            this.getCacheInstance(type).getCacheOffline().remove(cacheKey);
        }
    }

    public void cacheSet(I_CmsCacheInstanceType type, CmsUUID projectId, String cacheKey, Object data) {
        I_CmsCacheInstance cacheInstance = this.getCacheInstance(type);
        if (CmsProject.isOnlineProject((CmsUUID)projectId)) {
            cacheInstance.setCacheOnline(cacheKey, data);
        } else {
            cacheInstance.setCacheOffline(cacheKey, data);
        }
    }

    public I_CmsCacheInstance getCacheInstance(I_CmsCacheInstanceType type) {
        return (I_CmsCacheInstance)this.\u00d3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getObject(type.getType());
    }

    public I_CmsProjectDriver getProjectDriver() {
        return this.\u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
    }

    public I_CmsUserDriver getUserDriver() {
        return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
    }

    public I_CmsVfsDriver getVfsDriver() {
        return this.\u00d5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
    }

    public void initialize(CmsCacheConfiguration configuration) {
        if (this.\u00d4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String) {
            return;
        }
        this.\u00d4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = true;
        this.\u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = configuration;
        ArrayList<I_CmsCacheInstanceType> missing = new ArrayList<I_CmsCacheInstanceType>();
        ArrayList<A_CmsCacheInstanceType> allTypes = new ArrayList<A_CmsCacheInstanceType>();
        allTypes.addAll(CmsVfsCacheInstanceType.VALUES);
        allTypes.addAll(CmsUserCacheInstanceType.VALUES);
        allTypes.addAll(CmsProjectCacheInstanceType.VALUES);
        for (I_CmsCacheInstanceType type : allTypes) {
            if (this.getCacheInstance(type) != null) continue;
            try {
                this.\u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.addCacheInstance(type.getDefaultImplementationClass(), "0", "0", null);
            }
            catch (Exception e) {
                missing.add(type);
            }
        }
        if (!missing.isEmpty()) {
            throw new CmsIllegalStateException(Messages.get().container("INIT_ERR_MISSING_CACHE_CONF_1", (Object)missing.toString()));
        }
    }

    public CmsCacheConfiguration getConfiguration() {
        return this.\u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return;
    }

    public boolean isInitialized() {
        return this.\u00d4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;
    }

    public void setCacheInstance(String className, int onlineCapacity, int offlineCapacity, String parameters) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class clazz = cl.loadClass(className);
        Constructor ctor = clazz.getConstructor(new Class[0]);
        if (parameters == null) {
            parameters = "";
        }
        I_CmsCacheInstance cacheInstance = (I_CmsCacheInstance)ctor.newInstance(new Object[0]);
        this.\u00d3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.addIdentifiableObject(cacheInstance.getName(), (Object)cacheInstance);
        cacheInstance.initialize(onlineCapacity, offlineCapacity, parameters);
    }

    public void setProjectDriver(I_CmsProjectDriver projectDriver) {
        this.\u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = projectDriver;
    }

    public void setUserDriver(I_CmsUserDriver userDriver) {
        this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = userDriver;
    }

    public void setVfsDriver(I_CmsVfsDriver vfsDriver) {
        this.\u00d5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = vfsDriver;
    }

    public String toString() {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("[" + this.getClass().getName() + ":\n");
        for (Object obj : this.\u00d3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.elementList()) {
            strBuf.append(obj.toString()).append("\n");
        }
        strBuf.append("]");
        return strBuf.toString();
    }
}

