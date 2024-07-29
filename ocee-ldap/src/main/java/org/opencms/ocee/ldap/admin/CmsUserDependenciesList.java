package org.opencms.ocee.ldap.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
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
