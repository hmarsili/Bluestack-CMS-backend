package org.opencms.ocee.ldap.admin;

import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapConfiguration;
import org.opencms.ocee.ldap.CmsLdapGroupDefinition;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceUserInfoManager;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.tools.accounts.CmsAccountsToolHandler;

public class CmsLdapToolHandler extends CmsAccountsToolHandler {
    private static final String f166xf77b04e9 = "account_managers.jsp";
    private static final String f167x226a583a = "group_users.jsp";
    private static final String f168x9d2b144b = "unit_overview.jsp";
    private static final String f169xbb78c9c2 = "group_users.jsp";
    private static final String f170x77896254 = "unit_parent.jsp";
    private static final String f171x350cdc9c = "role_users.jsp";
    private static final String f172x8b47f845 = "unit_edit.jsp";
    private static final String f173x59c4920d = "unit_new.jsp";
    private static final String f174x587bb668 = "imexport_user_data/export_csv.jsp";
    private static final String f175xe3a92f9d = "user_assign.jsp";
    private static final Log f176x35b1a7df = CmsLog.getLog(CmsLdapToolHandler.class);
    private static final String f177xc79e744a = "user_role.jsp";
    private static final String f178x94ebafec = "user_allinfo.jsp";
    private static final String f179x5ae05e43 = "user_switch.jsp";
    private static final String f180xdc6db74 = "none";
    private static final String f181x27afe517 = "roles_list.jsp";
    private static final String f182xdb267f24 = "unit_delete.jsp";

    public String getDisabledHelpText() {
        if (CmsLdapManager.getInstance() == null) {
            return CmsOceeManager.getInstance().getDefaultHelpText();
        }
        if (getLink().equals(getPath(f174x587bb668)) && !CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            return "${key.GUI_LDAP_DISABLED_NO_COMP_0}";
        }
        if (CmsLdapManager.getInstance().isInitialized()) {
            return super.getDisabledHelpText();
        }
        return "${key.GUI_LDAP_DISABLED_NO_INIT_0}";
    }

    public boolean isEnabled(CmsObject cms) {
        if (CmsLdapManager.getInstance() == null || !CmsLdapManager.getInstance().isInitialized()) {
            return false;
        }
        return super.isEnabled(cms);
    }

    protected String getPath(String jspName) {
        return "/system/workplace/admin/ocee-ldap/" + jspName;
    }

    protected String getVisibilityFlag() {
        return CmsLdapConfiguration.C_LDAP_UPDATE_GROUP_ALL;
    }

