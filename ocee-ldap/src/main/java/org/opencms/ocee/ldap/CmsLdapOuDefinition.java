package org.opencms.ocee.ldap;

import java.util.ArrayList;
import java.util.List;
import org.opencms.main.CmsLog;

public class CmsLdapOuDefinition {
    private List f121x226a583a = new ArrayList();
    private String f122xbb78c9c2;
    private List f123x350cdc9c = new ArrayList();

    public CmsLdapOuDefinition(String ouName) {
        this.f122xbb78c9c2 = ouName;
        if (!this.f122xbb78c9c2.endsWith("/")) {
            this.f122xbb78c9c2 += "/";
        }
        if (this.f122xbb78c9c2.startsWith("/")) {
            this.f122xbb78c9c2 = this.f122xbb78c9c2.substring("/".length());
        }
    }

    public void addGroupDefinition(CmsLdapGroupDefinition groupDef) {
        groupDef.setOuName(this.f122xbb78c9c2);
        this.f123x350cdc9c.add(groupDef);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_OBJECT_1, new Object[]{groupDef}));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_OBJECT_SEARCH_CONTEXT_1, new Object[]{groupDef.getAccessContexts()}));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_OBJECT_ATTRIBUTE_MAPPINGS_1, new Object[]{groupDef.getAttributeMappings()}));
        }
    }

    public void addUserDefinition(CmsLdapUserDefinition userDef) {
        userDef.setOuName(this.f122xbb78c9c2);
        this.f121x226a583a.add(userDef);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_OBJECT_1, new Object[]{userDef}));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_OBJECT_SEARCH_CONTEXT_1, new Object[]{userDef.getAccessContexts()}));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_OBJECT_ATTRIBUTE_MAPPINGS_1, new Object[]{userDef.getAdditionalMappings()}));
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof CmsLdapOuDefinition) {
            return getOuName().equals(((CmsLdapOuDefinition) obj).getOuName());
        }
        return false;
    }

    public List getGroupDefinitions() {
        return this.f123x350cdc9c;
    }

    public String getOuName() {
        return this.f122xbb78c9c2;
    }

    public List getUserDefinitions() {
        return this.f121x226a583a;
    }

    public int hashCode() {
        return getOuName().hashCode();
    }
}
