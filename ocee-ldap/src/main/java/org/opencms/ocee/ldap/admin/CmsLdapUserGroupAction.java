package org.opencms.ocee.ldap.admin;

import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.tools.accounts.CmsGroupStateAction;

public class CmsLdapUserGroupAction extends CmsGroupStateAction {
    private final boolean f184x226a583a;

    public CmsLdapUserGroupAction(String id, CmsObject cms, boolean direct, boolean ldap) {
        super(id, cms, direct);
        this.f184x226a583a = ldap;
    }

    public boolean isVisible() {
        CmsDbContext dbc;
        boolean z = true;
        if (getItem() == null || !isDirect()) {
            return super.isVisible();
        }
        if (!super.isVisible()) {
            return false;
        }
        try {
            CmsGroup group = getCms().readGroup(new CmsUUID(getItem().getId()));
            if (!this.f184x226a583a) {
                if (group.getFlags() >= 65536) {
                    z = false;
                }
                return z;
            } else if (!CmsLdapManager.hasLdapFlag(group.getFlags())) {
                return false;
            } else {
                getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.TRUE);
                try {
                    CmsUser user = getCms().readUser(getUserName());
                    if (CmsLdapManager.hasLdapFlag(user.getFlags())) {
                        if (group.getId().equals(CmsOceeManager.LDAP_GROUP_ID)) {
                            if (isEnabled()) {
                                z = false;
                            }
                            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                                getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                            } else {
                                getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                            }
                            return z;
                        }
                        dbc = CmsCoreProvider.getInstance().getNewDbContext(null);
                        if (CmsLdapManager.getInstance().lookupUserNames(dbc, group).contains(user.getName())) {
                            if (isEnabled()) {
                                z = false;
                            }
                            dbc.clear();
                            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                                getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                            } else {
                                getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                            }
                            return z;
                        }
                        dbc.clear();
                    }
                    if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                        getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                    } else {
                        getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                    }
                    return isEnabled();
                } catch (Throwable th) {
                    if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                        getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                    } else {
                        getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                    }
                }
            }
        } catch (CmsException e) {
            return false;
        }
        return false;
    }
}
