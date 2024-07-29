package org.opencms.ocee.ldap.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsCoreProvider;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.workplace.tools.accounts.A_CmsEditGroupDialog;

public class CmsEditGroupDialog extends A_CmsEditGroupDialog {
    public CmsEditGroupDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsEditGroupDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected String getListClass() {
        return CmsLdapGroupsList.class.getName();
    }

    protected String getListRootPath() {
        return "/ocee-ldap/orgunit/groups";
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected boolean isEditable(CmsGroup group) {
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(null);
        try {
            boolean z = !CmsLdapManager.hasLdapFlag(group.getFlags()) || CmsLdapManager.getInstance().isGroupEditable(dbc, group);
            dbc.clear();
            return z;
        } catch (Throwable th) {
            dbc.clear();
        }
        return false;
    }
}
