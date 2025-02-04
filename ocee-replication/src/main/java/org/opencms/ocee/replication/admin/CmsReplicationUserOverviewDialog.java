package org.opencms.ocee.replication.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.accounts.CmsUserOverviewDialog;

public class CmsReplicationUserOverviewDialog extends CmsUserOverviewDialog {
   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public CmsReplicationUserOverviewDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsReplicationUserOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public String getParamSettingsid() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public void setParamSettingsid(String settingsid) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = settingsid;
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("GUI_USER_EDITOR_LABEL_IDENTIFICATION_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 5));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
         result.append(this.dialogBlockStart(this.key("GUI_USER_EDITOR_LABEL_ADDRESS_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(6, 9));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.initUserObject();
      this.setKeyPrefix("user.ov");
      this.addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "lastname", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "firstname", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "email", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "address", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "zipcode", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "city", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "country", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "enabled", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this, "selfManagement", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this, "lastlogin", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this, "created", PAGES[0], new CmsDisplayWidget()));
   }

   protected void initUserObject() {
      try {
         this.m_user = CmsReplicationManager.getInstance().getReplicationUser(this.getCms(), this.getParamSettingsid(), new CmsUUID(this.getParamUserid()));
      } catch (CmsException var2) {
      }

   }

   protected void validateParamaters() throws Exception {
      CmsReplicationManager.getInstance().getReplicationUser(this.getCms(), this.getParamSettingsid(), new CmsUUID(this.getParamUserid())).getName();
   }
}
