package org.opencms.ocee.ldap.admin;

import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.CmsListDefaultAction;

public class CmsLdapGroupUserAction extends CmsListDefaultAction {
    private final boolean f156x226a583a;
    private String f157xbb78c9c2;

    public CmsLdapGroupUserAction(String id, boolean ldap) {
        super(id);
        this.f156x226a583a = ldap;
    }

    public boolean isVisible() {
        CmsDbContext dbc;
        boolean z = true;
        if (getItem() == null) {
            return super.isVisible();
        }
        try {
            getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.TRUE);
            CmsUser user = getWp().getCms().readUser(new CmsUUID(getItem().getId()));
            if (!this.f156x226a583a) {
                if (user.getFlags() >= 65536) {
                    z = false;
                }
                if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                    getWp().getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                    return z;
                }
                getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                return z;
            } else if (CmsLdapManager.hasLdapFlag(user.getFlags())) {
                CmsGroup group = getWp().getCms().readGroup(this.f157xbb78c9c2);
                if (CmsLdapManager.hasLdapFlag(group.getFlags())) {
                    if (group.getId().equals(CmsOceeManager.LDAP_GROUP_ID)) {
                        if (isEnabled()) {
                            z = false;
                        }
                        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                            getWp().getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                            return z;
                        }
                        getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                        return z;
                    }
                    dbc = CmsCoreProvider.getInstance().getNewDbContext(null);
                    if (CmsLdapManager.getInstance().lookupUserNames(dbc, group).contains(user.getName())) {
                        if (isEnabled()) {
                            z = false;
                        }
                        dbc.clear();
                        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                            getWp().getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                            return z;
                        }
                        getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                        return z;
                    }
                    dbc.clear();
                }
                z = isEnabled();
                if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                    getWp().getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                    return z;
                }
                getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                return z;
            } else {
                if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                    getWp().getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                } else {
                    getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                }
                return false;
            }
        } catch (CmsException e) {
            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                getWp().getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
            } else {
                getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
            }
            return false;
        } catch (Throwable th) {
            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                getWp().getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
            } else {
                getWp().getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
            }
        }
        return false;
    }

    public void setGroupName(String groupName) {
        this.f157xbb78c9c2 = groupName;
    }
}
