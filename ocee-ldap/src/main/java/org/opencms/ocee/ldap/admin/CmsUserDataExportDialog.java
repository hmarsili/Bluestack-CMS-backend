package org.opencms.ocee.ldap.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;

public class CmsUserDataExportDialog extends org.opencms.workplace.tools.accounts.CmsUserDataExportDialog {
    public CmsUserDataExportDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsUserDataExportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected String getDownloadPath() {
        return "/system/workplace/admin/ocee-ldap/imexport_user_data/dodownload.jsp";
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }
}
