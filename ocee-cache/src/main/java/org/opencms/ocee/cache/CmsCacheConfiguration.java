/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.commons.digester3.Digester
 *  org.apache.commons.logging.Log
 *  org.dom4j.Element
 *  org.opencms.configuration.A_CmsXmlConfiguration
 *  org.opencms.db.I_CmsProjectDriver
 *  org.opencms.db.I_CmsUserDriver
 *  org.opencms.db.I_CmsVfsDriver
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.main.CmsIllegalArgumentException
 *  org.opencms.main.CmsLog
 *  org.opencms.ocee.base.CmsOceeManager
 *  org.opencms.util.CmsStringUtil
 */
package org.opencms.ocee.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.digester3.Digester;
import org.apache.commons.logging.Log;
import org.dom4j.Element;
import org.opencms.configuration.A_CmsXmlConfiguration;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.project.CmsProjectCacheInstanceType;
import org.opencms.ocee.cache.user.CmsUserCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;
import org.opencms.util.CmsStringUtil;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CmsCacheConfiguration
extends A_CmsXmlConfiguration {
    protected static final String oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do = "cache";
    protected static final String \u00f5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int = "capacities";
    protected static final String \u00d2O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while = "capacity";
    protected static final String \u00f8000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float = "class";
    protected static final String OO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000for = "drivers";
    protected static final String \u00d3O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public = "offline";
    protected static final String \u00d5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = "online";
    protected static final String \u00d4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = "params";
    protected static final String \u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = "project";
    protected static final String \u00f6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if = "online-flush-resource-limit";
    protected static final String \u00d8000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void = "user";
    protected static final String \u00d6O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper = "vfs";
    private static final String \u00d5O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface = "org/opencms/ocee/cache/";
    private static final String \u00d3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = "ocee-cache.dtd";
    private static final String \u00d4O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private = "http://www.alkacon.com/dtd/6.0/";
    private static final String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = "ocee-cache.xml";
    private static final Map<String, String> \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = new HashMap<String, String>();
    private final CmsCacheManager \u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper = CmsCacheManager.getInstance();
    private int \u00f4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null;

    public CmsCacheConfiguration() {
        \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.put("org.opencms.ocee.cache.CmsCacheResources", "org.opencms.ocee.cache.vfs.CmsCacheResources");
        \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.put("org.opencms.ocee.cache.CmsCacheResourceLists", "org.opencms.ocee.cache.vfs.CmsCacheResourceLists");
        \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.put("org.opencms.ocee.cache.CmsCacheFiles", "org.opencms.ocee.cache.vfs.CmsCacheFiles");
        \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.put("org.opencms.ocee.cache.CmsCacheProperties", "org.opencms.ocee.cache.vfs.CmsCacheProperties");
        \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.put("org.opencms.ocee.cache.CmsCachePropertyDefinitions", "org.opencms.ocee.cache.vfs.CmsCachePropertyDefinitions");
        \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.put("org.opencms.ocee.cache.CmsCacheRelations", "org.opencms.ocee.cache.vfs.CmsCacheRelations");
        \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.put("org.opencms.ocee.cache.CmsCacheNonExistentResources", "org.opencms.ocee.cache.vfs.CmsCacheNonExistentResources");
        this.setXmlFileName("ocee-cache.xml");
    }

    public void addCacheInstance(String className, String onlineCapacity, String offlineCapacity, String params) throws Exception {
        block18 : {
            int online;
            block16 : {
                if (\u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.containsKey(className)) {
                    className = \u00d6000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.get(className);
                }
                online = 512;
                try {
                    online = Integer.parseInt(onlineCapacity);
                }
                catch (NumberFormatException e) {
                    if (!CmsLog.INIT.isWarnEnabled()) break block16;
                    CmsLog.INIT.warn((Object)Messages.get().getBundle().key("INIT_ERR_SET_ONLINE_CAPACITY_3", new Object[]{className, onlineCapacity, String.valueOf(online)}), (Throwable)e);
                }
            }
            if (online < 0) {
                online = 512;
                if (CmsLog.INIT.isWarnEnabled()) {
                    CmsLog.INIT.warn((Object)Messages.get().getBundle().key("INIT_WARN_NEGATIVE_ONLINE_CAPACITY_3", new Object[]{className, onlineCapacity, String.valueOf(online)}));
                }
            }
            int offline = -1;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)offlineCapacity)) {
                block17 : {
                    offline = 512;
                    try {
                        offline = Integer.parseInt(offlineCapacity);
                    }
                    catch (NumberFormatException e) {
                        if (!CmsLog.INIT.isErrorEnabled()) break block17;
                        CmsLog.INIT.error((Object)Messages.get().getBundle().key("INIT_ERR_SET_OFFLINE_CAPACITY_3", new Object[]{className, offlineCapacity, String.valueOf(offline)}), (Throwable)e);
                    }
                }
                if (offline < 0) {
                    offline = 512;
                    if (CmsLog.INIT.isWarnEnabled()) {
                        CmsLog.INIT.warn((Object)Messages.get().getBundle().key("INIT_WARN_NEGATIVE_OFFLINE_CAPACITY_3", new Object[]{className, offlineCapacity, String.valueOf(offline)}));
                    }
                }
            }
            if (this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper != null) {
                try {
                    this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.setCacheInstance(className, online, offline, params);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        if (offline != -1) {
                            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_ADD_CACHE_CAPACITY_3", new Object[]{className, new Integer(online), new Integer(offline)}));
                        } else {
                            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_SET_CACHE_CAPACITY_2", new Object[]{className, new Integer(online)}));
                        }
                    }
                }
                catch (CmsIllegalArgumentException e) {
                    if (!CmsLog.INIT.isErrorEnabled()) break block18;
                    CmsLog.INIT.error((Object)Messages.get().getBundle().key("INIT_ERR_SET_CACHE_INSTANCE_1", new Object[]{className}), (Throwable)e);
                }
            }
        }
    }

    public void addXmlDigesterRules(Digester digester) {
        digester.addCallMethod("*/cache", "initConfiguration");
        String xOnlineFlushRulesPath = "*/cache/online-flush-resource-limit";
        digester.addCallMethod(xOnlineFlushRulesPath, "setOnlineFlushResourceLimit", 0);
        String xDriverPath = "*/cache/drivers/";
        digester.addCallMethod(xDriverPath + "vfs", "setVfsDriver", 1);
        digester.addCallParam(xDriverPath + "vfs", 0, "class");
        digester.addCallMethod(xDriverPath + "user", "setUserDriver", 1);
        digester.addCallParam(xDriverPath + "user", 0, "class");
        digester.addCallMethod(xDriverPath + "project", "setProjectDriver", 1);
        digester.addCallParam(xDriverPath + "project", 0, "class");
        String xCapacityPath = "*/capacities/capacity";
        digester.addCallMethod(xCapacityPath, "addCacheInstance", 4);
        digester.addCallParam(xCapacityPath + "/" + "class", 0);
        digester.addCallParam(xCapacityPath + "/" + "online", 1);
        digester.addCallParam(xCapacityPath + "/" + "offline", 2);
        digester.addCallParam(xCapacityPath + "/" + "params", 3);
    }

    public Element generateXml(Element parent) {
        Element cacheElement = parent.addElement("cache");
        if (this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper != null) {
            cacheElement.addElement("online-flush-resource-limit").addText(Integer.toString(this.getOnlineFlushResourceLimit()));
            Element driversElement = cacheElement.addElement("drivers");
            Element vfsElement = driversElement.addElement("vfs").addAttribute("class", this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.getVfsDriver().getClass().getName());
            Element vfsCapacitiesElement = vfsElement.addElement("capacities");
            this.generateCapacitiesXml(vfsCapacitiesElement, CmsVfsCacheInstanceType.VALUES);
            Element projectElement = driversElement.addElement("project").addAttribute("class", this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.getProjectDriver().getClass().getName());
            Element projectCapacitiesElement = projectElement.addElement("capacities");
            this.generateCapacitiesXml(projectCapacitiesElement, CmsProjectCacheInstanceType.VALUES);
            Element userElement = driversElement.addElement("user").addAttribute("class", this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.getUserDriver().getClass().getName());
            Element userCapacitiesElement = userElement.addElement("capacities");
            this.generateCapacitiesXml(userCapacitiesElement, CmsUserCacheInstanceType.VALUES);
        }
        return cacheElement;
    }

    public String getDtdFilename() {
        return "ocee-cache.dtd";
    }

    public String getDtdSystemLocation() {
        return "org/opencms/ocee/cache/";
    }

    public String getDtdUrlPrefix() {
        return "http://www.alkacon.com/dtd/6.0/";
    }

    public int getOnlineFlushResourceLimit() {
        return this.\u00f4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null;
    }

    public String getXmlFileName() {
        return "ocee-cache.xml";
    }

    public void initConfiguration() {
        if (this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper != null) {
            this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.initialize(this);
        }
    }

    public void setOnlineFlushResourceLimit(String resourceLimitString) {
        int resourceLimit;
        block5 : {
            resourceLimit = 50;
            try {
                resourceLimit = Integer.parseInt(resourceLimitString);
            }
            catch (NumberFormatException e) {
                if (!CmsLog.INIT.isWarnEnabled()) break block5;
                CmsLog.INIT.warn((Object)Messages.get().getBundle().key("INIT_ERR_SET_ONLINE_FLUSH_RESOURCE_LIMIT_2", new Object[]{resourceLimitString, String.valueOf(resourceLimit)}), (Throwable)e);
            }
        }
        if (resourceLimit < 0) {
            resourceLimit = 50;
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn((Object)Messages.get().getBundle().key("INIT_WARN_NEGATIVE_FLUSH_RESOURCE_LIMIT_2", new Object[]{resourceLimitString, String.valueOf(resourceLimit)}));
            }
        }
        this.\u00f4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null = resourceLimit;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_ADD_ONLINE_FLUSH_RESOURCE_LIMIT_1", new Object[]{String.valueOf(resourceLimit)}));
        }
    }

    public void setProjectDriver(String className) {
        I_CmsProjectDriver projectDriver;
        block4 : {
            projectDriver = null;
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                projectDriver = (I_CmsProjectDriver)cl.loadClass(className).newInstance();
            }
            catch (Throwable t) {
                if (!CmsLog.INIT.isErrorEnabled()) break block4;
                CmsLog.INIT.error((Object)Messages.get().getBundle().key("INIT_ERR_CREATING_PROJECTCACHE_HANDLER_1", new Object[]{className}), t);
            }
        }
        if (this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper != null) {
            this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.setProjectDriver(projectDriver);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_PROJECTCACHE_HANDLER_SET_1", new Object[]{className}));
        }
    }

    public void setUserDriver(String className) {
        I_CmsUserDriver userDriver;
        block4 : {
            userDriver = null;
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                userDriver = (I_CmsUserDriver)cl.loadClass(className).newInstance();
            }
            catch (Throwable t) {
                if (!CmsLog.INIT.isErrorEnabled()) break block4;
                CmsLog.INIT.error((Object)Messages.get().getBundle().key("INIT_ERR_CREATING_USERCACHE_HANDLER_1", new Object[]{className}), t);
            }
        }
        if (this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper != null) {
            this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.setUserDriver(userDriver);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_USERCACHE_HANDLER_SET_1", new Object[]{className}));
        }
    }

    public void setVfsDriver(String className) {
        I_CmsVfsDriver vfsDriver;
        block4 : {
            vfsDriver = null;
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                vfsDriver = (I_CmsVfsDriver)cl.loadClass(className).newInstance();
            }
            catch (Throwable t) {
                if (!CmsLog.INIT.isErrorEnabled()) break block4;
                CmsLog.INIT.error((Object)Messages.get().getBundle().key("INIT_ERR_CREATING_VFSCACHE_HANDLER_1", new Object[]{className}), t);
            }
        }
        if (this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper != null) {
            this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.setVfsDriver(vfsDriver);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_VFSCACHE_HANDLER_SET_1", new Object[]{className}));
        }
    }

    protected void generateCapacitiesXml(Element capacitiesElement, List<? extends I_CmsCacheInstanceType> types) {
        for (I_CmsCacheInstanceType cacheInstanceType : types) {
            I_CmsCacheInstance cacheInstance = this.\u00d8O00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper.getCacheInstance(cacheInstanceType);
            Element capacityElement = capacitiesElement.addElement("capacity");
            capacityElement.addElement("class").addText(cacheInstance.getClass().getName());
            if (cacheInstanceType.isProjectAware()) {
                capacityElement.addElement("offline").addText(Integer.toString(cacheInstance.getCapacityOffline()));
            }
            capacityElement.addElement("online").addText(Integer.toString(cacheInstance.getCapacityOnline()));
            if (!CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)cacheInstance.getParameters())) continue;
            capacityElement.addElement("params").addText(cacheInstance.getParameters());
        }
    }

    protected void initMembers() {
        CmsOceeManager.getInstance().checkOceeVersion();
    }
}

