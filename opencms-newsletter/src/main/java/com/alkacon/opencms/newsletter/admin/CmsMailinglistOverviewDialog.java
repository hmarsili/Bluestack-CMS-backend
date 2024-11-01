package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.accounts.CmsGroupOverviewDialog;

public class CmsMailinglistOverviewDialog extends CmsGroupOverviewDialog {
   public CmsMailinglistOverviewDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsMailinglistOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("GUI_GROUP_EDITOR_LABEL_IDENTIFICATION_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 1));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.initGroupObject();
      this.setKeyPrefix("group.ov");
      this.addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
   }

   protected void initMessages() {
      this.addMessages(org.opencms.workplace.tools.accounts.Messages.get().getBundleName());
      super.initMessages();
   }
}
