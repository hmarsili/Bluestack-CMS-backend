package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.I_CmsListDirectAction;

public class CmsNotUserGroupsList extends org.opencms.workplace.tools.accounts.CmsNotUserGroupsList {
    public CmsNotUserGroupsList(CmsJspActionElement jsp) {
        super(jsp, "lnugl");
    }

    public CmsNotUserGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected List getGroups(boolean withOtherOus) throws CmsException {
        List ret = new ArrayList(super.getGroups(withOtherOus));
        if (CmsLdapManager.getInstance().isLdapOnly()) {
            return CmsPrincipal.filterFlag(ret, CmsLdapManager.LDAP_FLAG);
        }
        return ret;
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void setIconAction(CmsListColumnDefinition iconCol) {
        I_CmsListDirectAction dirAction = new CmsLdapGroupAction("ai", getCms(), false);
        dirAction.setName(Messages.get().container("GUI_GROUPS_LIST_AVAILABLE_NAME_0"));
        dirAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_AVAILABLE_HELP_0"));
        dirAction.setIconPath("tools/accounts/buttons/group.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);
        I_CmsListDirectAction iconLdapAction = new CmsLdapGroupAction("ail", getCms(), true);
        iconLdapAction.setName(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_AVAILABLE_NAME_0));
        iconLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_AVAILABLE_HELP_0));
        iconLdapAction.setIconPath("tools/ocee-ldap/buttons/group.png");
        iconLdapAction.setEnabled(false);
        iconCol.addDirectAction(iconLdapAction);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());
    }
}