    public boolean isEnabled(CmsWorkplace wp) {
        boolean z = true;
        if (CmsLdapManager.getInstance() == null || !CmsLdapManager.getInstance().isInitialized()) {
            return false;
        }
        if (getLink().equals(getPath(f174x587bb668)) && !CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            return false;
        }
        if (getLink().equals(getPath("group_users.jsp"))) {
            try {
                if (wp.getCms().readGroup(new CmsUUID(CmsRequestUtil.getNotEmptyDecodedParameter(wp.getJsp().getRequest(), "groupid"))).isVirtual()) {
                    z = false;
                }
                return z;
            } catch (Exception e) {
                return false;
            }
        }
        if (!getLink().equals(f175xe3a92f9d)) {
            wp.getJsp().getRequest().getSession().removeAttribute("orgunit_users");
            wp.getJsp().getRequest().getSession().removeAttribute("not_orgunit_users");
        }
        if (getLink().equals(f182xdb267f24)) {
            String ouFqn = CmsRequestUtil.getNotEmptyDecodedParameter(wp.getJsp().getRequest(), "oufqn");
            if (ouFqn == null) {
                ouFqn = wp.getCms().getRequestContext().getOuFqn();
            }
            try {
                if (OpenCms.getOrgUnitManager().getUsers(wp.getCms(), ouFqn, true).size() > 0) {
                    return false;
                }
                if (OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true).size() > 0) {
                    for (CmsGroup group : (List<CmsGroup>)OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true)) {
                        if (!OpenCms.getDefaultUsers().isDefaultGroup(group.getName())) {
                            return false;
                        }
                    }
                }
                if (OpenCms.getOrgUnitManager().getOrganizationalUnits(wp.getCms(), ouFqn, true).size() > 0) {
                    return false;
                }
            } catch (CmsException e2) {
            }
        }
        if (getLink().equals(getPath(f171x350cdc9c))) {
            if (!OpenCms.getRoleManager().hasRole(wp.getCms(), CmsRole.valueOfGroupName(CmsRequestUtil.getNotEmptyDecodedParameter(wp.getJsp().getRequest(), "role")))) {
                return false;
            }
        }
        return true;
    }

    public boolean isVisible(CmsWorkplace wp) {
        boolean z = true;
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4") && getVisibilityFlag().equals("none")) {
            return false;
        }
        if (getLink().equals(getPath(f178x94ebafec))) {
            CmsWorkplaceUserInfoManager manager = OpenCms.getWorkplaceManager().getUserInfoManager();
            if (manager == null || manager.getBlocks() == null || manager.getBlocks().isEmpty()) {
                return false;
            }
        }
        CmsObject cms = wp.getCms();
        if (!OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER)) {
            return false;
        }
        String ouFqn = CmsRequestUtil.getNotEmptyDecodedParameter(wp.getJsp().getRequest(), "oufqn");
        if (ouFqn == null) {
            ouFqn = cms.getRequestContext().getOuFqn();
        }
        String parentOu = CmsOrganizationalUnit.getParentFqn(ouFqn);
        boolean m_webuserOu = false;
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            try {
                m_webuserOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(wp.getCms(), ouFqn).hasFlagWebuser();
            } catch (CmsException e) {
                if (f176x35b1a7df.isErrorEnabled()) {
                    f176x35b1a7df.error(e.getLocalizedMessage(), e);
                }
            }
        }
        if (getLink().equals(getPath(f168x9d2b144b))) {
            if (parentOu == null) {
                return true;
            }
            if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(parentOu))) {
                z = false;
            }
            return z;
        } else if (getLink().equals(getPath(f172x8b47f845))) {
            if (parentOu == null) {
                return false;
            }
            if (!(OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR) && OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(parentOu)))) {
                z = false;
            }
            return z;
        } else if (getLink().equals(getPath(f173x59c4920d))) {
            if (m_webuserOu) {
                return false;
            }
            return OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR);
        } else if (getLink().equals(getPath(f170x77896254))) {
            if (parentOu != null) {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(parentOu));
            }
            return false;
        } else if (getLink().equals(getPath(f182xdb267f24))) {
            if (parentOu == null) {
                return false;
            }
            if (!(OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR) && OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(parentOu)))) {
                z = false;
            }
            return z;
        } else if (getLink().equals(getPath(f175xe3a92f9d))) {
            try {
                if (OpenCms.getRoleManager().getOrgUnitsForRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(""), true).size() == 1) {
                    return false;
                }
                if (m_webuserOu) {
                    z = false;
                }
                return z;
            } catch (CmsException e2) {
                if (f176x35b1a7df.isErrorEnabled()) {
                    f176x35b1a7df.error(e2.getLocalizedMessage(), e2);
                }
            }
        } else if (getLink().equals(getPath(f181x27afe517))) {
            if (m_webuserOu) {
                z = false;
            }
            return z;
        } else if (getLink().equals(getPath(f179x5ae05e43)) || getLink().equals(getPath(f177xc79e744a)) || getLink().equals(getPath("group_users.jsp"))) {
            String userId = CmsRequestUtil.getNotEmptyDecodedParameter(wp.getJsp().getRequest(), CmsLdapGroupDefinition.MF_USERID);
            if (userId == null) {
                return false;
            }
            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
                try {
                    wp.getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.TRUE);
                    if (wp.getCms().readUser(new CmsUUID(userId)).isWebuser()) {
                        z = false;
                    }
                    wp.getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                    return z;
                } catch (Exception e3) {
                    if (f176x35b1a7df.isErrorEnabled()) {
                        f176x35b1a7df.error(e3.getLocalizedMessage(), e3);
                    }
                    wp.getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                } catch (Throwable th) {
                    wp.getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                }
            }
            return true;
        } else {
            if (getLink().equals(getPath(f166xf77b04e9))) {
                return m_webuserOu;
            }
            return true;
        }
        return false;
    }
}
