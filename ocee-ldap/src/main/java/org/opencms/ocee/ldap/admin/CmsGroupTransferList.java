package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.tools.accounts.Messages;

public class CmsGroupTransferList extends org.opencms.workplace.tools.accounts.CmsGroupTransferList {
    public CmsGroupTransferList(CmsJspActionElement jsp) {
        super("lgtl", jsp);
    }

    public CmsGroupTransferList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected List getGroups() throws CmsException {
        return CmsPrincipal.filterCoreFlag(new ArrayList(OpenCms.getOrgUnitManager().getGroups(getCms(), "", true)), CmsLdapManager.LDAP_FLAG);
    }

    protected void setTransferAction(CmsListColumnDefinition transferCol) {
        I_CmsListDirectAction transferCoreAction = new CmsLdapGroupAction("at", getCms(), false);
        transferCoreAction.setName(Messages.get().container("GUI_GROUPS_TRANSFER_LIST_ACTION_TRANSFER_NAME_0"));
        transferCoreAction.setHelpText(Messages.get().container("GUI_GROUPS_TRANSFER_LIST_ACTION_TRANSFER_HELP_0"));
        transferCoreAction.setIconPath("tools/accounts/buttons/group.png");
        transferCol.addDirectAction(transferCoreAction);
        I_CmsListDirectAction transferLdapAction = new CmsLdapGroupAction("at2", getCms(), true);
        transferLdapAction.setName(Messages.get().container("GUI_GROUPS_TRANSFER_LIST_ACTION_TRANSFER_NAME_0"));
        transferLdapAction.setHelpText(Messages.get().container("GUI_GROUPS_TRANSFER_LIST_ACTION_TRANSFER_HELP_0"));
        transferLdapAction.setIconPath("tools/ocee-ldap/buttons/group.png");
        transferCol.addDirectAction(transferLdapAction);
        transferCol.setListItemComparator(new CmsListItemActionIconComparator());
    }
}
