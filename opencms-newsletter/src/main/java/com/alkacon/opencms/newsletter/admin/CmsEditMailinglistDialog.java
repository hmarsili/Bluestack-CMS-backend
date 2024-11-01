package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.accounts.A_CmsEditGroupDialog;

public class CmsEditMailinglistDialog extends A_CmsEditGroupDialog {
   public static final String ML_KEY_PREFIX = "mailinglist";

   public CmsEditMailinglistDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsEditMailinglistDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      this.m_group.setProjectCoWorker(false);
      this.m_group.setProjectManager(false);
      this.m_group.setEnabled(true);
      this.setParentGroup("");
      super.actionCommit();
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
      this.setKeyPrefix("mailinglist");
      if (this.m_group.getId() == null) {
         this.addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsInputWidget()));
      } else {
         this.addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
      }

      this.addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsTextareaWidget()));
   }

   protected String getListClass() {
      return CmsMailinglistsList.class.getName();
   }

   protected String getListRootPath() {
      return "/newsletter/orgunit/mailinglists";
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected boolean isEditable(CmsGroup group) {
      return true;
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
