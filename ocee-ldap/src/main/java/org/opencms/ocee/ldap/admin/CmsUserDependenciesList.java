package org.opencms.ocee.ldap.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;

public class CmsUserDependenciesList extends org.opencms.workplace.tools.accounts.CmsUserDependenciesList {
    public CmsUserDependenciesList(CmsJspActionElement jsp) {
        super("ludl", jsp);
    }

    public CmsUserDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }
}
