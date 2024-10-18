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
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;

public class CmsUserGroupsList extends org.opencms.workplace.tools.accounts.CmsUserGroupsList {
    public CmsUserGroupsList(CmsJspActionElement jsp) {
        super(jsp, "lugl");
    }

    public CmsUserGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public CmsHtmlList getList() {
        CmsHtmlList list = super.getList();
        if (list != null) {
            CmsListColumnDefinition col = list.getMetadata().getColumnDefinition("cs");
            if (col != null) {
                for (I_CmsListDirectAction action : (List<I_CmsListDirectAction>)col.getDirectActions()) {
                    if (action instanceof CmsLdapUserGroupAction) {
                        ((CmsLdapUserGroupAction) action).setUserName(getParamUsername());
                    }
                }
            }
            CmsListColumnDefinition col2 = list.getMetadata().getColumnDefinition("cn");
            if (col2 != null) {
                for (CmsListDefaultAction action2 : (List<CmsListDefaultAction>)col2.getDefaultActions()) {
                    if (action2 instanceof CmsLdapUserGroupAction) {
                        ((CmsLdapUserGroupAction) action2).setUserName(getParamUsername());
                    }
                }
            }
        }
        return list;
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

    protected void setDefaultAction(CmsListColumnDefinition nameCol) {
        CmsListDefaultAction removeAction = new CmsLdapUserGroupAction("dr", getCms(), true, false);
        removeAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_HELP_0"));
        nameCol.addDefaultAction(removeAction);
        CmsListDefaultAction removeIndirAction = new CmsLdapUserGroupAction("dri", getCms(), false, false);
        removeIndirAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeIndirAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_HELP_0"));
        removeIndirAction.setEnabled(false);
        nameCol.addDefaultAction(removeIndirAction);
        CmsListDefaultAction removeLdapAction = new CmsLdapUserGroupAction("drl", getCms(), true, true);
        removeLdapAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_DEFACTION_REMOVE_HELP_0));
        nameCol.addDefaultAction(removeLdapAction);
        CmsListDefaultAction disabledLdapAction = new CmsLdapUserGroupAction("drli", getCms(), true, true);
        disabledLdapAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        disabledLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_DEFACTION_REMOVE_DISABLED_0));
        disabledLdapAction.setEnabled(false);
        nameCol.addDefaultAction(disabledLdapAction);
        m_removeActionIds.addAll(nameCol.getDefaultActionIds());
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

    protected void setStateActionCol(CmsListMetadata metadata) {
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition("cs");
        stateCol.setName(Messages.get().container("GUI_USERS_LIST_COLS_STATE_0"));
        stateCol.setHelpText(Messages.get().container("GUI_USERS_LIST_COLS_STATE_HELP_0"));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        I_CmsListDirectAction removeCoreAction = new CmsLdapUserGroupAction("ar", getCms(), true, false);
        removeCoreAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeCoreAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_HELP_0"));
        removeCoreAction.setIconPath("list/minus.png");
        stateCol.addDirectAction(removeCoreAction);
        I_CmsListDirectAction removeIndirAction = new CmsLdapUserGroupAction("ari", getCms(), false, false);
        removeIndirAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeIndirAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_HELP_0"));
        removeIndirAction.setIconPath("list/disabled.png");
        removeIndirAction.setEnabled(false);
        stateCol.addDirectAction(removeIndirAction);
        I_CmsListDirectAction removeLdapAction = new CmsLdapUserGroupAction("arl", getCms(), true, true);
        removeLdapAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        removeLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_DEFACTION_REMOVE_HELP_0));
        removeLdapAction.setIconPath("list/minus.png");
        stateCol.addDirectAction(removeLdapAction);
        I_CmsListDirectAction disabledLdapAction = new CmsLdapUserGroupAction("arli", getCms(), true, true);
        disabledLdapAction.setName(Messages.get().container("GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0"));
        disabledLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_DEFACTION_REMOVE_DISABLED_0));
        disabledLdapAction.setIconPath("list/disabled.png");
        disabledLdapAction.setEnabled(false);
        stateCol.addDirectAction(disabledLdapAction);
        stateCol.setListItemComparator(new CmsListItemActionIconComparator());
        metadata.addColumn(stateCol);
        m_removeActionIds.addAll(stateCol.getDirectActionIds());
    }
}
