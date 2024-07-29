package org.opencms.ocee.ldap.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;

public class CmsOrgUnitsSubList extends org.opencms.workplace.tools.accounts.CmsOrgUnitsSubList {
    public CmsOrgUnitsSubList(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsOrgUnitsSubList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void executeListSingleActions() throws IOException, ServletException {
        String ouFqn = getSelectedItem().get("cn").toString();
        Map params = new HashMap();
        params.put("oufqn", ouFqn.substring(1));
        params.put("action", "initial");
        if (getParamListAction().equals("ae")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/mgmt/edit", params);
        } else if (getParamListAction().equals("au")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/users", params);
        } else if (getParamListAction().equals("ag")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/groups", params);
        } else if (getParamListAction().equals("ad")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/mgmt/delete", params);
        } else if (getParamListAction().equals("do")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit", params);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }
}
