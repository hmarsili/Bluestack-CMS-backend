package org.opencms.ocee.ldap.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;

public class CmsOrgUnitsAdminList extends org.opencms.workplace.tools.accounts.CmsOrgUnitsAdminList {
    public CmsOrgUnitsAdminList(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsOrgUnitsAdminList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void forwardToSingleAdminOU() throws ServletException, IOException, CmsException {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            super.forwardToSingleAdminOU();
            return;
        }
        List orgUnits = OpenCms.getRoleManager().getOrgUnitsForRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(""), true);
        if (orgUnits.isEmpty()) {
            OpenCms.getWorkplaceManager().getToolManager().jspForwardTool(this, "/", null);
            return;
        }
        Map params = new HashMap();
        params.put("oufqn", ((CmsOrganizationalUnit) orgUnits.get(0)).getName());
        params.put("action", "initial");
        OpenCms.getWorkplaceManager().getToolManager().jspForwardTool(this, getForwardToolPath(), params);
    }

    protected String getForwardToolPath() {
        return "/ocee-ldap/orgunit";
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }
}
