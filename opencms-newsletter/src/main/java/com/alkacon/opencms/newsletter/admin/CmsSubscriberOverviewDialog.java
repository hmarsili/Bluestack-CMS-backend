package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.accounts.CmsUserOverviewDialog;

public class CmsSubscriberOverviewDialog extends CmsUserOverviewDialog {
   public static final String SB_KEY_PREFIX = "subscriber";

   public CmsSubscriberOverviewDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsSubscriberOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("GUI_USER_EDITOR_LABEL_IDENTIFICATION_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 3));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.initUserObject();
      this.setKeyPrefix("subscriber");
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "email", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "lastname", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "firstname", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "enabled", PAGES[0], new CmsDisplayWidget()));
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }
}
