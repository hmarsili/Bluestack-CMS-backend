package org.opencms.ocee.license;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

public class CmsLicensePrepareDialog extends CmsWidgetDialog {
   private static final String[] o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = new String[]{"page1"};
   private String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;

   public CmsLicensePrepareDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsLicensePrepareDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      List errors = new ArrayList();
      this.setCommitErrors(errors);
   }

   public String dialogButtonsCustom() {
      StringBuffer result = new StringBuffer(256);
      result.append(this.dialogButtonRow(0));
      this.dialogButtonsHtml(result, 0, "");
      result.append(this.dialogButtonRow(1));
      return result.toString();
   }

   public String getCollectedData() {
      StringBuffer result = new StringBuffer(512);
      result.append(this.key("GUI_LICENSE_PREPARE_NAME_1", new Object[]{this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new}));
      result.append("\n");
      result.append(this.key("GUI_LICENSE_PREPARE_ACTKEY_1", new Object[]{CmsLicenseManager.getInstance().getConfiguration().getActivationKey()}));
      result.append("\n");
      return result.toString();
   }

   public String getParamName() {
      return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   }

   public void setCollectedData(String collectedData) {
      collectedData.toLowerCase();
   }

   public void setParamName(String paramName) {
      this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = paramName;
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.dialogBlockStart(this.key("GUI_LICENSE_PREPARE_HEADER_TITLE_0")));
      result.append(this.key("GUI_LICENSE_PREPARE_HEADER_TEXT_0"));
      result.append("<br>\n");
      result.append(this.dialogBlockEnd());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0])) {
         result.append(this.createDialogRowsHtml(0, 0));
         result.append(this.dialogBlockStart(this.key("GUI_LICENSE_PREPARE_FOOTER_TITLE_0")));
         result.append(this.key("GUI_LICENSE_PREPARE_FOOTER_TEXT_0"));
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.addWidget(new CmsWidgetDialogParameter(this, "collectedData", o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0], new CmsTextareaWidget()));
   }

   protected String[] getPageArray() {
      return o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      this.addMessages(org.opencms.ocee.base.Messages.get().getBundleName());
      super.initMessages();
   }
}
