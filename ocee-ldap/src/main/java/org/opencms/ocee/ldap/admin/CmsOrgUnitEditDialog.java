package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.security.CmsOrgUnitManager;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.tools.accounts.CmsOrgUnitBean;

public class CmsOrgUnitEditDialog extends org.opencms.workplace.tools.accounts.CmsOrgUnitEditDialog {
    public CmsOrgUnitEditDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsOrgUnitEditDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    public void actionCommit() {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            super.actionCommit();
            return;
        }
        List errors = new ArrayList();
        try {
            if (isNewOrgUnit()) {
                String str;
                List resourceNames = CmsFileUtil.removeRedundancies(this.m_orgUnitBean.getResources());
                CmsOrgUnitManager orgUnitManager = OpenCms.getOrgUnitManager();
                CmsObject cms = getCms();
                String fqn = this.m_orgUnitBean.getFqn();
                String description = this.m_orgUnitBean.getDescription();
                if (resourceNames.isEmpty()) {
                    str = null;
                } else {
                    str = (String) resourceNames.get(0);
                }
                CmsOrganizationalUnit newOrgUnit = orgUnitManager.createOrganizationalUnit(cms, fqn, description, 0, str);
                if (!resourceNames.isEmpty()) {
                    resourceNames.remove(0);
                    for (String addResourceToOrgUnit : (List<String>)CmsFileUtil.removeRedundancies(resourceNames)) {
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(getCms(), newOrgUnit.getName(), addResourceToOrgUnit);
                    }
                }
            } else {
                CmsOrganizationalUnit orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), this.m_orgUnitBean.getFqn());
                orgunit.setDescription(this.m_orgUnitBean.getDescription());
                List<String> resourceNamesNew = CmsFileUtil.removeRedundancies(this.m_orgUnitBean.getResources());
                List<CmsResource> resourcesOld = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(getCms(), orgunit.getName());
                List<String> resourceNamesOld = new ArrayList();
                for (CmsResource resourceOld : resourcesOld) {
                    resourceNamesOld.add(getCms().getSitePath(resourceOld));
                }
                for (String resourceNameNew : resourceNamesNew) {
                    if (!resourceNamesOld.contains(resourceNameNew)) {
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(getCms(), orgunit.getName(), resourceNameNew);
                    }
                }
                for (String resourceNameOld : resourceNamesOld) {
                    if (!resourceNamesNew.contains(resourceNameOld)) {
                        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(getCms(), orgunit.getName(), resourceNameOld);
                    }
                }
                OpenCms.getOrgUnitManager().writeOrganizationalUnit(getCms(), orgunit);
            }
        } catch (Throwable t) {
            errors.add(t);
        }
        setCommitErrors(errors);
    }

    protected void initOrgUnitObject() {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            super.initOrgUnitObject();
            return;
        }
        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || "initial".equals(getParamAction())) {
                CmsOrganizationalUnit orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn());
                this.m_orgUnitBean = new CmsOrgUnitBean();
                if (isNewOrgUnit()) {
                    this.m_orgUnitBean.setParentOu(orgunit.getName());
                } else {
                    this.m_orgUnitBean.setName(orgunit.getName());
                    this.m_orgUnitBean.setDescription(orgunit.getDescription(getLocale()));
                    this.m_orgUnitBean.setParentOu(orgunit.getParentFqn());
                    this.m_orgUnitBean.setFqn(orgunit.getName());
                }
                setResourcesInBean(this.m_orgUnitBean, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(getCms(), orgunit.getName()));
                return;
            }
            this.m_orgUnitBean = (CmsOrgUnitBean) getDialogObject();
            this.m_orgUnitBean.getName();
        } catch (Exception e) {
            this.m_orgUnitBean = new CmsOrgUnitBean();
            this.m_orgUnitBean.setParentOu(getParamOufqn());
        }
    }

    protected boolean isNewOrgUnit() {
        return getCurrentToolPath().endsWith("/orgunit/mgmt/new");
    }

    protected void validateParamaters() throws Exception {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.3")) {
            super.validateParamaters();
            return;
        }
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        if (!isNewOrgUnit()) {
            OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
        }
    }
}
