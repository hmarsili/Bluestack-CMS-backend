package org.opencms.ocee.ldap.admin;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;

public class CmsUsersCsvDownloadDialog extends org.opencms.workplace.tools.accounts.CmsUsersCsvDownloadDialog {
    public CmsUsersCsvDownloadDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsUsersCsvDownloadDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected Map getData() {
        return (Map) ((Map) getSettings().getDialogObject()).get(CmsUserDataExportDialog.class.getName());
    }

    protected String getDownloadPath() {
        return "/system/workplace/admin/ocee-ldap/imexport_user_data/csvdownload.jsp";
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected boolean isExportable(CmsUser exportUser) {
        return true;
    }
}
