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
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;

public class CmsGroupUsersList extends org.opencms.workplace.tools.accounts.CmsGroupUsersList {
    public CmsGroupUsersList(CmsJspActionElement jsp) {
        super(jsp, "lgul");
    }

    public CmsGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public CmsHtmlList getList() {
        CmsHtmlList list = super.getList();
        if (list != null) {
            CmsListColumnDefinition col = list.getMetadata().getColumnDefinition("cs");
            if (col != null) {
                for (I_CmsListDirectAction action : (List<I_CmsListDirectAction>)col.getDirectActions()) {
                    if (action instanceof CmsLdapGroupUserAction) {
                        ((CmsLdapGroupUserAction) action).setGroupName(getParamGroupname());
                    }
                }
            }
            CmsListColumnDefinition col2 = list.getMetadata().getColumnDefinition("cl");
            if (col2 != null) {
                for (CmsListDefaultAction action2 : (List<CmsListDefaultAction>)col2.getDefaultActions()) {
                    if (action2 instanceof CmsLdapGroupUserAction) {
                        ((CmsLdapGroupUserAction) action2).setGroupName(getParamGroupname());
                    }
                }
            }
        }
        return list;
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

    protected void setDefaultAction(CmsListColumnDefinition loginCol) {
        CmsListDefaultAction removeAction = new CmsLdapGroupUserAction("dr", false);
        removeAction.setName(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeAction.setHelpText(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0"));
        loginCol.addDefaultAction(removeAction);
        CmsListDefaultAction removeLdapAction = new CmsLdapGroupUserAction("dr2", true);
        removeLdapAction.setName(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_DEFACTION_REMOVE_HELP_0));
        loginCol.addDefaultAction(removeLdapAction);
        CmsListDefaultAction disabledLdapAction = new CmsLdapGroupUserAction("dr3", true);
        disabledLdapAction.setName(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0"));
        disabledLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_DEFACTION_REMOVE_DISABLED_0));
        disabledLdapAction.setEnabled(false);
        loginCol.addDefaultAction(disabledLdapAction);
        m_removeActionIds.addAll(loginCol.getDefaultActionIds());
    }

    protected void setIconAction(CmsListColumnDefinition iconCol) {
        I_CmsListDirectAction iconCoreAction = new CmsLdapUserAction("ai", false);
        iconCoreAction.setName(Messages.get().container("GUI_USERS_LIST_INGROUP_NAME_0"));
        iconCoreAction.setHelpText(Messages.get().container("GUI_USERS_LIST_INGROUP_HELP_0"));
        iconCoreAction.setIconPath("tools/accounts/buttons/user.png");
        iconCoreAction.setEnabled(false);
        iconCol.addDirectAction(iconCoreAction);
        I_CmsListDirectAction iconLdapAction = new CmsLdapUserAction("ai2", true);
        iconLdapAction.setName(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_INGROUP_NAME_0));
        iconLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_INGROUP_HELP_0));
        iconLdapAction.setIconPath("tools/ocee-ldap/buttons/user.png");
        iconLdapAction.setEnabled(false);
        iconCol.addDirectAction(iconLdapAction);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());
    }

    protected void setStateActionCol(CmsListMetadata metadata) {
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition("cs");
        stateCol.setName(Messages.get().container("GUI_USERS_LIST_COLS_STATE_0"));
        stateCol.setHelpText(Messages.get().container("GUI_USERS_LIST_COLS_STATE_HELP_0"));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        I_CmsListDirectAction removeCoreAction = new CmsLdapGroupUserAction("ar", false);
        removeCoreAction.setName(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeCoreAction.setHelpText(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0"));
        removeCoreAction.setIconPath("list/minus.png");
        stateCol.addDirectAction(removeCoreAction);
        I_CmsListDirectAction removeLdapAction = new CmsLdapGroupUserAction("ar2", true);
        removeLdapAction.setName(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_DEFACTION_REMOVE_HELP_0));
        removeLdapAction.setIconPath("list/minus.png");
        removeLdapAction.setEnabled(true);
        stateCol.addDirectAction(removeLdapAction);
        I_CmsListDirectAction disabledLdapAction = new CmsLdapGroupUserAction("ar3", true);
        disabledLdapAction.setName(Messages.get().container("GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0"));
        disabledLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_DEFACTION_REMOVE_DISABLED_0));
        disabledLdapAction.setIconPath("list/disabled.png");
        disabledLdapAction.setEnabled(false);
        stateCol.addDirectAction(disabledLdapAction);
        stateCol.setListItemComparator(new CmsListItemActionIconComparator());
        metadata.addColumn(stateCol);
        m_removeActionIds.addAll(stateCol.getDirectActionIds());
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
