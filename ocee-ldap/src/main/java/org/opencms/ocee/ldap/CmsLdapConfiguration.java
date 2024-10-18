package org.opencms.ocee.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.digester3.Digester;
import org.dom4j.Element;
import org.opencms.configuration.A_CmsXmlConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.util.CmsStringUtil;

public class CmsLdapConfiguration extends A_CmsXmlConfiguration {
    public static final String C_LDAP_UPDATE_GROUP_ALL = "all";
    public static final String C_LDAP_UPDATE_GROUP_MEMBERSHIP = "membership";
    public static final String C_LDAP_UPDATE_GROUP_NONE = "none";
    protected static final String f1xf77b04e9 = "sync-interval";
    protected static final String f2xbd4dae9b = "enabled-cmsusers";
    protected static final String f3xad5712b4 = "by-member";
    protected static final String f4x9e212ab5 = "user-definitions";
    protected static final String all = "all";
    protected static final String f6x9d2b144b = "lookup-defaultusers";
    protected static final String f7x24465687 = "ldap-group";
    protected static final String f8x3454ae71 = "cache-live";
    protected static final String f9x4f50b538 = "groupname";
    protected static final String mail = "mail";
    protected static final String f11xbb78c9c2 = "page-size";
    protected static final String f12x77896254 = "ou-definitions";
    protected static final String f13xa547efd7 = "digest-type";
    protected static final String f14xdbf31040 = "firstname";
    protected static final String f15x58894450 = "digest-encoding";
    protected static final String f16xee8245e8 = "group-filters";
    protected static final String f17xe565c82e = "group-definition";
    protected static final String f18x8b47f845 = "providers";
    private static final String ldapConfiguration = "ocee-ldap.xml";
    protected static final String f20x214dc48d = "write-back";
    protected static final String f21x45ceda32 = "additional-mappings";
    protected static final String f22x587bb668 = "role";
    protected static final String userMappings = "user-mappings";
    protected static final String f24x46c06591 = "group-role-mapping";
    protected static final String f25x994236bc = "provider";
    protected static final String f26x730e473c = "context";
    protected static final String f27xe6caf6e3 = "group";
    private static final String dtdFilename = "ocee-ldap.dtd";
    protected static final String f29x35b1a7df = "contexts";
    protected static final String userAccess = "user-access";
    protected static final String f31xd1e6b805 = "membersformat";
    private static final String f32xb873ace5 = "userPassword";
    protected static final String ur = "url";
    protected static final String f34xe4f71b09 = "password";
    protected static final String f35xc79e744a = "group-access";
    protected static final String f36xe84905eb = "group-consistency";
    protected static final String f37xc51efe34 = "group-role-mappings";
    protected static final String member = "member";
    protected static final String ldap = "ldap";
    protected static final String f40x736e9399 = "group-mappings";
    protected static final String f41x1c9eebff = "attribute";
    protected static final String f42x7ddafa6 = "group-definitions";
    protected static final String f43x46f187e5 = "user-definition";
    protected static final String userFilters = "user-filters";
    private static final String dtdUrlPrefix = "http://www.alkacon.com/dtd/6.0/";
    protected static final String f46x589ec570 = "ou-name";
    protected static final String userId = "userid";
    protected static final String f48x634e24c2 = "editable";
    protected static final String f49x5767f4c7 = "pwd-mapping";
    protected static final String f50x36f32df1 = "by-name";
    private static final String dtdSystemLocation = "org/opencms/ocee/ldap/";
    protected static final String groupId = "groupid";
    protected static final String f53xb4694b2d = "description";
    protected static final String f54x7390ff44 = "lastname";
    protected static final String f55xdb267f24 = "address";
    protected static final String f56xdec0e25a = "search";
    protected static final String ouDefinition = "ou-definition";
    private String f58x20856c4b = "Ldap Group";
    private long f59xa7ae0fbf;
    private List f60xa1fbee48;
    private List f61x350cdc9c;
    private long f62x9e5a1be8 = Long.MAX_VALUE;
    private String classname;
    private boolean lookupDefaultUsers = true;
    private CmsLdapOuDefinition f65x88521d44;
    private boolean f66x153c1aea = true;
    private String f67x1cb34257;
    private Map ouDefList;
    private String f69x5ae05e43;
    private boolean f70x5033f176;
    private CmsLdapGroupRoleMappings f71x78967bd8;
    private String f72xc6f0494e;
    private String f73x136d2e7c;
    private boolean pwdEditable;
    private List ouDefsList;
    private int pageSize = -1;
    private Map ldapProviders;

