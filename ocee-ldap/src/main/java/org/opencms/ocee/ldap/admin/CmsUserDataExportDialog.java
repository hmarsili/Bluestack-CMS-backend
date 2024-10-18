package org.opencms.ocee.ldap.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
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
