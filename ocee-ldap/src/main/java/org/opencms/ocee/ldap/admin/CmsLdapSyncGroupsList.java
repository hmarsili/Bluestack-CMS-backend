package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
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

public class CmsLdapSyncGroupsList extends A_CmsListDialog {
    public static final String LIST_ACTION_ICON = "ai";
    public static final String LIST_ACTION_REMOVE = "ar";
    public static final String LIST_ACTION_UPDATE = "au";
    public static final String LIST_COLUMN_ICON = "ci";
    public static final String LIST_COLUMN_NAME = "cn";
    public static final String LIST_COLUMN_REMOVE = "cr";
    public static final String LIST_COLUMN_UPDATE = "cu";
    public static final String LIST_DEFACTION_UPDATE = "dau";
    public static final String LIST_DETAIL_DESCRIPTION = "dd";
    public static final String LIST_DETAIL_USERS = "du";
    public static final String LIST_ID = "llsg";
    public static final String LIST_MACTION_REMOVE = "mr";
    public static final String LIST_MACTION_UPDATE = "mu";
    private String f164x226a583a;

    public CmsLdapSyncGroupsList(CmsJspActionElement jsp) {
        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_NAME_0), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
    }

    public CmsLdapSyncGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void executeListMultiActions() throws CmsRuntimeException {
        if (getParamListAction().equals("mr")) {
            Map params = new HashMap();
            params.put("groupid", getParamSelItems());
            params.put("action", "initial");
            try {
                getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container("ERR_DELETE_SELECTED_GROUPS_0"), e);
            }
        } else if (getParamListAction().equals("mu")) {
            for (CmsListItem listItem : getSelectedItems()) {
                m20x226a583a(listItem.getId(), (String) listItem.get("cn"));
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    public void executeListSingleActions() throws CmsRuntimeException {
        if (getParamListAction().equals("ar")) {
            Map params = new HashMap();
            params.put("groupid", getParamSelItems());
            params.put("action", "initial");
            try {
                getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container("ERR_DELETE_SELECTED_GROUPS_0"), e);
            }
        } else if (getParamListAction().equals(LIST_DEFACTION_UPDATE) || getParamListAction().equals("au")) {
            CmsListItem listItem = getSelectedItem();
            m20x226a583a(listItem.getId(), (String) listItem.get("cn"));
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    public String getParamOufqn() {
        if (this.f164x226a583a == null) {
            return "";
        }
        return this.f164x226a583a;
    }

    public void setParamOufqn(String paramOufqn) {
        this.f164x226a583a = paramOufqn;
    }

    protected void fillDetails(String detailId) {
        for (CmsListItem item : (List<CmsListItem>)getList().getAllContent()) {
            StringBuffer html = new StringBuffer(512);
            try {
                CmsGroup group = getCms().readGroup(new CmsUUID(item.getId()));
                if (detailId.equals("dd")) {
                    html.append(group.getDescription());
                    html.append("<br>\n");
                } else if (detailId.equals("du")) {
                    Iterator itUsers = getCms().getUsersOfGroup(group.getName()).iterator();
                    while (itUsers.hasNext()) {
                        html.append(((CmsUser) itUsers.next()).getName());
                        if (itUsers.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            item.set(detailId, html.toString());
        }
    }

    protected List getListItems() throws CmsException {
        List ret = new ArrayList();
        for (CmsGroup group : (List<CmsGroup>) CmsPrincipal.filterFlag(new ArrayList(OpenCms.getOrgUnitManager().getGroups(getCms(), getParamOufqn(), false)), CmsLdapManager.LDAP_FLAG)) {
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set("cn", group.getSimpleName());
            ret.add(item);
        }
        return ret;
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
        CmsListColumnDefinition updateCol = new CmsListColumnDefinition("cu");
        updateCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_UPDATE_0));
        updateCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_UPDATE_HELP_0));
        updateCol.setWidth("20");
        updateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        updateCol.setSorteable(false);
        CmsListDirectAction updateAction = new CmsListDirectAction("au");
        updateAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_ACTION_UPDATE_NAME_0));
        updateAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_ACTION_UPDATE_HELP_0));
        updateAction.setIconPath("list/add.png");
        updateCol.addDirectAction(updateAction);
        metadata.addColumn(updateCol);
        CmsListColumnDefinition removeCol = new CmsListColumnDefinition("cr");
        removeCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_REMOVE_0));
        removeCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_REMOVE_HELP_0));
        removeCol.setWidth("20");
        removeCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        removeCol.setSorteable(false);
        CmsListDirectAction removeAction = new CmsListDirectAction("ar");
        removeAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_ACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_ACTION_REMOVE_HELP_0));
        removeAction.setIconPath("list/minus.png");
        removeCol.addDirectAction(removeAction);
        metadata.addColumn(removeCol);
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
        nameCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_COLS_NAME_0));
        nameCol.setWidth("100%");
        CmsListDefaultAction updateDefAction = new CmsListDefaultAction(LIST_DEFACTION_UPDATE);
        updateDefAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_DEFACTION_UPDATE_NAME_0));
        updateDefAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_DEFACTION_UPDATE_HELP_0));
        updateDefAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_DEFACTION_UPDATE_CONF_0));
        nameCol.addDefaultAction(updateDefAction);
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
    }

    protected void setMultiActions(CmsListMetadata metadata) {
        CmsListMultiAction updateMultiAction = new CmsListMultiAction("mu");
        updateMultiAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_MACTION_UPDATE_NAME_0));
        updateMultiAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_MACTION_UPDATE_HELP_0));
        updateMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_MACTION_UPDATE_CONF_0));
        updateMultiAction.setIconPath("list/multi_add.png");
        metadata.addMultiAction(updateMultiAction);
        CmsListMultiAction removeMultiAction = new CmsListMultiAction("mr");
        removeMultiAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_MACTION_REMOVE_NAME_0));
        removeMultiAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_MACTION_REMOVE_HELP_0));
        removeMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_SYNCGROUPS_LIST_MACTION_REMOVE_CONF_0));
        removeMultiAction.setIconPath("list/multi_minus.png");
        metadata.addMultiAction(removeMultiAction);
    }

    protected void validateParamaters() throws Exception {
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
    }

    private void m20x226a583a(String id, String name) {
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(getJsp().getRequestContext());
        try {
            CmsGroup oldGroup = getCms().readGroup(new CmsUUID(id));
            CmsGroup group = CmsLdapManager.getInstance().lookupGroup(dbc, oldGroup.getOuFqn() + name);
            oldGroup.setName(group.getName());
            oldGroup.setDescription(group.getDescription());
            if (!CmsLdapManager.hasLdapFlag(oldGroup.getFlags())) {
                oldGroup.setFlags(oldGroup.getFlags() ^ CmsLdapManager.LDAP_FLAG);
            }
            getCms().writeGroup(oldGroup);
            CmsLdapManager.getInstance().updateUsersForGroup(dbc, getCms(), oldGroup);
            dbc.clear();
        } catch (CmsException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_LDAP_UPDATE_SELECTED_GROUP_1, name), e);
        } catch (Throwable th) {
            dbc.clear();
        }
    }
}
