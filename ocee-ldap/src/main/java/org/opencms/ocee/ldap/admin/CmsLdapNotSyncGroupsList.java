package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsLdapNotSyncGroupsList extends A_CmsListDialog {
    public static final String LIST_ACTION_ADD = "ad";
    public static final String LIST_ACTION_ICON = "ai";
    public static final String LIST_COLUMN_ADD = "ca";
    public static final String LIST_COLUMN_ICON = "ci";
    public static final String LIST_COLUMN_NAME = "cn";
    public static final String LIST_DEFACTION_ADD = "da";
    public static final String LIST_DETAIL_DESCRIPTION = "dd";
    public static final String LIST_DETAIL_LDAP = "dl";
    public static final String LIST_DETAIL_USERS = "du";
    public static final String LIST_ID = "llng";
    public static final String LIST_MACTION_ADD = "ma";
    private String f160x226a583a;

    public CmsLdapNotSyncGroupsList(CmsJspActionElement jsp) {
        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_NAME_0), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
    }

    public CmsLdapNotSyncGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public static Set getSystemGroups(CmsObject cms, String ouFqn) {
        List groups = new ArrayList();
        try {
            groups = CmsPrincipal.filterFlag(new ArrayList(OpenCms.getOrgUnitManager().getGroups(cms, ouFqn, true)), CmsLdapManager.LDAP_FLAG);
        } catch (CmsException e) {
        }
        Set systemGroups = new HashSet(groups.size());
        for (CmsGroup group : (List<CmsGroup>)groups) {
            systemGroups.add(group.getName());
        }
        return systemGroups;
    }

    public void executeListMultiActions() throws CmsRuntimeException {
        if (getParamListAction().equals("ma")) {
            for (CmsListItem listItem : getSelectedItems()) {
                CmsLdapManager.getInstance().addGroup(getCms(), listItem.getId());
                m15x226a583a(listItem);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    public void executeListSingleActions() throws CmsRuntimeException {
        if (getParamListAction().equals("da") || getParamListAction().equals("ad")) {
            CmsListItem listItem = getSelectedItem();
            CmsLdapManager.getInstance().addGroup(getCms(), listItem.getId());
            m15x226a583a(listItem);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    public CmsHtmlList getList() {
        CmsHtmlList list = super.getList();
        if (!CmsOceeManager.getInstance().checkCoreVersion("7.0.5") || list == null || (list instanceof CmsLdapServerList)) {
            return list;
        }
        CmsHtmlList list2 = new CmsLdapServerList(list, getCms(), getParamOufqn());
        setList(list2);
        listSave();
        return list2;
    }

    public String getParamOufqn() {
        if (this.f160x226a583a == null) {
            return "";
        }
        return this.f160x226a583a;
    }

    public void setParamOufqn(String paramOufqn) {
        this.f160x226a583a = paramOufqn;
    }

    protected void fillDetails(String detailId) {
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(getJsp().getRequestContext());
        try {
            for (CmsListItem item : (List<CmsListItem>)getList().getAllContent()) {
                String groupName = item.getId().toString();
                StringBuffer html = new StringBuffer(512);
                CmsGroup group = CmsLdapManager.getInstance().lookupGroup(dbc, groupName);
                if (detailId.equals("du")) {
                    Iterator itUsers = CmsLdapManager.getInstance().lookupUserNames(dbc, group).iterator();
                    while (itUsers.hasNext()) {
                        html.append(itUsers.next());
                        if (itUsers.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals("dl")) {
                    html.append(CmsLdapManager.getInstance().getDNforGroup(dbc, group.getName()));
                } else if (detailId.equals("dd")) {
                    html.append(group.getDescription());
                    html.append("<br>\n");
                }
                item.set(detailId, html.toString());
            }
            dbc.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            dbc.clear();
        }
    }

    protected List getListItems() throws CmsException {
        Set systemGroups = getSystemGroups(getCms(), getParamOufqn());
        List ret = new ArrayList();
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(getJsp().getRequestContext());
        try {
            for (String groupName : (List<String>)CmsLdapManager.getInstance().lookupGroupNames(dbc, getParamOufqn())) {
                if (!systemGroups.contains(groupName)) {
                    CmsListItem item = getList().newItem(groupName);
                    item.set("cn", CmsOrganizationalUnit.getSimpleName(groupName));
                    ret.add(item);
                }
            }
            return ret;
        } finally {
            dbc.clear();
        }
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void setColumns(CmsListMetadata metadata) {
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
        iconCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        CmsListDirectAction iconAction = new CmsListDirectAction("ai");
        iconAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_ACTION_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_ACTION_ICON_HELP_0));
        iconAction.setIconPath("tools/ocee-ldap/buttons/group.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        metadata.addColumn(iconCol);
        CmsListColumnDefinition addCol = new CmsListColumnDefinition("ca");
        addCol.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_COLS_ADD_0));
        addCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_COLS_ADD_HELP_0));
        addCol.setWidth("20");
        addCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        addCol.setSorteable(false);
        CmsListDirectAction addAction = new CmsListDirectAction("ad");
        addAction.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_ACTION_ADD_NAME_0));
        addAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_ACTION_ADD_HELP_0));
        addAction.setIconPath("list/add.png");
        addCol.addDirectAction(addAction);
        metadata.addColumn(addCol);
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
        nameCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_NAME_0));
        nameCol.setWidth("100%");
        CmsListDefaultAction addDefAction = new CmsListDefaultAction("da");
        addDefAction.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_DEFACTION_ADD_NAME_0));
        addDefAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_DEFACTION_ADD_HELP_0));
        nameCol.addDefaultAction(addDefAction);
        metadata.addColumn(nameCol);
    }

    protected void setIndependentActions(CmsListMetadata metadata) {
        CmsListItemDetails groupDescriptionDetails = new CmsListItemDetails("dd");
        groupDescriptionDetails.setAtColumn("cn");
        groupDescriptionDetails.setVisible(false);
        groupDescriptionDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_SHOW_DESC_NAME_0));
        groupDescriptionDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_SHOW_DESC_HELP_0));
        groupDescriptionDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_HIDE_DESC_NAME_0));
        groupDescriptionDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_HIDE_DESC_HELP_0));
        groupDescriptionDetails.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_DESC_NAME_0));
        groupDescriptionDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_DESC_NAME_0)));
        metadata.addItemDetails(groupDescriptionDetails);
        CmsListItemDetails groupUsersDetails = new CmsListItemDetails("du");
        groupUsersDetails.setAtColumn("cn");
        groupUsersDetails.setVisible(false);
        groupUsersDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_SHOW_USERS_NAME_0));
        groupUsersDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_SHOW_USERS_HELP_0));
        groupUsersDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_HIDE_USERS_NAME_0));
        groupUsersDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_HIDE_USERS_HELP_0));
        groupUsersDetails.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_USERS_NAME_0));
        groupUsersDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_USERS_NAME_0)));
        metadata.addItemDetails(groupUsersDetails);
        CmsListItemDetails groupLdapDetails = new CmsListItemDetails("dl");
        groupLdapDetails.setAtColumn("cn");
        groupLdapDetails.setVisible(false);
        groupLdapDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_SHOW_LDAP_NAME_0));
        groupLdapDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_SHOW_LDAP_HELP_0));
        groupLdapDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_HIDE_LDAP_NAME_0));
        groupLdapDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_HIDE_LDAP_HELP_0));
        groupLdapDetails.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_LDAP_NAME_0));
        groupLdapDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_DETAIL_LDAP_NAME_0)));
        metadata.addItemDetails(groupLdapDetails);
    }

    protected void setMultiActions(CmsListMetadata metadata) {
        CmsListMultiAction addMultiAction = new CmsListMultiAction("ma");
        addMultiAction.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_MACTION_ADD_NAME_0));
        addMultiAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_MACTION_ADD_HELP_0));
        addMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_NSYNCGROUPS_LIST_MACTION_ADD_CONF_0));
        addMultiAction.setIconPath("list/multi_add.png");
        metadata.addMultiAction(addMultiAction);
    }

    protected void validateParamaters() throws Exception {
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
    }

    private void m15x226a583a(CmsListItem listItem) {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            getList().removeItem(listItem.getId());
        }
    }
}
