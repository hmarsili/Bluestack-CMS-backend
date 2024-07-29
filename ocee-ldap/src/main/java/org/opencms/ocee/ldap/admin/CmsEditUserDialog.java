package org.opencms.ocee.ldap.admin;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.workplace.tools.accounts.A_CmsEditUserDialog;

public class CmsEditUserDialog extends A_CmsEditUserDialog {
    public CmsEditUserDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsEditUserDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected CmsUser createUser(String name, String pwd, String desc, Map info) throws CmsException {
        return getCms().createUser(name, pwd, desc, info);
    }

    protected String getListClass() {
        return CmsLdapUsersList.class.getName();
    }

    protected String getListRootPath() {
        return "/ocee-ldap/orgunit/users";
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected boolean isEditable(CmsUser user) {
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(null);
        try {
            boolean z = !CmsLdapManager.hasLdapFlag(user.getFlags()) || CmsLdapManager.getInstance().isUserEditable(dbc, user);
            dbc.clear();
            return z;
        } catch (Throwable th) {
            dbc.clear();
        }
        return false;
    }
    

    protected boolean isPwdChangeAllowed(CmsUser user) {
        return CmsLdapManager.getInstance().getConfiguration().isPwdEditable() || !CmsLdapManager.hasLdapFlag(user.getFlags());
    }

    protected void writeUser(CmsUser user) throws CmsException {
        getCms().writeUser(user);
    }
}
