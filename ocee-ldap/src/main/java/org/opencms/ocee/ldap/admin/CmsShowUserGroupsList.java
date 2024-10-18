package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.I_CmsListDirectAction;

public class CmsShowUserGroupsList extends org.opencms.workplace.tools.accounts.CmsShowUserGroupsList {
    public CmsShowUserGroupsList(CmsJspActionElement jsp) {
        super(jsp, "lsugl");
    }

    public CmsShowUserGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
        I_CmsListDirectAction dirAction = new CmsLdapGroupAction("aid", getCms(), true, false);
        dirAction.setName(Messages.get().container("GUI_GROUPS_LIST_DIRECT_NAME_0"));
        dirAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_DIRECT_HELP_0"));
        dirAction.setIconPath("tools/accounts/buttons/group.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);
        I_CmsListDirectAction indirAction = new CmsLdapGroupAction("aii", getCms(), false, false);
        indirAction.setName(Messages.get().container("GUI_GROUPS_LIST_INDIRECT_NAME_0"));
        indirAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_INDIRECT_HELP_0"));
        indirAction.setIconPath("tools/accounts/buttons/group_indirect.png");
        indirAction.setEnabled(false);
        iconCol.addDirectAction(indirAction);
        I_CmsListDirectAction iconLdapAction = new CmsLdapGroupAction("aidl", getCms(), true, true);
        iconLdapAction.setName(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_DIRECT_NAME_0));
        iconLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_DIRECT_HELP_0));
        iconLdapAction.setIconPath("tools/ocee-ldap/buttons/group.png");
        iconLdapAction.setEnabled(false);
        iconCol.addDirectAction(iconLdapAction);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());
    }
}
