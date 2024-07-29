package org.opencms.ocee.ldap.admin;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.tools.accounts.CmsGroupStateAction;

public class CmsLdapGroupAction extends CmsGroupStateAction {
    private final boolean f153x226a583a;
    private final boolean f154xbb78c9c2 = false;

    public CmsLdapGroupAction(String id, CmsObject cms, boolean ldap) {
        super(id, cms, true);
        this.f153x226a583a = ldap;
    }

    public CmsLdapGroupAction(String id, CmsObject cms, boolean direct, boolean ldap) {
        super(id, cms, direct);
        this.f153x226a583a = ldap;
    }

    public boolean isVisible() {
        if (getItem() == null || !isDirect()) {
            return super.isVisible();
        }
        if (this.f154xbb78c9c2 && !super.isVisible()) {
            return false;
        }
        try {
            CmsGroup group = getCms().readGroup(new CmsUUID(getItem().getId()));
            if (this.f153x226a583a) {
                return CmsLdapManager.hasLdapFlag(group.getFlags());
            }
            if (group.getFlags() < 65536) {
                return true;
            }
            return false;
        } catch (CmsException e) {
            return false;
        }
    }
}
