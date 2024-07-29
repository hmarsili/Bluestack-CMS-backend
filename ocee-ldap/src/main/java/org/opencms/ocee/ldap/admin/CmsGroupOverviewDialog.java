package org.opencms.ocee.ldap.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;

public class CmsGroupOverviewDialog extends org.opencms.workplace.tools.accounts.CmsGroupOverviewDialog {
    public CmsGroupOverviewDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsGroupOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }
}
