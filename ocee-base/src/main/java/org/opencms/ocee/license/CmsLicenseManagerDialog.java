package org.opencms.ocee.license;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

public class CmsLicenseManagerDialog extends CmsWidgetDialog {
   public static final String COLLECT_ACTION = "collect";
   private static final String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = "license";
   private static final String[] o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = new String[]{"page1"};
   private CmsLicenseData Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   private String Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;

   public CmsLicenseManagerDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsLicenseManagerDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      ArrayList errors = new ArrayList();

      try {
         CmsLicenseManager manager = CmsLicenseManager.getInstance();
         if (!this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.isAgree() && (!manager.isActivated() || manager.isEvaluation() != null)) {
            throw new CmsException(Messages.get().container("ERR_LICENSE_NOT_AGREED_0"));
         }

         CmsLicenseConfiguration config = manager.getConfiguration();
         if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getKey().equals(config.getLicenseKey()) && this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getName().equals(config.getLicenseName())) {
            if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.isEnabled() != config.isEnabled()) {
               config.setEnabled(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.isEnabled());
            }
         } else {
            config.setLicenseKey(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getKey());
            config.setLicenseName(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getName());
            manager.writeConfiguration();
            Map params = new HashMap();
            params.put("style", "new");
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-license/confirm.jsp", params);
         }
      } catch (Exception var5) {
         errors.add(var5);
      }

      this.setCommitErrors(errors);
   }

   public String dialogButtonsCustom() {
      StringBuffer result = new StringBuffer(256);
      result.append(this.dialogButtonRow(0));
      this.dialogButtonsHtml(result, 0, "");
      this.dialogButtonsHtml(result, 1, "");
      CmsLicenseManager manager = CmsLicenseManager.getInstance();
      if (!manager.isActivated() || manager.isEvaluation() != null) {
         result.append("<input name='").append("collect").append("' type=\"button\" value='");
         result.append(this.key("GUI_LICENSE_BUTTON_COLLECT_DATA_0"));
         result.append("' onclick=\"submitAction('").append("collect");
         result.append("', form);\" class=\"dialogbutton\">\n");
      }

      result.append(this.dialogButtonRow(1));
      return result.toString();
   }

   public String getUseTerms() {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String)) {
         try {
            this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = new String(this.getCms().readFile("/system/modules/org.opencms.ocee.base/pages/agreement.txt").getContents());
         } catch (CmsException var2) {
            this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = "";
         }
      }

      return this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      CmsLicenseManager manager = CmsLicenseManager.getInstance();
      result.append(this.dialogBlockStart(this.key("GUI_LICENSE_MANAGER_HEADER_TITLE_0")));
      if (manager.isActivated()) {
         CmsLicenseConfiguration conf = manager.getConfiguration();
         Object[] args;
         if (manager.isEvaluation() == null) {
            args = new Object[]{conf.getDistribution()};
            if (!manager.isDevelopmentVersion()) {
               result.append(this.key("GUI_LICENSE_MANAGER_ACTIVATED_TEXT_1", args));
               result.append("<br>\n");
               result.append(this.key("GUI_LICENSE_MANAGER_ACTIVATED_TEXT2_0"));
               result.append("<br>\n");
            } else {
               result.append(this.key("GUI_LICENSE_MANAGER_DEVELOPMENT_TEXT_1", args));
               result.append("<br>&nbsp;<br>\n");
               if (conf.isEnabled()) {
                  long time = OpenCms.getSystemInfo().getRuntime();
                  if (time > (long)(conf.getDevelopmentTime() * 60 * 60 * 1000)) {
                     result.append(this.key("GUI_LICENSE_MANAGER_DEV_OVER_TEXT2_0"));
                     result.append("<br>\n");
                     result.append(this.key("GUI_LICENSE_MANAGER_DEV_OVER_TEXT3_0"));
                     result.append("<br>&nbsp;<br>\n");
                     result.append(this.key("GUI_LICENSE_MANAGER_DEV_OVER_TEXT4_0"));
                     result.append("<br>\n");
                     result.append(this.key("GUI_LICENSE_MANAGER_DEV_OVER_TEXT5_0"));
                  } else {
                     args = new Object[]{manager.getFormatedDevExpirationDate(this.getLocale())};
                     result.append(this.key("GUI_LICENSE_MANAGER_DEVELOPMENT_TEXT2_0"));
                     result.append("<br>\n");
                     result.append(this.key("GUI_LICENSE_MANAGER_DEVELOPMENT_TEXT3_1", args));
                     result.append("<br>&nbsp;<br>\n");
                     result.append(this.key("GUI_LICENSE_MANAGER_DEVELOPMENT_TEXT4_1", args));
                  }
               } else {
                  result.append(this.key("GUI_LICENSE_MANAGER_DISABLED_OCEE_TEXT_0"));
               }
            }
         } else if (manager.getEvaluationDaysLeft() <= 0L) {
            args = new Object[]{conf.getDistribution(), this.key(manager.getEvaluationName())};
            result.append(this.key("GUI_LICENSE_MANAGER_EXPIRED_TEXT_2", args));
            result.append("<br>&nbsp;<br>\n");
            args = new Object[]{this.key(manager.getEvaluationName())};
            result.append(this.key("GUI_LICENSE_MANAGER_EXPIRED_TEXT2_1", args));
            result.append("<br>&nbsp;<br>\n");
            result.append(this.key("GUI_LICENSE_MANAGER_EXPIRED_TEXT3_1", args));
         } else {
            args = new Object[]{conf.getDistribution(), this.key(manager.getEvaluationName())};
            result.append(this.key("GUI_LICENSE_MANAGER_EVALUATION_TEXT_2", args));
            result.append("<br>&nbsp;<br>\n");
            result.append(this.key("GUI_LICENSE_MANAGER_EVALUATION_TEXT2_0"));
            result.append("<br>&nbsp;<br>\n");
            args = new Object[]{new Long(manager.getEvaluationDaysLeft())};
            result.append(this.key("GUI_LICENSE_MANAGER_EVALUATION_TEXT3_1", args));
            result.append("<br>\n");
            args = new Object[]{manager.getFormatedExpDate(this.getLocale())};
            result.append(this.key("GUI_LICENSE_MANAGER_EVALUATION_TEXT4_1", args));
            result.append("<br>&nbsp;<br>\n");
            args = new Object[]{this.key(manager.getEvaluationName())};
            result.append(this.key("GUI_LICENSE_MANAGER_EVALUATION_TEXT5_1", args));
         }
      } else {
         result.append(this.key("GUI_LICENSE_MANAGER_DISABLED_TEXT_0"));
         result.append("<br>\n");
         result.append(this.key("GUI_LICENSE_MANAGER_DISABLED_TEXT2_0"));
         result.append("<br>&nbsp;<br>\n");
         result.append(this.key("GUI_LICENSE_MANAGER_DISABLED_TEXT3_0"));
         result.append("<br>\n");
         result.append(this.key("GUI_LICENSE_MANAGER_DISABLED_TEXT4_0"));
         result.append("<br>&nbsp;<br>\n");
         result.append(this.key("GUI_LICENSE_MANAGER_DISABLED_TEXT5_0"));
      }

      result.append(this.dialogBlockEnd());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0])) {
         result.append(this.createWidgetBlockStart(this.key("GUI_LICENSE_INFO_TITLE_0")));
         result.append(this.createDialogRowsHtml(0, 1));
         result.append(this.createWidgetBlockEnd());
         if (manager.isActivated() && manager.isEvaluation() == null) {
            if (manager.isDevelopmentVersion()) {
               if (manager.getConfiguration().isEnabled()) {
                  result.append(this.createWidgetBlockStart(this.key("GUI_LICENSE_DISABLE_OCEE_TITLE_0")));
                  result.append("<tr><td colspan=\"5\">\n");
                  result.append(this.key("GUI_LICENSE_MANAGER_DISABLE_OCEE_TEXT_0"));
                  result.append("</td></tr>\n");
                  result.append(this.createDialogRowsHtml(2, 2));
               } else {
                  result.append(this.createWidgetBlockStart(this.key("GUI_LICENSE_DISABLED_OCEE_TITLE_0")));
                  result.append("<tr><td colspan=\"5\">\n");
                  result.append(this.key("GUI_LICENSE_MANAGER_DISABLED_OCEE_TEXT_0"));
                  result.append("</td></tr>\n");
               }
            }
         } else {
            result.append(this.createWidgetBlockStart(this.key("GUI_LICENSE_AGREEMENT_TITLE_0")));
            result.append("<tr><td colspan=\"5\">\n");
            result.append("<textarea cols='100' rows='10'>").append(this.getUseTerms()).append("</textarea>");
            result.append("</td></tr>\n");
            result.append(this.createDialogRowsHtml(2, 2));
         }

         result.append(this.createWidgetBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      CmsLicenseManager manager = CmsLicenseManager.getInstance();
      if (manager.isActivated() && manager.isEvaluation() == null) {
         this.addWidget(new CmsWidgetDialogParameter(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object, "name", o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0], new CmsDisplayWidget()));
      } else {
         this.addWidget(new CmsWidgetDialogParameter(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object, "name", o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0], new CmsInputWidget()));
      }

      this.addWidget(new CmsWidgetDialogParameter(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object, "key", o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0], new CmsInputWidget()));
      if (manager.isActivated() && manager.isEvaluation() == null) {
         if (manager.isDevelopmentVersion() && manager.getConfiguration().isEnabled()) {
            this.addWidget(new CmsWidgetDialogParameter(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object, "enabled", o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0], new CmsCheckboxWidget()));
         }
      } else {
         this.addWidget(new CmsWidgetDialogParameter(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object, "agree", o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super[0], new CmsCheckboxWidget()));
      }

   }

   protected String[] getPageArray() {
      return o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      this.addMessages(org.opencms.ocee.base.Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      this.setParamDialogtype("license");
      super.initWorkplaceRequestValues(settings, request);
      if ("collect".equals(this.getParamAction())) {
         this.commitWidgetValues();
         Map params = new HashMap();
         params.put("name", this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getName());
         params.put("style", "new");

         try {
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-license/prepare.jsp", params);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      this.setDialogObject(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object);
   }

   private void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
      Object o;
      if (!CmsStringUtil.isEmpty(this.getParamAction()) && !"initial".equals(this.getParamAction())) {
         o = this.getDialogObject();
      } else {
         o = null;
      }

      if (!(o instanceof CmsLicenseData)) {
         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = new CmsLicenseData();
         CmsLicenseConfiguration license = CmsLicenseManager.getInstance().getConfiguration();

         try {
            this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.setName(license.getLicenseName());
         } catch (Throwable var5) {
         }

         try {
            this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.setKey(license.getLicenseKey());
         } catch (Throwable var4) {
         }

         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.setAgree(false);
         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.setEnabled(license.isEnabled());
      } else {
         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = (CmsLicenseData)o;
      }

   }
}
