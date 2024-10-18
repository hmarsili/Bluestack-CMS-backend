package org.opencms.ocee.cluster.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cluster.CmsClusterLicenseData;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

public class CmsClusterLicenseDialog extends CmsWidgetDialog {
   private static final String[] PAGES = new String[]{"page1"};

   public CmsClusterLicenseDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterLicenseDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
   }

   public String dialogButtonsCustom() {
      StringBuffer result = new StringBuffer(256);
      result.append(this.dialogButtonRow(0));
      this.dialogButtonsHtml(result, 0, "");
      if (!this.isActivated(true)) {
         result.append("<input name='").append("collect");
         result.append("' type=\"button\" value='");
         result.append(this.key("GUI_LICENSE_BUTTON_COLLECT_DATA_0"));
         result.append("' onclick=\"submitAction('").append("collect");
         result.append("', form);\" class=\"dialogbutton\">\n");
      }

      result.append(this.dialogButtonRow(1));
      return result.toString();
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      CmsClusterManager manager = CmsClusterManager.getInstance();
      result.append(this.dialogBlockStart(this.key("GUI_CLUSTER_LICENSE_HEADER_TITLE_0")));
      Object[] args;
      if (this.isActivated(false)) {
         args = new Object[]{manager.getWpServer().getLicenseData().getDistribution()};
         result.append(this.key("GUI_CLUSTER_LICENSE_HEADER_OK_1", args));
         result.append("<br>\n");
         result.append(this.key("GUI_CLUSTER_LICENSE_HEADER_OK2_0"));
      } else {
         args = new Object[]{manager.getWpServer().getLicenseData().getKey(), manager.getWpServer().getLicenseData().getDistribution()};
         result.append(this.key("GUI_CLUSTER_LICENSE_HEADER_ERR_2", args));
         result.append("<br>\n");
         result.append(this.key("GUI_CLUSTER_LICENSE_HEADER_ERR2_0"));
      }

      result.append(this.dialogBlockEnd());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         int wc_start = 0;
         CmsClusterServer server = manager.getThisServer();
         int wc_end = wc_start + server.getLicenseData().getWidgetCount() - 1;
         result.append(this.createWidgetBlockStart(this.key("GUI_CLUSTER_LICENSE_SERVER_1", new Object[]{server.getName()})));
         result.append(this.createDialogRowsHtml(wc_start, wc_end));
         result.append(this.createWidgetBlockEnd());
         Iterator it = manager.getOtherServers().iterator();

         while(it.hasNext()) {
            server = (CmsClusterServer)it.next();
            wc_start = wc_end + 1;
            wc_end = wc_start + server.getLicenseData().getWidgetCount() - 1;
            result.append(this.createWidgetBlockStart(this.key("GUI_CLUSTER_LICENSE_SERVER_1", new Object[]{server.getName()})));
            result.append(this.createDialogRowsHtml(wc_start, wc_end));
            result.append(this.createWidgetBlockEnd());
         }
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      int num = 0;
      this.defineWidgets(CmsClusterManager.getInstance().getThisServer(), num);
      Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

      while(it.hasNext()) {
         CmsClusterServer server = (CmsClusterServer)it.next();
         ++num;
         this.defineWidgets(server, num);
      }

   }

   protected String[] getPageArray() {
      return PAGES;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      this.addMessages(org.opencms.ocee.license.Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      this.setParamDialogtype(this.getClass().getName());
      super.initWorkplaceRequestValues(settings, request);
      if ("collect".equals(this.getParamAction())) {
         this.commitWidgetValues();
         Map params = new HashMap();
         params.put("style", "new");

         try {
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/license_collect.jsp", params);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

   }

   private void defineWidgets(CmsClusterServer server, int num) {
      CmsClusterLicenseData license = server.getLicenseData();
      this.addWidget(new CmsWidgetDialogParameter(license, "type", "type" + num, PAGES[0], new CmsDisplayWidget()));
      if (license.isAccessible()) {
         this.addWidget(new CmsWidgetDialogParameter(license, "name", "name" + num, PAGES[0], new CmsDisplayWidget()));
         this.addWidget(new CmsWidgetDialogParameter(license, "distribution", "dist" + num, PAGES[0], new CmsDisplayWidget()));
         if (!license.isActivated()) {
            this.addWidget(new CmsWidgetDialogParameter(license, "activationKey", "act" + num, PAGES[0], new CmsDisplayWidget()));
         }

         if (license.isTimeLimited()) {
            this.addWidget(new CmsWidgetDialogParameter(license, "formatedTimeLeft", "time" + num, PAGES[0], new CmsDisplayWidget()));
         }
      }

   }

   private boolean isActivated(boolean act) {
      Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

      CmsClusterServer server;
      do {
         if (!it.hasNext()) {
            return true;
         }

         server = (CmsClusterServer)it.next();
      } while(server.isLicenseCompatible() && (!act || server.getLicenseData().isActivated()));

      return false;
   }
}
