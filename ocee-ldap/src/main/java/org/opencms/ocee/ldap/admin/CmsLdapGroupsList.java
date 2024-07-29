package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.list.I_CmsListFormatter;
import org.opencms.workplace.tools.accounts.A_CmsGroupsList;

public class CmsLdapGroupsList extends A_CmsGroupsList {
    public static final String LIST_ID = "lgl";
    private static final String f159x226a583a = "ct";

    class C00011 implements I_CmsListFormatter {
        final /* synthetic */ CmsLdapGroupsList f158x226a583a;

        C00011(CmsLdapGroupsList cmsLdapGroupsList) {
            this.f158x226a583a = cmsLdapGroupsList;
        }

        public String format(Object data, Locale locale) {
            if ((data instanceof Boolean) && ((Boolean) data).booleanValue()) {
                return this.f158x226a583a.key(Messages.GUI_LDAP_TYPE_LDAP_0);
            }
            return this.f158x226a583a.key(Messages.GUI_LDAP_TYPE_CORE_0);
        }
    }

    public CmsLdapGroupsList(CmsJspActionElement jsp) {
        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_NAME_0));
    }

    public CmsLdapGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected List getGroups() throws CmsException {
        List ret = new ArrayList(OpenCms.getOrgUnitManager().getGroups(getCms(), getParamOufqn(), false));
        if (CmsLdapManager.getInstance().isLdapOnly()) {
            return CmsPrincipal.filterFlag(ret, CmsLdapManager.LDAP_FLAG);
        }
        return CmsPrincipal.filterCoreFlag(ret, CmsLdapManager.LDAP_FLAG);
    }

    protected List getListItems() throws CmsException {
        List ret = new ArrayList();
        for (CmsGroup group : (List<CmsGroup>)getGroups()) {
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set("cn", group.getName());
            item.set("cdn", group.getSimpleName());
            item.set("cc", group.getDescription());
            item.set("ct", new Boolean(CmsLdapManager.hasLdapFlag(group.getFlags())));
            ret.add(item);
        }
        return ret;
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
        typeCol.setFormatter(new C00011(this));
        metadata.addColumn(typeCol);
    }

    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {
        I_CmsListDirectAction deleteCoreAction = new CmsLdapGroupAction("ad", getCms(), false);
        deleteCoreAction.setName(Messages.get().container("GUI_GROUPS_LIST_ACTION_DELETE_NAME_0"));
        deleteCoreAction.setHelpText(Messages.get().container("GUI_GROUPS_LIST_ACTION_DELETE_HELP_0"));
        deleteCoreAction.setIconPath("list/delete.png");
        deleteCol.addDirectAction(deleteCoreAction);
        I_CmsListDirectAction deleteLdapAction = new CmsLdapGroupAction("ad2", getCms(), true);
        deleteLdapAction.setName(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_ACTION_DELETE_NAME_0));
        deleteLdapAction.setHelpText(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_ACTION_DELETE_HELP_0));
        deleteLdapAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAPGROUPS_LIST_ACTION_DELETE_CONF_0));
        deleteLdapAction.setIconPath("list/delete.png");
        deleteCol.addDirectAction(deleteLdapAction);
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
