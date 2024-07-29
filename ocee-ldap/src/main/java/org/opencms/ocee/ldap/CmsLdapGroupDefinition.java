package org.opencms.ocee.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.ldap.Rdn;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.ocee.ldap.admin.CmsLdapGroupSelectDialog;
import org.opencms.util.CmsUUID;

public class CmsLdapGroupDefinition extends CmsLdapObject {
    public static final String MF_FULLDN = "fulldn";
    public static final String MF_MBURL = "mburl";
    public static final String MF_NESTED = "nested-fulldn";
    public static final String MF_USERID = "userid";
    private String mappingName;
    private String mappingMember;
    private String filterByMember;
    private String f91xdb267f24 = MF_FULLDN;
    private Map<String,String> atributes = null;
    private String[] searchAtributtes = null;

    public CmsGroup createGroupFromSearchResult(Map attributes) {
        return new CmsGroup(CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), getOuName() + getValueUnescaped(attributes, "groupid", "", getAttributeMappings()), getValueUnescaped(attributes, CmsLdapGroupSelectDialog.PARAM_GROUPNAME, "", getAttributeMappings()), CmsLdapManager.LDAP_FLAG);
    }

    public Map getAttributeMappings() {
        if (this.atributes == null) {
            this.atributes = new HashMap();
            this.atributes.put("groupid", getMappingId());
            this.atributes.put(CmsLdapGroupSelectDialog.PARAM_GROUPNAME, getMappingName());
            this.atributes.put("member", getMappingMember());
            this.atributes = Collections.unmodifiableMap(this.atributes);
        }
        return this.atributes;
    }

    public String getFilterByMember() {
        return this.filterByMember;
    }

    public String getFilterByMember(CmsUser user) {
        String value;
        if (getMembersFormat().equals(MF_FULLDN) || getMembersFormat().equals(MF_NESTED)) {
            value = CmsLdapManager.getInstance().getRelativeDN((String) user.getAdditionalInfo("dn"));
        } else {
            value = user.getSimpleName();
        }
        return getFilterByMemberValue(value, true);
    }

    public String getFilterByMemberValue(String value, boolean escape) {
        String filter = getFilterByMember();
        String memberValue = value;
        if (escape) {
            memberValue = Rdn.escapeValue(memberValue);
        }
        return filter.replace("?", memberValue);
    }

    public String getMappingMember() {
        return this.mappingMember;
    }

    public String getMappingName() {
        return this.mappingName;
    }

    public String getMembersFormat() {
        return this.f91xdb267f24;
    }

    public String[] getSearchAttributes() {
        if (this.searchAtributtes == null) {
            this.searchAtributtes = new String[2];
            this.searchAtributtes[0] = getMappingId();
            this.searchAtributtes[1] = getMappingName();
        }
        return this.searchAtributtes;
    }

    public void setFilterByMember(String filterByMember) {
        this.filterByMember = filterByMember;
    }

    public void setMappingMember(String mappingMember) {
        this.mappingMember = mappingMember;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    public void setMembersFormat(String membersFormat) {
        this.f91xdb267f24 = membersFormat;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        result.append(getClass().getName());
        result.append(", ou=" + getOuName());
        result.append(", format=" + this.f91xdb267f24);
        result.append("]");
        return result.toString();
    }
}
