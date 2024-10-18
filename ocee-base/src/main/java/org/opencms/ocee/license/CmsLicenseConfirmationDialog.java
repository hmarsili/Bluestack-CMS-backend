package org.opencms.ocee.license;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

public class CmsLicenseConfirmationDialog extends CmsWidgetDialog {
   private static final String[] Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = new String[]{"page1"};
   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   private String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;

   public CmsLicenseConfirmationDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsLicenseConfirmationDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
   }

   public String dialogButtonsCustom() {
      StringBuffer result = new StringBuffer(256);
      result.append(this.dialogButtonRow(0));
      this.dialogButtonsHtml(result, 0, "");
      CmsLicenseManager manager = CmsLicenseManager.getInstance();
      if (!manager.isActivated()) {
         this.dialogButtonsHtml(result, 9, "");
      }

      result.append(this.dialogButtonRow(1));
      return result.toString();
   }

   public String getDaysLeft() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public String getLicenseDate() {
      return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   }

   public void setDaysLeft(String daysLeft) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = daysLeft;
   }

   public void setLicenseDate(String licenseDate) {
      this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = licenseDate;
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      CmsLicenseManager manager = CmsLicenseManager.getInstance();
      result.append(this.dialogBlockStart(this.key("GUI_LICENSE_CONFIRM_HEADER_TITLE_0")));
      if (manager.isActivated()) {
         Object[] args;
         if (manager.isEvaluation() == null) {
            if (!manager.isDevelopmentVersion()) {
               args = new Object[]{manager.getConfiguration().getDistribution()};
               result.append(this.key("GUI_LICENSE_CONFIRM_ACTIVATED_TEXT_1", args));
               result.append("<br>&nbsp;<br>\n");
               result.append(this.key("GUI_LICENSE_CONFIRM_ACTIVATED_TEXT2_0"));
            } else {
               result.append(this.key("GUI_LICENSE_CONFIRM_DEVELOPMENT_TEXT_0"));
            }
         } else if (manager.getEvaluationDaysLeft() <= 0L) {
            result.append(this.key("GUI_LICENSE_CONFIRM_EXPIRED_TEXT_0"));
            result.append("<br>\n");
            args = new Object[]{manager.getFormatedExpDate(this.getLocale())};
            result.append(this.key("GUI_LICENSE_CONFIRM_EXPIRED_TEXT2_1", args));
            result.append("<br>&nbsp;<br>\n");
            result.append(this.key("GUI_LICENSE_CONFIRM_EXPIRED_TEXT3_0"));
         } else {
            result.append(this.key("GUI_LICENSE_CONFIRM_EVALUATION_TEXT_0"));
            result.append("<br>&nbsp;<br>\n");
            args = new Object[]{new Long(manager.getEvaluationDaysLeft())};
            result.append(this.key("GUI_LICENSE_CONFIRM_EVALUATION_TEXT2_1", args));
            result.append("<br>\n");
            args = new Object[]{manager.getFormatedExpDate(this.getLocale())};
            result.append(this.key("GUI_LICENSE_CONFIRM_EVALUATION_TEXT3_1", args));
            result.append("<br>&nbsp;<br>\n");
            args = new Object[]{this.key(manager.getEvaluationName())};
            result.append(this.key("GUI_LICENSE_CONFIRM_EVALUATION_TEXT4_1", args));
         }
      } else {
         result.append(this.key("GUI_LICENSE_CONFIRM_DISABLED_TEXT_0"));
      }

      result.append("<br>\n");
      result.append(this.dialogBlockEnd());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new[0])) {
         int widgets = 2 + (manager.isActivated() ? 2 - (manager.getEvaluationDaysLeft() <= 0L ? 1 : 0) : 0);
         result.append(this.createWidgetBlockStart(this.key("GUI_LICENSE_INFO_TITLE_0")));
         result.append(this.createDialogRowsHtml(0, widgets));
         result.append(this.createWidgetBlockEnd());
         if (manager.isActivated()) {
            result.append(this.dialogBlockStart(this.key("GUI_LICENSE_CONFIRM_FOOTER_TITLE_0")));
            result.append(this.key("GUI_LICENSE_CONFIRM_FOOTER_TEXT_0"));
            result.append(this.dialogBlockEnd());
         }
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      CmsLicenseManager manager = CmsLicenseManager.getInstance();
      CmsLicenseConfiguration conf = manager.getConfiguration();
      this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = manager.getFormattedLicenseDate(this.getLocale());
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = "" + manager.getEvaluationDaysLeft();
      this.addWidget(new CmsWidgetDialogParameter(conf, "licenseName", Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(conf, "activationKey", Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(conf, "licenseKey", Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new[0], new CmsDisplayWidget()));
      if (manager.isActivated()) {
         if (manager.isEvaluation() == null) {
            this.addWidget(new CmsWidgetDialogParameter(conf, "distribution", Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new[0], new CmsDisplayWidget()));
         }

         this.addWidget(new CmsWidgetDialogParameter(this, "licenseDate", Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new[0], new CmsDisplayWidget()));
         if (manager.isEvaluation() != null && manager.getEvaluationDaysLeft() >= 0L) {
            this.addWidget(new CmsWidgetDialogParameter(this, "daysLeft", Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new[0], new CmsDisplayWidget()));
         }
      }

   }

   protected String[] getPageArray() {
      return Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      this.addMessages(org.opencms.ocee.base.Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      this.setParamDialogtype(this.getClass().getName());
      this.fillParamValues(request);
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamPage()) || !this.getPages().contains(this.getParamPage())) {
         this.setParamPage((String)this.getPages().get(0));
      }

      this.defineWidgets();
      this.fillWidgetValues(request);
      if ("back".equals(this.getParamAction())) {
         try {
            this.getToolManager().jspForwardTool(this, "/ocee-license", (Map)null);
         } catch (Exception var5) {
         }
      }

      if ("save".equals(this.getParamAction())) {
         try {
            this.getToolManager().jspForwardTool(this, "/", (Map)null);
         } catch (Exception var4) {
         }
      }

   }
}
