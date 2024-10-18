package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;

public class CmsUserTransferList extends org.opencms.workplace.tools.accounts.CmsUserTransferList {
    public CmsUserTransferList(CmsJspActionElement jsp) {
        super("lutl", jsp);
    }

    public CmsUserTransferList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected List getListItems() throws CmsException {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            return super.getListItems();
        }
        List ret = new ArrayList();
        List<CmsUser> users = getUsers();
        Set selUsers = new HashSet(CmsStringUtil.splitAsList(getParamUserid(), "|", true));
        for (CmsUser user : users) {
            if (!selUsers.contains(user.getId().toString())) {
                CmsListItem item = getList().newItem(user.getId().toString());
                setUserData(user, item);
                ret.add(item);
            }
        }
        return ret;
    }

    protected List getUsers() throws CmsException {
        return CmsPrincipal.filterCoreFlag(new ArrayList(OpenCms.getOrgUnitManager().getUsers(getCms(), "", true)), CmsLdapManager.LDAP_FLAG);
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void setColumns(CmsListMetadata metadata) {
        super.setColumns(metadata);
        CmsListColumnDefinition flagsCol = new CmsListColumnDefinition(CmsLdapUserAction.LIST_COLUMN_FLAGS);
        flagsCol.setName(Messages.get().container(Messages.GUI_LDAP_LIST_COLS_TYPE_0));
        flagsCol.setVisible(false);
        metadata.addColumn(flagsCol);
    }

    protected void setTransferAction(CmsListColumnDefinition transferCol) {
        I_CmsListDirectAction transferCoreAction = new CmsLdapUserAction("at", false);
        transferCoreAction.setName(Messages.get().container("GUI_USERS_TRANSFER_LIST_ACTION_TRANSFER_NAME_0"));
        transferCoreAction.setHelpText(Messages.get().container("GUI_USERS_TRANSFER_LIST_ACTION_TRANSFER_HELP_0"));
        transferCoreAction.setIconPath("tools/accounts/buttons/user.png");
        transferCol.addDirectAction(transferCoreAction);
        I_CmsListDirectAction transferLdapAction = new CmsLdapUserAction("at2", true);
        transferLdapAction.setName(Messages.get().container("GUI_USERS_TRANSFER_LIST_ACTION_TRANSFER_NAME_0"));
        transferLdapAction.setHelpText(Messages.get().container("GUI_USERS_TRANSFER_LIST_ACTION_TRANSFER_HELP_0"));
        transferLdapAction.setIconPath("tools/ocee-ldap/buttons/user.png");
        transferCol.addDirectAction(transferLdapAction);
        transferCol.setListItemComparator(new CmsListItemActionIconComparator());
    }

    protected void setUserData(CmsUser user, CmsListItem item) {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            super.setUserData(user, item);
        } else {
            item.set("ci", user.getName());
            item.set("cn", user.getFullName());
            item.set("cm", user.getEmail());
            item.set("cl", new Date(user.getLastlogin()));
        }
        item.set(CmsLdapUserAction.LIST_COLUMN_FLAGS, new Integer(user.getFlags()));
    }
}
