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
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapGroupDefinition;
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

public class CmsLdapSyncUsersList extends A_CmsListDialog {
    public static final String LIST_ACTION_ICON = "ai";
    public static final String LIST_ACTION_REMOVE = "ar";
    public static final String LIST_ACTION_UPDATE = "au";
    public static final String LIST_COLUMN_EMAIL = "ce";
    public static final String LIST_COLUMN_ICON = "ci";
    public static final String LIST_COLUMN_LOGIN = "cl";
    public static final String LIST_COLUMN_REMOVE = "cr";
    public static final String LIST_COLUMN_UPDATE = "cu";
    public static final String LIST_DEFACTION_UPDATE = "du";
    public static final String LIST_DETAIL_ADDRESS = "da";
    public static final String LIST_DETAIL_GROUPS = "dg";
    public static final String LIST_DETAIL_NAME = "dn";
    public static final String LIST_ID = "llsu";
    public static final String LIST_MACTION_REMOVE = "mr";
    public static final String LIST_MACTION_UPDATE = "mu";
    private String f165x226a583a;

    public CmsLdapSyncUsersList(CmsJspActionElement jsp) {
        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_NAME_0), "cl", CmsListOrderEnum.ORDER_ASCENDING, "cl");
    }

    public CmsLdapSyncUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void executeListMultiActions() throws CmsRuntimeException {
        if (getParamListAction().equals("mr")) {
            Map params = new HashMap();
            params.put(CmsLdapGroupDefinition.MF_USERID, getParamSelItems());
            params.put("action", "initial");
            try {
                getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container("ERR_DELETE_SELECTED_USERS_0"), e);
            }
        } else if (getParamListAction().equals("mu")) {
            CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(getJsp().getRequestContext());
            getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.TRUE);
            for (CmsListItem listItem : getSelectedItems()) {
                try {
                    CmsUser user = CmsLdapManager.getInstance().synchronizeUser(dbc, getCms().readUser(new CmsUUID(listItem.getId())), null);
                    getCms().writeUser(user);
                    CmsLdapManager.getInstance().updateGroupsForUser(dbc, getCms(), user);
                } catch (CmsException e2) {
                    throw new CmsRuntimeException(Messages.get().container(Messages.ERR_LDAP_UPDATE_SELECTED_USER_1, listItem.get("cl")), e2);
                } catch (Throwable th) {
                    dbc.clear();
                    if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                        getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                    } else {
                        getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                    }
                }
            }
            dbc.clear();
            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
            } else {
                getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    public void executeListSingleActions() throws CmsRuntimeException {
        if (getParamListAction().equals("ar")) {
            Map params = new HashMap();
            params.put(CmsLdapGroupDefinition.MF_USERID, getParamSelItems());
            params.put("action", "initial");
            try {
                getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container("ERR_DELETE_SELECTED_USERS_0"), e);
            }
        } else if (getParamListAction().equals("du") || getParamListAction().equals("au")) {
            CmsListItem listItem = getSelectedItem();
            CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(getJsp().getRequestContext());
            getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.TRUE);
            try {
                getCms().writeUser(CmsLdapManager.getInstance().synchronizeUser(dbc, getCms().readUser(new CmsUUID(listItem.getId())), null));
                dbc.clear();
                if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                    getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                } else {
                    getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                }
            } catch (CmsException e2) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_LDAP_UPDATE_SELECTED_USER_1, listItem.get("cl")), e2);
            } catch (Throwable th) {
                dbc.clear();
                if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                    getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                } else {
                    getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                }
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    public String getParamOufqn() {
        if (this.f165x226a583a == null) {
            return "";
        }
        return this.f165x226a583a;
    }

    public void setParamOufqn(String paramOufqn) {
        this.f165x226a583a = paramOufqn;
    }

    protected void fillDetails(String detailId) {
        List<CmsListItem> users = getList().getAllContent();
        getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.TRUE);
        for (CmsListItem item : users) {
            StringBuffer html = new StringBuffer(512);
            try {
                CmsUser user = getCms().readUser(new CmsUUID(item.getId()));
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
                } else if (detailId.equals("dg")) {
                    Iterator itGroups = getCms().getGroupsOfUser(user.getName(), false).iterator();
                    while (itGroups.hasNext()) {
                        html.append(((CmsGroup) itGroups.next()).getName());
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
                if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                    getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
                } else {
                    getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
                }
            }
            item.set(detailId, html.toString());
        }
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
            getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
        } else {
            getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
        }
    }

    protected List getListItems() throws CmsException {
        List ret = new ArrayList();
        for (CmsUser user : (List<CmsUser>)CmsPrincipal.filterFlag(new ArrayList(OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false)), CmsLdapManager.LDAP_FLAG)) {
            CmsListItem item = getList().newItem(user.getId().toString());
            item.set("cl", user.getSimpleName());
            item.set("ce", user.getEmail());
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
        CmsListColumnDefinition updateCol = new CmsListColumnDefinition("cu");
        updateCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_UPDATE_0));
        updateCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_UPDATE_HELP_0));
        updateCol.setWidth("20");
        updateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        updateCol.setSorteable(false);
        CmsListDirectAction updateAction = new CmsListDirectAction("au");
        updateAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_ACTION_UPDATE_NAME_0));
        updateAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_ACTION_UPDATE_HELP_0));
        updateAction.setIconPath("list/add.png");
        updateCol.addDirectAction(updateAction);
        metadata.addColumn(updateCol);
        CmsListColumnDefinition removeCol = new CmsListColumnDefinition("cr");
        removeCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_REMOVE_0));
        removeCol.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_REMOVE_HELP_0));
        removeCol.setWidth("20");
        removeCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        removeCol.setSorteable(false);
        CmsListDirectAction removeAction = new CmsListDirectAction("ar");
        removeAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_ACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_ACTION_REMOVE_HELP_0));
        removeAction.setIconPath("list/minus.png");
        removeCol.addDirectAction(removeAction);
        metadata.addColumn(removeCol);
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition("cl");
        loginCol.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_COLS_LOGIN_0));
        loginCol.setWidth("35%");
        CmsListDefaultAction updateDefAction = new CmsListDefaultAction("du");
        updateDefAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_DEFACTION_UPDATE_NAME_0));
        updateDefAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_DEFACTION_UPDATE_HELP_0));
        updateDefAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_DEFACTION_UPDATE_CONF_0));
        loginCol.addDefaultAction(updateDefAction);
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
    }

    protected void setMultiActions(CmsListMetadata metadata) {
        CmsListMultiAction updateMultiAction = new CmsListMultiAction("mu");
        updateMultiAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_MACTION_UPDATE_NAME_0));
        updateMultiAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_MACTION_UPDATE_HELP_0));
        updateMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_MACTION_UPDATE_CONF_0));
        updateMultiAction.setIconPath("list/multi_add.png");
        metadata.addMultiAction(updateMultiAction);
        CmsListMultiAction removeMultiAction = new CmsListMultiAction("mr");
        removeMultiAction.setName(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_MACTION_REMOVE_NAME_0));
        removeMultiAction.setHelpText(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_MACTION_REMOVE_HELP_0));
        removeMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAP_SYNCUSERS_LIST_MACTION_REMOVE_CONF_0));
        removeMultiAction.setIconPath("list/multi_minus.png");
        metadata.addMultiAction(removeMultiAction);
    }

    protected void validateParamaters() throws Exception {
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
    }
}
