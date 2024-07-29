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
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapGroupDefinition;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.list.I_CmsListFormatter;

public class CmsUsersAllOrgUnitsList extends org.opencms.workplace.tools.accounts.CmsUsersAllOrgUnitsList {
    private static final String f189x226a583a = "ct";

    class C00031 implements I_CmsListFormatter {
        final /* synthetic */ CmsUsersAllOrgUnitsList f188x226a583a;

        C00031(CmsUsersAllOrgUnitsList cmsUsersAllOrgUnitsList) {
            this.f188x226a583a = cmsUsersAllOrgUnitsList;
        }

        public String format(Object data, Locale locale) {
            if ((data instanceof Boolean) && ((Boolean) data).booleanValue()) {
                return this.f188x226a583a.key(Messages.GUI_LDAP_TYPE_LDAP_0);
            }
            return this.f188x226a583a.key(Messages.GUI_LDAP_TYPE_CORE_0);
        }
    }

    public CmsUsersAllOrgUnitsList(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsUsersAllOrgUnitsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void executeListSingleActions() throws IOException, ServletException {
        String userId = getSelectedItem().getId();
        Map params = new HashMap();
        params.put(CmsLdapGroupDefinition.MF_USERID, userId);
        params.put("oufqn", getSelectedItem().get("co").toString().substring(1));
        params.put("action", "initial");
        if (getParamListAction().equals("ao") || getParamListAction().equals("ae") || getParamListAction().equals("ae2")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/users/edit", params);
        } else if (getParamListAction().equals("de") || getParamListAction().equals("de2")) {
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/users/edit", params);
        } else {
            super.executeListSingleActions();
        }
        listSave();
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
        return ret;
    }

    protected List getUsers() throws CmsException {
        List ret = new ArrayList(OpenCms.getRoleManager().getManageableUsers(getCms(), "", true));
        if (CmsLdapManager.getInstance().isLdapOnly()) {
            return CmsPrincipal.filterFlag(ret, CmsLdapManager.LDAP_FLAG);
        }
        return CmsPrincipal.filterCoreFlag(ret, CmsLdapManager.LDAP_FLAG);
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
        typeCol.setFormatter(new C00031(this));
        metadata.addColumn(typeCol);
        CmsListColumnDefinition flagsCol = new CmsListColumnDefinition(CmsLdapUserAction.LIST_COLUMN_FLAGS);
        flagsCol.setName(Messages.get().container(Messages.GUI_LDAP_LIST_COLS_TYPE_0));
        flagsCol.setVisible(false);
        metadata.addColumn(flagsCol);
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
            item.set("co", "/" + user.getOuFqn());
        }
        item.set("ct", new Boolean(CmsLdapManager.hasLdapFlag(user.getFlags())));
        item.set(CmsLdapUserAction.LIST_COLUMN_FLAGS, new Integer(user.getFlags()));
    }
}
