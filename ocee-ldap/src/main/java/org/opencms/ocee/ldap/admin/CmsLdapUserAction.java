package org.opencms.ocee.ldap.admin;

import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.workplace.list.CmsListDirectAction;

public class CmsLdapUserAction extends CmsListDirectAction {
    public static final String LIST_COLUMN_FLAGS = "dacf";
    private boolean f183x226a583a;

    public CmsLdapUserAction(String id, boolean ldap) {
        super(id);
        this.f183x226a583a = ldap;
    }

    public boolean isVisible() {
        if (getItem() == null) {
            return super.isVisible();
        }
        Integer flags = (Integer) getItem().get(LIST_COLUMN_FLAGS);
        if (flags == null) {
            return false;
        }
        if (this.f183x226a583a) {
            return CmsLdapManager.hasLdapFlag(flags.intValue());
        }
        if (flags.intValue() < 65536) {
            return true;
        }
        return false;
    }
}