    public CmsLdapConfiguration() {
        setXmlFileName(ldapConfiguration);
    }

    public void addGroupDefinition(CmsLdapGroupDefinition groupDef) {
        if (this.f60xa1fbee48 == null) {
            this.f60xa1fbee48 = new ArrayList();
        }
        this.f60xa1fbee48.add(groupDef);
        this.f65x88521d44.addGroupDefinition(groupDef);
    }

    public void addGroupRoleMapping(CmsLdapGroupRoleMapping groupRoleMapping) {
        if (this.f71x78967bd8 == null) {
            this.f71x78967bd8 = new CmsLdapGroupRoleMappings();
        }
        this.f71x78967bd8.add(groupRoleMapping);
    }

    public void addLdapProvider(CmsLdapProvider ldapProvider) {
        if (this.ldapProviders == null) {
            this.ldapProviders = new HashMap();
        }
        this.ldapProviders.put(ldapProvider.getProviderUrl(), ldapProvider);
    }

    public void addUserDefinition(CmsLdapUserDefinition userDef) {
        if (this.f61x350cdc9c == null) {
            this.f61x350cdc9c = new ArrayList();
        }
        this.f61x350cdc9c.add(userDef);
        this.f65x88521d44.addUserDefinition(userDef);
    }

    public void addXmlDigesterRules(Digester digester) {
        digester.addCallMethod("*/ldap", "initConfiguration");
        digester.addSetProperties("*/ldap", "class", "className");
        digester.addCallMethod("*/ldap/ldap-group", "setLdapGroup", 2);
        digester.addCallParam("*/ldap/ldap-group", 0);
        digester.addCallParam("*/ldap/ldap-group", 1, "enabled");
        digester.addCallMethod("*/ldap/password/digest-encoding", "setPwdDigestEncoding", 0);
        digester.addCallMethod("*/ldap/password/digest-type", "setPwdDigestType", 0);
        digester.addCallMethod("*/ldap/password/editable", "setPwdEditable", 0);
        digester.addCallMethod("*/ldap/password/pwd-mapping", "setPwdMapping", 0);
        digester.addCallMethod("*/ldap/enabled-cmsusers", "setEnabledCmsUsers", 0);
        digester.addCallMethod("*/ldap/lookup-defaultusers", "setLookupDefaultUsers", 0);
        digester.addCallMethod("*/ldap/group-consistency", "setGroupConsistency", 0);
        digester.addCallMethod("*/ldap/sync-interval", "setSyncInterval", 0);
        digester.addCallMethod("*/ldap/cache-live", "setCacheLive", 0);
        digester.addCallMethod("*/ldap/page-size", "setPageSize", 0);
       
        
        digester.addObjectCreate("*/ldap/providers/provider", CmsLdapProvider.class);
        digester.addSetProperties("*/ldap/providers/provider", ur, "providerUrl");
        digester.addSetProperties("*/ldap/providers/provider", "default", "default");
        String paramPath = "*/ldap/providers/provider/param";
        digester.addCallMethod(paramPath, "addConfigurationParameter", 2);
        digester.addCallParam(paramPath, 0, "name");
        digester.addCallParam(paramPath, 1);
        digester.addCallMethod("*/ldap/providers/provider", "initConfiguration");
        digester.addSetNext("*/ldap/providers/provider", "addLdapProvider");
        digester.addCallMethod("*/ldap/providers", "setRootOu");
        digester.addCallMethod("*/ou-definitions", "setRootOu");
        digester.addCallMethod("*/ou-definitions/ou-definition" + "/" + f46x589ec570, "setOuName", 0);
        String groupDefPath = "*/group-definitions/group-definition";
        digester.addObjectCreate(groupDefPath, CmsLdapGroupDefinition.class);
        digester.addSetProperties(groupDefPath, f31xd1e6b805, "membersFormat");
        digester.addSetNext(groupDefPath, "addGroupDefinition");
        digester.addCallMethod(groupDefPath + "/" + f35xc79e744a + "/" + f16xee8245e8 + "/" + "all", "setFilterAll", 0);
        digester.addCallMethod(groupDefPath + "/" + f35xc79e744a + "/" + f16xee8245e8 + "/" + f50x36f32df1, "setFilterByName", 0);
        digester.addCallMethod(groupDefPath + "/" + f35xc79e744a + "/" + f16xee8245e8 + "/" + f3xad5712b4, "setFilterByMember", 0);
        digester.addCallMethod(groupDefPath + "/" + f35xc79e744a + "/" + f16xee8245e8 + "/" + f56xdec0e25a, "setFilterSearch", 0);
        digester.addCallMethod(groupDefPath + "/" + f35xc79e744a + "/" + f29x35b1a7df + "/" + f26x730e473c, "addAccessContext", 0);
        digester.addCallMethod(groupDefPath + "/" + f40x736e9399 + "/" + groupId, "setMappingId", 0);
        digester.addCallMethod(groupDefPath + "/" + f40x736e9399 + "/" + "groupname", "setMappingName", 0);
        digester.addCallMethod(groupDefPath + "/" + f40x736e9399 + "/" + member, "setMappingMember", 0);
        digester.addCallMethod(groupDefPath + "/" + f48x634e24c2, "setEditable", 0);
        String groupRolePath = "*/group-role-mappings/group-role-mapping";
        digester.addObjectCreate(groupRolePath, CmsLdapGroupRoleMapping.class);
        digester.addCallMethod(groupRolePath + "/" + f27xe6caf6e3, "setGroupName", 0);
        digester.addCallMethod(groupRolePath + "/" + f22x587bb668, "setRoleName", 0);
        digester.addSetNext(groupRolePath, "addGroupRoleMapping");
        String userDefPath = "*/user-definitions/user-definition";
        digester.addObjectCreate(userDefPath, CmsLdapUserDefinition.class);
        digester.addSetNext(userDefPath, "addUserDefinition");
        digester.addCallMethod(userDefPath + "/" + userAccess + "/" + userFilters + "/" + "all", "setFilterAll", 0);
        digester.addCallMethod(userDefPath + "/" + userAccess + "/" + userFilters + "/" + f50x36f32df1, "setFilterByName", 0);
        digester.addCallMethod(userDefPath + "/" + userAccess + "/" + userFilters + "/" + f56xdec0e25a, "setFilterSearch", 0);
        digester.addCallMethod(userDefPath + "/" + userAccess + "/" + f29x35b1a7df + "/" + f26x730e473c, "addAccessContext", 0);
        digester.addCallMethod(userDefPath + "/" + userMappings + "/" + "userid", "setMappingId", 0);
        digester.addCallMethod(userDefPath + "/" + userMappings + "/" + f14xdbf31040, "setMappingFirstName", 2);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f14xdbf31040, 0);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f14xdbf31040, 1, "default");
        digester.addCallMethod(userDefPath + "/" + userMappings + "/" + f54x7390ff44, "setMappingLastName", 2);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f54x7390ff44, 0);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f54x7390ff44, 1, "default");
        digester.addCallMethod(userDefPath + "/" + userMappings + "/" + mail, "setMappingMail", 2);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + mail, 0);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + mail, 1, "default");
        digester.addCallMethod(userDefPath + "/" + userMappings + "/" + f53xb4694b2d, "setMappingDescription", 2);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f53xb4694b2d, 0);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f53xb4694b2d, 1, "default");
        digester.addCallMethod(userDefPath + "/" + userMappings + "/" + f55xdb267f24, "setMappingAddress", 2);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f55xdb267f24, 0);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f55xdb267f24, 1, "default");
        digester.addCallMethod(userDefPath + "/" + userMappings + "/" + f21x45ceda32 + "/" + f41x1c9eebff, "addAdditionalMapping", 4);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f21x45ceda32 + "/" + f41x1c9eebff, 0, "name");
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f21x45ceda32 + "/" + f41x1c9eebff, 1);
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f21x45ceda32 + "/" + f41x1c9eebff, 2, "default");
        digester.addCallParam(userDefPath + "/" + userMappings + "/" + f21x45ceda32 + "/" + f41x1c9eebff, 3, f20x214dc48d);
        digester.addCallMethod(userDefPath + "/" + f48x634e24c2, "setEditable", 0);
    }

    public Element generateXml(Element parent) {
        Element ldapElement = parent.addElement("ldap").addAttribute("class", this.classname);
        ldapElement.addElement(f7x24465687).addText(this.f58x20856c4b).addAttribute("enabled", Boolean.toString(this.f66x153c1aea));
        Element passwordElement = ldapElement.addElement(f34xe4f71b09);
        passwordElement.addElement(f15x58894450).addText(getPwdDigestEncoding());
        passwordElement.addElement(f13xa547efd7).addText(getPwdDigestType());
        passwordElement.addElement(f48x634e24c2).addText(Boolean.toString(isPwdEditable()));
        if (this.f73x136d2e7c != null) {
            passwordElement.addElement(f49x5767f4c7).addText(this.f73x136d2e7c);
        }
        ldapElement.addElement(f2xbd4dae9b).addText(Boolean.toString(isEnabledCmsUsers()));
        ldapElement.addElement(f6x9d2b144b).addText(Boolean.toString(isLookupDefaultUsers()));
        ldapElement.addElement(f36xe84905eb).addText(getGroupConsistency());
        ldapElement.addElement(f1xf77b04e9).addText(Long.toString(getSyncInterval()));
        ldapElement.addElement(f8x3454ae71).addText(Long.toString(getCacheLive()));
        ldapElement.addElement(f11xbb78c9c2).addText(Integer.toString(getPageSize()));
        Element providersElement = ldapElement.addElement(f18x8b47f845);
        String[] providerUrls = (String[]) getLdapProviders().keySet().toArray(new String[0]);
        Arrays.sort(providerUrls);
        for (Object obj : providerUrls) {
            CmsLdapProvider p = (CmsLdapProvider) getLdapProviders().get(obj);
            m0x226a583a(providersElement.addElement(f25x994236bc).addAttribute(ur, p.getProviderUrl()).addAttribute("default", Boolean.toString(p.isDefault())), p);
        }
        CmsLdapOuDefinition rootOuDef = null;
        Element ouDefsElement = null;
        Iterator itOuDefs = getOuDefsList().iterator();
        while (true) {
            if (!itOuDefs.hasNext() && rootOuDef == null) {
                return ldapElement;
            }
            CmsLdapOuDefinition ouDef;
            if (itOuDefs.hasNext()) {
                ouDef = (CmsLdapOuDefinition) itOuDefs.next();
            } else {
                ouDef = rootOuDef;
            }
            boolean isRootOu = ouDef.getOuName().length() == 0 || ouDef.getOuName().equals("/");
            if (isRootOu && itOuDefs.hasNext()) {
                rootOuDef = ouDef;
            } else {
                Element accessElement;
                Element filtersElement;
                Element contextsElement;
                Element mappingsElement;
                Element baseElement = ldapElement;
                if (!isRootOu) {
                    if (ouDefsElement == null) {
                        ouDefsElement = ldapElement.addElement(f12x77896254);
                    }
                    baseElement = ouDefsElement.addElement(ouDefinition);
                    baseElement.addElement(f46x589ec570).addText(ouDef.getOuName());
                }
                Element groupDefsElement = baseElement.addElement(f42x7ddafa6);
                for (CmsLdapGroupDefinition groupDef : (List<CmsLdapGroupDefinition>)ouDef.getGroupDefinitions()) {
                    Element groupDefElement = groupDefsElement.addElement(f17xe565c82e);
                    groupDefElement.addAttribute(f31xd1e6b805, groupDef.getMembersFormat());
                    accessElement = groupDefElement.addElement(f35xc79e744a);
                    filtersElement = accessElement.addElement(f16xee8245e8);
                    filtersElement.addElement("all").addText(groupDef.getFilterAll());
                    filtersElement.addElement(f50x36f32df1).addText(groupDef.getFilterByName());
                    if (groupDef.getFilterByMember() != null) {
                        filtersElement.addElement(f3xad5712b4).addText(groupDef.getFilterByMember());
                    }
                    if (groupDef.getFilterSearch() != null) {
                        filtersElement.addElement(f56xdec0e25a).addText(groupDef.getFilterSearch());
                    }
                    contextsElement = accessElement.addElement(f29x35b1a7df);
                    for (String addText : (List<String>) groupDef.getAccessContexts()) {
                        contextsElement.addElement(f26x730e473c).addText(addText);
                    }
                    mappingsElement = groupDefElement.addElement(f40x736e9399);
                    mappingsElement.addElement(groupId).addText(groupDef.getMappingId());
                    mappingsElement.addElement("groupname").addText(groupDef.getMappingName());
                    mappingsElement.addElement(member).addText(groupDef.getMappingMember());
                    groupDefElement.addElement(f48x634e24c2).addText(Boolean.toString(groupDef.isEditable()));
                }
                if (isRootOu && this.f71x78967bd8 != null) {
                    Element groupRoleMapsElement = ldapElement.addElement(f37xc51efe34);
                    for (CmsLdapGroupRoleMapping groupRoleMapping : (List<CmsLdapGroupRoleMapping>)this.f71x78967bd8.getConfiguredMappings()) {
                        Element groupRoleMapElement = groupRoleMapsElement.addElement(f24x46c06591);
                        groupRoleMapElement.addElement(f27xe6caf6e3).addText(groupRoleMapping.getGroupName());
                        groupRoleMapElement.addElement(f22x587bb668).addText(groupRoleMapping.getRoleName());
                    }
                }
                Element userDefsElement = baseElement.addElement(f4x9e212ab5);
                for (CmsLdapUserDefinition userDef : (List<CmsLdapUserDefinition>)ouDef.getUserDefinitions()) {
                    Element userDefElement = userDefsElement.addElement(f43x46f187e5);
                    accessElement = userDefElement.addElement(userAccess);
                    filtersElement = accessElement.addElement(userFilters);
                    filtersElement.addElement("all").addText(userDef.getFilterAll());
                    filtersElement.addElement(f50x36f32df1).addText(userDef.getFilterByName());
                    if (userDef.getFilterSearch() != null) {
                        filtersElement.addElement(f56xdec0e25a).addText(userDef.getFilterSearch());
                    }
                    contextsElement = accessElement.addElement(f29x35b1a7df);
                    for (String addText2 : (List<String>)userDef.getAccessContexts()) {
                        contextsElement.addElement(f26x730e473c).addText(addText2);
                    }
                    mappingsElement = userDefElement.addElement(userMappings);
                    mappingsElement.addElement("userid").addText(userDef.getMappingId());
                    Element mapElement = mappingsElement.addElement(f14xdbf31040);
                    String defaultValue = userDef.getDefaultValue(f14xdbf31040);
                    if (CmsStringUtil.isNotEmpty(defaultValue)) {
                        mapElement.addAttribute("default", defaultValue);
                    }
                    mapElement.addText(userDef.getMappingFirstName());
                    mapElement = mappingsElement.addElement(f54x7390ff44);
                    defaultValue = userDef.getDefaultValue(f54x7390ff44);
                    if (CmsStringUtil.isNotEmpty(defaultValue)) {
                        mapElement.addAttribute("default", defaultValue);
                    }
                    mapElement.addText(userDef.getMappingLastName());
                    mapElement = mappingsElement.addElement(mail);
                    defaultValue = userDef.getDefaultValue(mail);
                    if (CmsStringUtil.isNotEmpty(defaultValue)) {
                        mapElement.addAttribute("default", defaultValue);
                    }
                    mapElement.addText(userDef.getMappingMail());
                    mapElement = mappingsElement.addElement(f53xb4694b2d);
                    defaultValue = userDef.getDefaultValue(f53xb4694b2d);
                    if (CmsStringUtil.isNotEmpty(defaultValue)) {
                        mapElement.addAttribute("default", defaultValue);
                    }
                    mapElement.addText(userDef.getMappingDescription());
                    mapElement = mappingsElement.addElement(f55xdb267f24);
                    defaultValue = userDef.getDefaultValue(f55xdb267f24);
                    if (CmsStringUtil.isNotEmpty(defaultValue)) {
                        mapElement.addAttribute("default", defaultValue);
                    }
                    mapElement.addText(userDef.getMappingAddress());
                    Element additionalMapsElement = mappingsElement.addElement(f21x45ceda32);
                    Map attributeMappings = userDef.getAdditionalMappings();
                    List<String> attributeList = new ArrayList(attributeMappings.keySet());
                    Collections.sort(attributeList);
                    for (String key : attributeList) {
                        String value = (String) attributeMappings.get(key);
                        Element attributeElement = additionalMapsElement.addElement(f41x1c9eebff);
                        attributeElement.addAttribute("name", key);
                        defaultValue = userDef.getDefaultValue(key);
                        if (CmsStringUtil.isNotEmpty(defaultValue)) {
                            attributeElement.addAttribute("default", defaultValue);
                        }
                        if (userDef.isWriteBackMapping(key)) {
                            attributeElement.addAttribute(f20x214dc48d, "true");
                        }
                        if (value == null) {
                            attributeElement.addText("");
                        } else {
                            attributeElement.addText(value);
                        }
                    }
                    userDefElement.addElement(f48x634e24c2).addText(Boolean.toString(userDef.isEditable()));
                }
                if (ouDef == rootOuDef) {
                    rootOuDef = null;
                }
            }
        }
    }

    public long getCacheLive() {
        return this.f59xa7ae0fbf;
    }

    public String getClassName() {
        return this.classname;
    }

    public String getDtdFilename() {
        return dtdFilename;
    }

    public String getDtdSystemLocation() {
        return dtdSystemLocation;
    }

    public String getDtdUrlPrefix() {
        return dtdUrlPrefix;
    }

    public String getGroupConsistency() {
        return this.f72xc6f0494e;
    }

    public List getGroupDefinitions() {
        return this.f60xa1fbee48;
    }

    public CmsLdapGroupRoleMappings getGroupRoleMappings() {
        return this.f71x78967bd8;
    }

    public String getLdapGroupName() {
        return this.f58x20856c4b;
    }

    public Map getLdapProviders() {
        return this.ldapProviders;
    }

    public Map getOuDefs() {
        return this.ouDefList;
    }

    public List getOuDefsList() {
        return this.ouDefsList;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public String getPwdDigestEncoding() {
        return this.f67x1cb34257;
    }

    public String getPwdDigestType() {
        return this.f69x5ae05e43;
    }

    public String getPwdMapping() {
        if (this.f73x136d2e7c != null) {
            return this.f73x136d2e7c;
        }
        return f32xb873ace5;
    }

    public long getSyncInterval() {
        return this.f62x9e5a1be8;
    }

    public List getUserDefinitions() {
        return this.f61x350cdc9c;
    }

    public void initConfiguration() {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null) {
            manager.initialize(this);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_DRIVER_CONFIGURED_0));
        }
    }

    public boolean isEnabledCmsUsers() {
        return this.f70x5033f176;
    }

    public boolean isLdapGroupEnabled() {
        return this.f66x153c1aea;
    }

    public boolean isLookupDefaultUsers() {
        return this.lookupDefaultUsers;
    }

    public boolean isPwdEditable() {
        return this.pwdEditable;
    }

    public boolean isUsePaging() {
        return this.pageSize > -1;
    }

    public void setCacheLive(String cacheLive) {
        try {
            this.f59xa7ae0fbf = Long.parseLong(cacheLive);
        } catch (Exception e) {
            this.f59xa7ae0fbf = 0;
        }
    }

    public void setClassName(String className) {
        this.classname = className;
    }

    public void setEnabledCmsUsers(String enabledCmsUsers) {
        try {
            this.f70x5033f176 = Boolean.valueOf(enabledCmsUsers).booleanValue();
        } catch (Exception e) {
            this.f70x5033f176 = true;
        }
    }

    public void setGroupConsistency(String groupConsistency) {
        this.f72xc6f0494e = groupConsistency;
    }

    public void setLdapGroup(String groupName, String enabled) {
        this.f58x20856c4b = groupName;
        try {
            this.f66x153c1aea = Boolean.valueOf(enabled).booleanValue();
        } catch (Throwable th) {
            this.f66x153c1aea = false;
        }
    }

    public void setLookupDefaultUsers(String lookupDefaultUsers) {
        try {
            this.lookupDefaultUsers = Boolean.valueOf(lookupDefaultUsers).booleanValue();
        } catch (Exception e) {
            this.lookupDefaultUsers = true;
        }
    }

    public void setOuName(String ouName) {
        this.f65x88521d44 = new CmsLdapOuDefinition(ouName);
        m1x226a583a();
    }

    public void setPageSize(String pageSize) {
        try {
            this.pageSize = Integer.parseInt(pageSize);
        } catch (Throwable th) {
            this.pageSize = -1;
        }
    }

    public void setPwdDigestEncoding(String pwdDigestEncoding) {
        this.f67x1cb34257 = pwdDigestEncoding;
    }

    public void setPwdDigestType(String pwdDigestType) {
        this.f69x5ae05e43 = pwdDigestType;
    }

    public void setPwdEditable(String pwdEditable) {
        try {
            this.pwdEditable = Boolean.valueOf(pwdEditable).booleanValue();
        } catch (Throwable th) {
            this.pwdEditable = false;
        }
    }

    public void setPwdMapping(String pwdMapping) {
        this.f73x136d2e7c = pwdMapping;
    }

    public void setRootOu() {
        if (this.ouDefList == null || !this.ouDefList.containsKey("")) {
            this.f65x88521d44 = new CmsLdapOuDefinition("");
            m1x226a583a();
            return;
        }
        this.f65x88521d44 = (CmsLdapOuDefinition) this.ouDefList.get("");
    }

    public void setSyncInterval(String syncInterval) {
        try {
            this.f62x9e5a1be8 = Long.parseLong(syncInterval);
        } catch (Throwable th) {
            this.f62x9e5a1be8 = Long.MAX_VALUE;
        }
    }

    private void m1x226a583a() {
        if (this.ouDefList == null) {
            this.ouDefList = new HashMap();
        }
        if (this.ouDefList.containsKey(this.f65x88521d44.getOuName())) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_LDAP_ADD_OU_1, this.f65x88521d44.getOuName()));
        }
        this.ouDefList.put(this.f65x88521d44.getOuName(), this.f65x88521d44);
        if (this.ouDefsList == null) {
            this.ouDefsList = new ArrayList();
        }
        if (!this.ouDefsList.contains(this.f65x88521d44)) {
            this.ouDefsList.add(this.f65x88521d44);
        }
    }

    private Element m0x226a583a(Element parent, I_CmsConfigurationParameterHandler configurationParameter) {
        Map configuration = configurationParameter.getConfiguration();
        List<String> parameterList = new ArrayList(configuration.keySet());
        Collections.sort(parameterList);
        for (String key : parameterList) {
            String value = (String) configuration.get(key);
            Element paramElement = parent.addElement("param").addAttribute("name", key);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                paramElement.addText(value);
            }
        }
        return parent;
    }

    protected void initMembers() {
        CmsOceeManager.getInstance().checkOceeVersion();
    }
}
