package org.opencms.ocee.ldap.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.tools.accounts.CmsDependencyIconActionType;
import org.opencms.workplace.tools.accounts.Messages;

public class CmsGroupDependenciesList extends org.opencms.workplace.tools.accounts.CmsGroupDependenciesList {
    public CmsGroupDependenciesList(CmsJspActionElement jsp) {
        super("lgdl", jsp);
    }

    public CmsGroupDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void setIconActions(CmsListColumnDefinition iconCol) {
        CmsListDirectAction resourceIconAction = new CmsLdapDependencyIconAction("ai", CmsDependencyIconActionType.RESOURCE, getCms());
        resourceIconAction.setName(Messages.get().container("GUI_GROUP_DEPENDENCIES_LIST_ACTION_RES_NAME_0"));
        resourceIconAction.setHelpText(Messages.get().container("GUI_GROUP_DEPENDENCIES_LIST_ACTION_RES_HELP_0"));
        resourceIconAction.setEnabled(false);
        iconCol.addDirectAction(resourceIconAction);
        CmsListDirectAction groupIconAction = new CmsLdapDependencyIconAction("ai", CmsDependencyIconActionType.GROUP, getCms());
        groupIconAction.setName(Messages.get().container("GUI_GROUP_DEPENDENCIES_LIST_ACTION_GRP_NAME_0"));
        groupIconAction.setHelpText(Messages.get().container("GUI_GROUP_DEPENDENCIES_LIST_ACTION_GRP_HELP_0"));
        groupIconAction.setEnabled(false);
        iconCol.addDirectAction(groupIconAction);
        CmsListDirectAction userIconAction = new CmsLdapDependencyIconAction("ai", CmsDependencyIconActionType.USER, getCms());
        userIconAction.setName(Messages.get().container("GUI_GROUP_DEPENDENCIES_LIST_ACTION_USR_NAME_0"));
        userIconAction.setHelpText(Messages.get().container("GUI_GROUP_DEPENDENCIES_LIST_ACTION_USR_HELP_0"));
        userIconAction.setEnabled(false);
        iconCol.addDirectAction(userIconAction);
    }
}
