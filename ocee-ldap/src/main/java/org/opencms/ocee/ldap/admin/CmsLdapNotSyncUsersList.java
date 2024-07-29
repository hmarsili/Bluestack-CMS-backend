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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
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

public class CmsLdapNotSyncUsersList extends A_CmsListDialog {
    public static final String LIST_ACTION_ADD = "ad";
    public static final String LIST_ACTION_ICON = "ai";
    public static final String LIST_COLUMN_ADD = "ca";
    public static final String LIST_COLUMN_EMAIL = "ce";
    public static final String LIST_COLUMN_ICON = "ci";
    public static final String LIST_COLUMN_LOGIN = "cl";
    public static final String LIST_DEFACTION_ADD = "daa";
    public static final String LIST_DETAIL_ADDRESS = "da";
    public static final String LIST_DETAIL_GROUPS = "dg";
    public static final String LIST_DETAIL_LDAP = "dl";
    public static final String LIST_DETAIL_NAME = "dn";
    public static final String LIST_ID = "llnu";
    public static final String LIST_MACTION_ADD = "ma";
    private String f161x226a583a;

    public CmsLdapNotSyncUsersList(CmsJspActionElement jsp) {
        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_NAME_0), "cl", CmsListOrderEnum.ORDER_ASCENDING, "cl");
    }

    public CmsLdapNotSyncUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public static Set getSystemUsers(CmsObject cms, String ouFqn) {
        List users = new ArrayList();
        try {
            users = CmsPrincipal.filterFlag(new ArrayList(OpenCms.getOrgUnitManager().getUsers(cms, ouFqn, false)), CmsLdapManager.LDAP_FLAG);
        } catch (CmsException e) {
        }
        Set systemUsers = new HashSet(users.size());
        for (CmsUser user : (List<CmsUser>)users) {
            systemUsers.add(user.getName());
        }
        return systemUsers;
    }

    public void executeListMultiActions() throws CmsRuntimeException {
        if (getParamListAction().equals("ma")) {
            for (CmsListItem listItem : getSelectedItems()) {
                CmsLdapManager.getInstance().addUser(getCms(), listItem.getId());
                m16x226a583a(listItem);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    public void executeListSingleActions() throws CmsRuntimeException {
        if (getParamListAction().equals(LIST_DEFACTION_ADD) || getParamListAction().equals("ad")) {
            CmsListItem listItem = getSelectedItem();
            CmsLdapManager.getInstance().addUser(getCms(), listItem.getId());
            m16x226a583a(listItem);
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
        if (this.f161x226a583a == null) {
            return "";
        }
        return this.f161x226a583a;
    }

    public void setParamOufqn(String paramOufqn) {
        this.f161x226a583a = paramOufqn;
    }

    protected void fillDetails(String detailId) {
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(getJsp().getRequestContext());
        for (CmsListItem item : (List<CmsListItem>)getList().getAllContent()) {
            String userName = item.getId();
            StringBuffer html = new StringBuffer(512);
            try {
                CmsUser user = CmsLdapManager.getInstance().lookupUser(dbc, userName, null);
                if (detailId.equals("da")) {
                    html.append(user.getAddress());
                    if (user.getCity() != null) {
                        html.append("<br>");
                        if (user.getZipcode() != null) {
                            html.append(user.getZipcode());
                            html.append(" ");
                        }
                        html.append(user.getCity());
                    }
                    if (user.getCountry() != null) {
                        html.append("<br>");
                        html.append(user.getCountry());
                    }
                } else if (detailId.equals("dl")) {
                    html.append(user.getAdditionalInfo("dn"));
                } else if (detailId.equals("dg")) {
                    Iterator itGroups = CmsLdapManager.getInstance().lookupGroupNames(dbc, user).iterator();
                    while (itGroups.hasNext()) {
                        html.append(itGroups.next());
                        if (itGroups.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals("dn")) {
                    html.append(user.getFullName());
                    html.append("<br>\n");
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable th) {
                dbc.clear();
            }
            item.set(detailId, html.toString());
        }
        dbc.clear();
    }

    protected List getListItems() throws CmsException {
        Set systemUsers = getSystemUsers(getCms(), getParamOufqn());
        List ret = new ArrayList();
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(getJsp().getRequestContext());
        try {
            for (CmsUser user : (List<CmsUser>)CmsLdapManager.getInstance().lookupUsers(dbc, getParamOufqn())) {
                if (!systemUsers.contains(user.getName())) {
                    CmsListItem item = getList().newItem(user.getName());
                    item.set("cl", user.getSimpleName());
                    item.set("ce", user.getEmail());
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
        iconCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        CmsListDirectAction iconAction = new CmsListDirectAction("ai");
        iconAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_ACTION_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_ACTION_ICON_HELP_0));
        iconAction.setIconPath("tools/ocee-ldap/buttons/user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        metadata.addColumn(iconCol);
        CmsListColumnDefinition addCol = new CmsListColumnDefinition("ca");
        addCol.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_COLS_ADD_0));
        addCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_COLS_ADD_HELP_0));
        addCol.setWidth("20");
        addCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        addCol.setSorteable(false);
        CmsListDirectAction addAction = new CmsListDirectAction("ad");
        addAction.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_ACTION_ADD_NAME_0));
        addAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_ACTION_ADD_HELP_0));
        addAction.setIconPath("list/add.png");
        addCol.addDirectAction(addAction);
        metadata.addColumn(addCol);
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition("cl");
        loginCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_LOGIN_0));
        loginCol.setWidth("35%");
        CmsListDefaultAction addDefAction = new CmsListDefaultAction(LIST_DEFACTION_ADD);
        addDefAction.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_DEFACTION_ADD_NAME_0));
        addDefAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_DEFACTION_ADD_HELP_0));
        loginCol.addDefaultAction(addDefAction);
        metadata.addColumn(loginCol);
        CmsListColumnDefinition emailCol = new CmsListColumnDefinition("ce");
        emailCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_EMAIL_0));
        emailCol.setWidth("65%");
        metadata.addColumn(emailCol);
    }

    protected void setIndependentActions(CmsListMetadata metadata) {
        CmsListItemDetails userNameDetails = new CmsListItemDetails("dn");
        userNameDetails.setAtColumn("cl");
        userNameDetails.setVisible(false);
        userNameDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_NAME_NAME_0));
        userNameDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_NAME_HELP_0));
        userNameDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_NAME_NAME_0));
        userNameDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_NAME_HELP_0));
        userNameDetails.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_NAME_NAME_0));
        userNameDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_NAME_NAME_0)));
        metadata.addItemDetails(userNameDetails);
        CmsListItemDetails userAddressDetails = new CmsListItemDetails("da");
        userAddressDetails.setAtColumn("cl");
        userAddressDetails.setVisible(false);
        userAddressDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_ADDRESS_NAME_0));
        userAddressDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_ADDRESS_HELP_0));
        userAddressDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_ADDRESS_NAME_0));
        userAddressDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_ADDRESS_HELP_0));
        userAddressDetails.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_ADDRESS_NAME_0));
        userAddressDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_ADDRESS_NAME_0)));
        metadata.addItemDetails(userAddressDetails);
        CmsListItemDetails userGroupsDetails = new CmsListItemDetails("dg");
        userGroupsDetails.setAtColumn("cl");
        userGroupsDetails.setVisible(false);
        userGroupsDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_GROUPS_NAME_0));
        userGroupsDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_GROUPS_HELP_0));
        userGroupsDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_GROUPS_NAME_0));
        userGroupsDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_GROUPS_HELP_0));
        userGroupsDetails.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_GROUPS_NAME_0));
        userGroupsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_GROUPS_NAME_0)));
        metadata.addItemDetails(userGroupsDetails);
        CmsListItemDetails userLdapDetails = new CmsListItemDetails("dl");
        userLdapDetails.setAtColumn("cl");
        userLdapDetails.setVisible(false);
        userLdapDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_LDAP_NAME_0));
        userLdapDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_SHOW_LDAP_HELP_0));
        userLdapDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_LDAP_NAME_0));
        userLdapDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_HIDE_LDAP_HELP_0));
        userLdapDetails.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_LDAP_NAME_0));
        userLdapDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_DETAIL_LDAP_NAME_0)));
        metadata.addItemDetails(userLdapDetails);
    }

    protected void setMultiActions(CmsListMetadata metadata) {
        CmsListMultiAction addMultiAction = new CmsListMultiAction("ma");
        addMultiAction.setName(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_MACTION_ADD_NAME_0));
        addMultiAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_MACTION_ADD_HELP_0));
        addMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_NSYNCUSERS_LIST_MACTION_ADD_CONF_0));
        addMultiAction.setIconPath("list/multi_add.png");
        metadata.addMultiAction(addMultiAction);
    }

    protected void validateParamaters() throws Exception {
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
    }

    private void m16x226a583a(CmsListItem listItem) {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            getList().removeItem(listItem.getId());
        }
    }
}
