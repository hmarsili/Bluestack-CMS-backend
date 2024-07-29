package org.opencms.ocee.ldap.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

public class CmsLdapGroupSelectDialog extends CmsWidgetDialog {
    public static final String DIALOG_TYPE = "GroupSelect";
    public static final String[] PAGES = new String[]{"page1"};
    public static final String PARAM_GROUPNAME = "groupname";
    private String f155x226a583a;

    public CmsLdapGroupSelectDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsLdapGroupSelectDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void actionCommit() throws IOException, ServletException {
        List errors = new ArrayList();
        try {
            Map params = new HashMap();
            CmsGroup group = getCms().readGroup(getGroupname());
            params.put("groupid", group.getId());
            params.put("oufqn", group.getOuFqn());
            getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/groups/edit", params);
        } catch (CmsException e) {
            errors.add(e);
            setCommitErrors(errors);
        }
    }

    public String getGroupname() {
        return this.f155x226a583a;
    }

    public void setGroupname(String groupname) {
        CmsLdapManager.getInstance().addGroup(getCms(), groupname);
        try {
            getCms().readGroup(groupname).getId();
            this.f155x226a583a = groupname;
        } catch (CmsException e) {
            throw new CmsIllegalArgumentException(e.getMessageContainer());
        }
    }

    protected String createDialogHtml(String dialog) {
        StringBuffer result = new StringBuffer(1024);
        result.append(createWidgetTableStart());
        result.append(createWidgetErrorHeader());
        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(key(Messages.GUI_LDAP_GROUP_SELECT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    protected void defineWidgets() {
        addWidget(new CmsWidgetDialogParameter(this, PARAM_GROUPNAME, PAGES[0], new CmsInputWidget()));
    }

    protected String[] getPageArray() {
        return PAGES;
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        setParamDialogtype(DIALOG_TYPE);
        super.initWorkplaceRequestValues(settings, request);
    }
}
