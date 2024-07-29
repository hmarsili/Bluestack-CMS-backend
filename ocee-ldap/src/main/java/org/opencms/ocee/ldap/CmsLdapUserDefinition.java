package org.opencms.ocee.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

public class CmsLdapUserDefinition extends CmsLdapObject {
    private String f127x8b47f845;
    private String f128x587bb668;
    private Map f129x35b1a7df;
    private Set f130x88521d44 = new HashSet();
    private String f131x1cb34257;
    private String f132x5033f176;
    private String f133x634e24c2;
    private Map f134x36f32df1;
    private Map attributes = null;

    public void addAdditionalMapping(String name, String mapping, String defaultValue, String writeBack) {
        if (this.f134x36f32df1 == null) {
            this.f134x36f32df1 = new HashMap();
        }
        this.f134x36f32df1.put(name, mapping);
        if (defaultValue != null) {
            setDefaultValue(name, defaultValue);
        }
        if ("true".equalsIgnoreCase(writeBack)) {
            this.f130x88521d44.add(name);
        }
    }

    public CmsUser createUserFromSearchResult(Map attributes) {
        Map userAttributes = getAttributeMappings();
        Map additionalInfo = new HashMap();
        String dn = getValue(attributes, "dn", null, null);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(dn)) {
            additionalInfo.put("dn", dn);
        }
        String name = getOuName() + getValueUnescaped(attributes, CmsLdapGroupDefinition.MF_USERID, "", userAttributes);
        Map map = attributes;
        String firstName = getValueUnescaped(map, "firstname", getDefaultValue("firstname"), userAttributes);
        map = attributes;
        String lastName = getValueUnescaped(map, "lastname", getDefaultValue("lastname"), userAttributes);
        map = attributes;
        String mail = getValueUnescaped(map, "mail", getDefaultValue("mail"), userAttributes);
        for (String key : (Set<String>)getAdditionalMappings().keySet()) {
            additionalInfo.put(key, getValueUnescaped(attributes, key, getDefaultValue(key), userAttributes));
        }
        return new CmsUser(CmsUUID.getNullUUID(), name, "", firstName, lastName, mail, 0, CmsLdapManager.LDAP_FLAG, new Date().getTime(), new Date().getTime(), additionalInfo);
    }

    public Map getAdditionalMappings() {
        if (this.f134x36f32df1 == null) {
            this.f134x36f32df1 = new HashMap();
        }
        return Collections.unmodifiableMap(this.f134x36f32df1);
    }

    public Map getAttributeMappings() {
        if (this.attributes == null) {
            this.attributes = new HashMap();
            this.attributes.put(CmsLdapGroupDefinition.MF_USERID, getMappingId());
            this.attributes.put("firstname", getMappingFirstName());
            this.attributes.put("lastname", getMappingLastName());
            this.attributes.put("mail", getMappingMail());
            this.attributes.put("description", getMappingDescription());
            this.attributes.put("address", getMappingAddress());
            this.attributes.putAll(getAdditionalMappings());
            this.attributes = Collections.unmodifiableMap(this.attributes);
        }
        return this.attributes;
    }

    public String getDefaultValue(String name) {
        if (this.f129x35b1a7df == null || !this.f129x35b1a7df.containsKey(name)) {
            return "";
        }
        return (String) this.f129x35b1a7df.get(name);
    }

    public String getMappingAddress() {
        return this.f127x8b47f845;
    }

    public String getMappingDescription() {
        return this.f128x587bb668;
    }

    public String getMappingFirstName() {
        return this.f132x5033f176;
    }

    public String getMappingLastName() {
        return this.f131x1cb34257;
    }

    public String getMappingMail() {
        return this.f133x634e24c2;
    }

    public String[] getSearchAttributes() {
        Map userAttributes = getAttributeMappings();
        List attributes = new ArrayList();
        for (String mappingKey : (Set<String>)userAttributes.keySet()) {
            String key = (String) userAttributes.get(mappingKey);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(key)) {
                attributes.add(key);
            }
        }
        return (String[]) attributes.toArray(new String[attributes.size()]);
    }

    public boolean isWriteBackMapping(String name) {
        return this.f130x88521d44.contains(name);
    }

    public void setDefaultValue(String name, String value) {
        if (this.f129x35b1a7df == null) {
            this.f129x35b1a7df = new HashMap();
        }
        this.f129x35b1a7df.put(name, value);
    }

    public void setMappingAddress(String mappingAddress, String defaultValue) {
        this.f127x8b47f845 = mappingAddress;
        if (defaultValue != null) {
            setDefaultValue("address", defaultValue);
        }
    }

    public void setMappingDescription(String mappingDescription, String defaultValue) {
        this.f128x587bb668 = mappingDescription;
        if (defaultValue != null) {
            setDefaultValue("description", defaultValue);
        }
    }

    public void setMappingFirstName(String mappingFirstName, String defaultValue) {
        this.f132x5033f176 = mappingFirstName;
        if (defaultValue != null) {
            setDefaultValue("firstname", defaultValue);
        }
    }

    public void setMappingLastName(String mappingName, String defaultValue) {
        this.f131x1cb34257 = mappingName;
        if (defaultValue != null) {
            setDefaultValue("lastname", defaultValue);
        }
    }

    public void setMappingMail(String mappingMail, String defaultValue) {
        this.f133x634e24c2 = mappingMail;
        if (defaultValue != null) {
            setDefaultValue("mail", defaultValue);
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        result.append(getClass().getName());
        result.append(", ou=" + getOuName());
        result.append("]");
        return result.toString();
    }

    public void updateUserFromSearchResult(CmsUser user, Map attributes) {
        Map userAttributes = getAttributeMappings();
        String dn = getValue(attributes, "dn", "", null);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(dn)) {
            user.setAdditionalInfo("dn", dn);
        }
        for (String key : (Set<String>)getAdditionalMappings().keySet()) {
            user.setAdditionalInfo(key, getValueUnescaped(attributes, key, getDefaultValue(key), userAttributes));
        }
        String loginName = getOuName() + getValueUnescaped(attributes, CmsLdapGroupDefinition.MF_USERID, "", userAttributes);
        if (CmsStringUtil.isNotEmpty(loginName)) {
            user.setName(loginName);
        }
        String firstName = getValueUnescaped(attributes, "firstname", getDefaultValue("firstname"), userAttributes);
        if (CmsStringUtil.isNotEmpty(firstName)) {
            user.setFirstname(firstName);
        }
        String lastName = getValueUnescaped(attributes, "lastname", getDefaultValue("lastname"), userAttributes);
        if (CmsStringUtil.isNotEmpty(lastName)) {
            user.setLastname(lastName);
        }
        String description = getValueUnescaped(attributes, "description", getDefaultValue("description"), userAttributes);
        if (CmsStringUtil.isNotEmpty(description)) {
            user.setDescription(description);
        }
        String mail = getValueUnescaped(attributes, "mail", getDefaultValue("mail"), userAttributes);
        if (CmsStringUtil.isNotEmpty(mail) && CmsLdapManager.getInstance() != null) {
            user.setEmail(mail);
        }
        String address = getValueUnescaped(attributes, "address", getDefaultValue("address"), userAttributes);
        if (CmsStringUtil.isNotEmpty(address)) {
            user.setAddress(address);
        }
        if (!CmsLdapManager.hasLdapFlag(user.getFlags())) {
            user.setFlags(user.getFlags() ^ CmsLdapManager.LDAP_FLAG);
        }
    }
}
