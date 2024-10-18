package org.opencms.ocee.ldap.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;

public class CmsUserOverviewDialog extends org.opencms.workplace.tools.accounts.CmsUserOverviewDialog {
    public CmsUserOverviewDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsUserOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }
}
