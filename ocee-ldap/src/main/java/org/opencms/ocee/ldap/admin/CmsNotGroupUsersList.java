package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;

public class CmsNotGroupUsersList extends org.opencms.workplace.tools.accounts.CmsNotGroupUsersList {
    public CmsNotGroupUsersList(CmsJspActionElement jsp) {
        super(jsp, "lngul");
    }

    public CmsNotGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected List getListItems() throws CmsException {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            return super.getListItems();
        }
        List ret = new ArrayList();
        boolean withOtherOus = hasUsersInOtherOus() && getList().getMetadata().getItemDetailDefinition("doo") != null && getList().getMetadata().getItemDetailDefinition("doo").isVisible();
        for (CmsUser user : (List<CmsUser>)getUsers(withOtherOus)) {
            CmsListItem item = getList().newItem(user.getId().toString());
            setUserData(user, item);
            ret.add(item);
        }
        return ret;
    }

    protected List getUsers(boolean withOtherOus) throws CmsException {
        List ret = new ArrayList(super.getUsers(withOtherOus));
        if (CmsLdapManager.getInstance().isLdapOnly()) {
            return CmsPrincipal.filterFlag(ret, CmsLdapManager.LDAP_FLAG);
        }
        return ret;
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

    protected void setIconAction(CmsListColumnDefinition iconCol) {
        I_CmsListDirectAction iconCoreAction = new CmsLdapUserAction("ai", false);
        iconCoreAction.setName(Messages.get().container("GUI_USERS_LIST_AVAILABLE_NAME_0"));
        iconCoreAction.setHelpText(Messages.get().container("GUI_USERS_LIST_AVAILABLE_HELP_0"));
        iconCoreAction.setIconPath("tools/accounts/buttons/user.png");
        iconCoreAction.setEnabled(false);
        iconCol.addDirectAction(iconCoreAction);
        I_CmsListDirectAction iconLdapAction = new CmsLdapUserAction("ai2", true);
        iconLdapAction.setName(Messages.get().container("GUI_USERS_LIST_AVAILABLE_NAME_0"));
        iconLdapAction.setHelpText(Messages.get().container("GUI_USERS_LIST_AVAILABLE_HELP_0"));
        iconLdapAction.setIconPath("tools/ocee-ldap/buttons/user.png");
        iconLdapAction.setEnabled(false);
        iconCol.addDirectAction(iconLdapAction);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());
    }

    protected void setUserData(CmsUser user, CmsListItem item) {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            super.setUserData(user, item);
        } else {
            item.set("cl", user.getName());
            item.set("cn", user.getSimpleName());
            item.set("co", "/" + user.getOuFqn());
            item.set("cf", user.getFullName());
        }
        item.set(CmsLdapUserAction.LIST_COLUMN_FLAGS, new Integer(user.getFlags()));
    }
}
