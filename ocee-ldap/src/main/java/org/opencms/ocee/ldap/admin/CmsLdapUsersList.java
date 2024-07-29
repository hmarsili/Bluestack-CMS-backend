package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.list.I_CmsListFormatter;
import org.opencms.workplace.tools.accounts.A_CmsUsersList;

public class CmsLdapUsersList extends A_CmsUsersList {
    public static final String LIST_ID = "llu";
    private static final String f187x226a583a = "ct";

    class C00021 implements I_CmsListFormatter {
        final /* synthetic */ CmsLdapUsersList f186x226a583a;

        C00021(CmsLdapUsersList cmsLdapUsersList) {
            this.f186x226a583a = cmsLdapUsersList;
        }

        public String format(Object data, Locale locale) {
            if ((data instanceof Boolean) && ((Boolean) data).booleanValue()) {
                return this.f186x226a583a.key(Messages.GUI_LDAP_TYPE_LDAP_0);
            }
            return this.f186x226a583a.key(Messages.GUI_LDAP_TYPE_CORE_0);
        }
    }

    public CmsLdapUsersList(CmsJspActionElement jsp) {
        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LDAPUSERS_LIST_NAME_0));
    }

    public CmsLdapUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected String getGroupIcon() {
        return "tools/accounts/buttons/group.png";
    }

    protected List getListItems() throws CmsException {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            return super.getListItems();
        }
        List ret = new ArrayList();
        for (CmsUser user : (List<CmsUser>)getUsers()) {
            CmsListItem item = getList().newItem(user.getId().toString());
            setUserData(user, item);
            ret.add(item);
        }
        CmsListColumnDefinition colDef = getList().getMetadata().getColumnDefinition("cr");
        if (colDef == null || !CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            return ret;
        }
        colDef.setVisible(!OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).hasFlagWebuser());
        return ret;
    }

    protected List getUsers() throws CmsException {
        List ret = new ArrayList(OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false));
        if (CmsLdapManager.getInstance().isLdapOnly()) {
            return CmsPrincipal.filterFlag(ret, CmsLdapManager.LDAP_FLAG);
        }
        return CmsPrincipal.filterCoreFlag(ret, CmsLdapManager.LDAP_FLAG);
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected CmsUser readUser(String name) throws CmsException {
        getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.TRUE);
        try {
            CmsUser readUser = getCms().readUser(name);
            return readUser;
        } finally {
            if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                getCms().getRequestContext().removeAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC);
            } else {
                getCms().getRequestContext().setAttribute(CmsLdapManager.REQUEST_ATTR_SKIP_SYNC, Boolean.FALSE);
            }
        }
    }

    protected void setColumns(CmsListMetadata metadata) {
        super.setColumns(metadata);
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition("ct");
        typeCol.setName(Messages.get().container(Messages.GUI_LDAP_LIST_COLS_TYPE_0));
        typeCol.setWidth("5%");
        typeCol.setFormatter(new C00021(this));
        metadata.addColumn(typeCol);
        CmsListColumnDefinition flagsCol = new CmsListColumnDefinition(CmsLdapUserAction.LIST_COLUMN_FLAGS);
        flagsCol.setName(Messages.get().container(Messages.GUI_LDAP_LIST_COLS_TYPE_0));
        flagsCol.setVisible(false);
        metadata.addColumn(flagsCol);
    }

    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {
        I_CmsListDirectAction deleteCoreAction = new CmsLdapUserAction("ad", false);
        deleteCoreAction.setName(Messages.get().container("GUI_USERS_LIST_ACTION_DELETE_NAME_0"));
        deleteCoreAction.setHelpText(Messages.get().container("GUI_USERS_LIST_ACTION_DELETE_HELP_0"));
        deleteCoreAction.setIconPath("list/delete.png");
        deleteCol.addDirectAction(deleteCoreAction);
        I_CmsListDirectAction deleteLdapAction = new CmsLdapUserAction("ad2", true);
        deleteLdapAction.setName(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_ACTION_DELETE_NAME_0));
        deleteLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_ACTION_DELETE_HELP_0));
        deleteLdapAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_ACTION_DELETE_CONF_0));
        deleteLdapAction.setIconPath("list/delete.png");
        deleteCol.addDirectAction(deleteLdapAction);
    }

    protected void setEditAction(CmsListColumnDefinition editCol) {
        I_CmsListDirectAction editCoreAction = new CmsLdapUserAction("ae", false);
        editCoreAction.setName(Messages.get().container("GUI_USERS_LIST_ACTION_EDIT_NAME_0"));
        editCoreAction.setHelpText(Messages.get().container("GUI_USERS_LIST_ACTION_EDIT_HELP_0"));
        editCoreAction.setIconPath("tools/accounts/buttons/user.png");
        editCol.addDirectAction(editCoreAction);
        I_CmsListDirectAction editLdapAction = new CmsLdapUserAction("ae2", true);
        editLdapAction.setName(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_ACTION_EDIT_NAME_0));
        editLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPUSERS_LIST_ACTION_EDIT_HELP_0));
        editLdapAction.setIconPath("tools/ocee-ldap/buttons/user.png");
        editCol.addDirectAction(editLdapAction);
        editCol.setListItemComparator(new CmsListItemActionIconComparator());
    }

    protected void setUserData(CmsUser user, CmsListItem item) {
        if (CmsOceeManager.getInstance().checkCoreVersion("7.0.4")) {
            super.setUserData(user, item);
        } else {
            item.set("ci", user.getName());
            item.set("cdn", user.getSimpleName());
            item.set("cn", user.getFullName());
            item.set("cm", user.getEmail());
            item.set("cl", new Date(user.getLastlogin()));
        }
        item.set("ct", new Boolean(CmsLdapManager.hasLdapFlag(user.getFlags())));
        item.set(CmsLdapUserAction.LIST_COLUMN_FLAGS, new Integer(user.getFlags()));
    }
}
