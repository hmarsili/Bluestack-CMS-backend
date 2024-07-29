package org.opencms.ocee.ldap.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListFormatter;

public class CmsGroupsAllOrgUnitsList extends org.opencms.workplace.tools.accounts.CmsGroupsAllOrgUnitsList {
    private static final String f152x226a583a = "ct";

    class C00001 implements I_CmsListFormatter {
        final /* synthetic */ CmsGroupsAllOrgUnitsList f151x226a583a;

        C00001(CmsGroupsAllOrgUnitsList cmsGroupsAllOrgUnitsList) {
            this.f151x226a583a = cmsGroupsAllOrgUnitsList;
        }

        public String format(Object data, Locale locale) {
            if ((data instanceof Boolean) && ((Boolean) data).booleanValue()) {
                return this.f151x226a583a.key(Messages.GUI_LDAP_TYPE_LDAP_0);
            }
            return this.f151x226a583a.key(Messages.GUI_LDAP_TYPE_CORE_0);
        }
    }

    public CmsGroupsAllOrgUnitsList(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsGroupsAllOrgUnitsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void executeListSingleActions() throws IOException, ServletException {
        String groupId = getSelectedItem().getId();
        String groupName = "";
        try {
            groupName = getCms().readGroup(new CmsUUID(groupId)).getName();
        } catch (CmsException e) {
        }
        Map params = new HashMap();
        params.put("groupid", groupId);
        params.put(CmsLdapGroupSelectDialog.PARAM_GROUPNAME, groupName);
        params.put("oufqn", getSelectedItem().get("co").toString().substring(1));
        params.put("action", "initial");
        if (getParamListAction().equals("ao") || getParamListAction().equals("ae") || getParamListAction().equals("ae2")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/groups/edit", params);
        } else if (getParamListAction().equals("de") || getParamListAction().equals("de2")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/groups/edit", params);
        } else {
            super.executeListSingleActions();
        }
        listSave();
    }

    protected List getGroups() throws CmsException {
        List ret = new ArrayList(OpenCms.getRoleManager().getManageableGroups(getCms(), "", true));
        if (CmsLdapManager.getInstance().isLdapOnly()) {
            return CmsPrincipal.filterFlag(ret, CmsLdapManager.LDAP_FLAG);
        }
        return CmsPrincipal.filterCoreFlag(ret, CmsLdapManager.LDAP_FLAG);
    }

    protected List getListItems() throws CmsException {
        List<CmsListItem> listItems = super.getListItems();
        for (CmsListItem item : listItems) {
            item.set("ct", new Boolean(CmsLdapManager.hasLdapFlag(getCms().readGroup(new CmsUUID(item.getId())).getFlags())));
        }
        return listItems;
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void setColumns(CmsListMetadata metadata) {
        super.setColumns(metadata);
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition("ct");
        typeCol.setName(Messages.get().container(Messages.GUI_LDAP_LIST_COLS_TYPE_0));
        typeCol.setWidth("5%");
        typeCol.setFormatter(new C00001(this));
        metadata.addColumn(typeCol);
    }

    protected void setEditAction(CmsListColumnDefinition editCol) {
        CmsListDirectAction editCoreAction = new CmsLdapGroupAction("ae", getCms(), false);
        editCoreAction.setName(Messages.get().container("GUI_GROUPS_LIST_ACTION_EDIT_NAME_0"));
        editCoreAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_ACTION_EDIT_HELP_0"));
        editCoreAction.setIconPath("tools/accounts/buttons/group.png");
        editCol.addDirectAction(editCoreAction);
        CmsListDirectAction editLdapAction = new CmsLdapGroupAction("ae2", getCms(), true);
        editLdapAction.setName(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_ACTION_EDIT_NAME_0));
        editLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_ACTION_EDIT_HELP_0));
        editLdapAction.setIconPath("tools/ocee-ldap/buttons/group.png");
        editCol.addDirectAction(editLdapAction);
        editCol.setListItemComparator(new CmsListItemActionIconComparator());
    }
}
