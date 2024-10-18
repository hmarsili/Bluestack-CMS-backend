package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

public class CmsClusterLicenseCollectDialog extends CmsWidgetDialog {
   private static final String[] PAGES = new String[]{"page1"};

   public CmsClusterLicenseCollectDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterLicenseCollectDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
      CmsClusterServer server = CmsClusterManager.getInstance().getThisServer();
      result.append(this.key("GUI_LICENSE_PREPARE_NAME_1", new Object[]{server.getLicenseData().getName()}));
      result.append("\n\n");
      if (!server.getLicenseData().isActivated()) {
         result.append(this.key("GUI_CLUSTER_LICENSE_SERVER_1", new Object[]{server.getName()}));
         result.append("\n");
         result.append(this.key("GUI_LICENSE_PREPARE_ACTKEY_1", new Object[]{server.getLicenseData().getActivationKey()}));
         result.append("\n\n");
      }

      Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

      while(it.hasNext()) {
         server = (CmsClusterServer)it.next();
         if (!server.getLicenseData().isActivated()) {
            result.append(this.key("GUI_CLUSTER_LICENSE_SERVER_1", new Object[]{server.getName()}));
            result.append("\n");
            result.append(this.key("GUI_LICENSE_PREPARE_ACTKEY_1", new Object[]{server.getLicenseData().getActivationKey()}));
            result.append("\n\n");
         }
      }

      return result.toString();
   }

   public void setCollectedData(String collectedData) {
      collectedData.toLowerCase();
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.dialogBlockStart(this.key("GUI_LICENSE_PREPARE_HEADER_TITLE_0")));
      result.append(this.key("GUI_LICENSE_PREPARE_HEADER_TEXT_0"));
      result.append("<br>\n");
      result.append(this.dialogBlockEnd());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.createDialogRowsHtml(0, 0));
         result.append(this.dialogBlockStart(this.key("GUI_LICENSE_PREPARE_FOOTER_TITLE_0")));
         result.append(this.key("GUI_LICENSE_PREPARE_FOOTER_TEXT_0"));
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.addWidget(new CmsWidgetDialogParameter(this, "collectedData", PAGES[0], new CmsTextareaWidget(12)));
   }

   protected String[] getPageArray() {
      return PAGES;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      this.addMessages(org.opencms.ocee.license.Messages.get().getBundleName());
      super.initMessages();
   }
}
