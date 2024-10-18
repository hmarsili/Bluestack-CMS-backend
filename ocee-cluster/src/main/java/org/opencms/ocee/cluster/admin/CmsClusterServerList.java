package org.opencms.ocee.cluster.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cluster.CmsClusterConfiguration;
import org.opencms.ocee.cluster.CmsClusterEventTypes;
import org.opencms.ocee.cluster.CmsClusterLicenseData;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.util.CmsDateUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsClusterServerList extends A_CmsListDialog {
   public static final String LIST_ACTION_ICON = "ai";
   public static final String LIST_ACTION_STATE = "as";
   public static final String LIST_DEFACTION_CONF = "dac";
   public static final String LIST_DEFACTION_NOCONF = "dan";
   public static final String LIST_COLUMN_ICON = "ci";
   public static final String LIST_COLUMN_IP = "cp";
   public static final String LIST_COLUMN_NAME = "cn";
   public static final String LIST_COLUMN_STATE = "cs";
   public static final String LIST_COLUMN_URL = "cu";
   public static final String LIST_COLUMN_CONF = "cc";
   public static final String LIST_DETAIL_EVENT = "de";
   public static final String LIST_DETAIL_LICENSE = "dl";
   public static final String LIST_DETAIL_STATE = "ds";
   public static final String LIST_ID = "lcs";
   public static final String LIST_MACTION_EXPORT = "me";

   public CmsClusterServerList(CmsJspActionElement jsp) {
      this("lcs", jsp);
   }

   public CmsClusterServerList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsClusterServerList(String listId, CmsJspActionElement jsp) {
      super(jsp, listId, Messages.get().container("GUI_CLUSTER_SERVERS_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void actionReinitializeCluster() throws JspException {
      CmsClusterManager.getInstance().reInitializeCluster();
      this.actionCloseDialog();
   }

   public void actionSetNewWpServer() throws JspException {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      manager.setWpServer(manager.getThisServer().getName());
      OpenCms.writeConfiguration(CmsClusterConfiguration.class);
      List servers = manager.getOtherServers();
      int i = 0;

      for(int n = servers.size(); i < n; ++i) {
         CmsClusterServer server = (CmsClusterServer)servers.get(i);
         Map eventData = new HashMap();
         eventData.put("wpServerName", manager.getThisServer().getName());
         CmsClusterManager.getInstance().getEventHandler().forwardEvent(server, CmsClusterEventTypes.SET_WP_SERVER.getType(), eventData, eventData);
      }

      this.refreshList();
      this.actionCloseDialog();
   }

   public String defaultActionHtmlContent() {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager.getWpServer() == null || manager.getThisServer() == null) {
         manager.reInitializeCluster();
      }

      return super.defaultActionHtmlContent();
   }

   public void executeListMultiActions() throws IOException, ServletException {
      if (this.getParamListAction().equals("me")) {
         Map params = new HashMap();
         params.put("servers", this.getParamSelItems());
         params.put("style", "new");
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, "/ocee-cluster/export", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   public void executeListSingleActions() throws IOException, ServletException {
      if (this.getParamListAction().equals("dac")) {
         Map params = new HashMap();
         params.put("server", this.getSelectedItem().get("cn"));
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, "/ocee-cluster/config", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   protected void fillDetails(String detailId) {
      CmsClusterManager clusterManager = CmsClusterManager.getInstance();
      Iterator itServers = this.getList().getAllContent().iterator();

      while(itServers.hasNext()) {
         CmsListItem item = (CmsListItem)itServers.next();
         CmsClusterServer server = clusterManager.getServer((String)item.get("cn"));

         try {
            String status;
            if (detailId.equals("de")) {
               if (CmsClusterManager.getInstance().getWpServer().getName().equals(item.getId())) {
                  status = clusterManager.getLastEventType() == 0 ? "-" : CmsClusterEventTypes.valueOf(clusterManager.getLastEventType()).toString();
                  String lastClusterEventDate = clusterManager.getLastEventDate() == null ? "-" : CmsDateUtil.getDateTime(clusterManager.getLastEventDate(), 1, this.getLocale());
                  StringBuffer html = new StringBuffer();
                  html.append("<table border='0'>").append("\n");
                  html.append("<tr><td>").append(this.key("GUI_EVENT_TYPE_0")).append(":</td><td>");
                  html.append(status).append("</td></tr>").append("\n");
                  html.append("<tr><td>").append(this.key("GUI_EVENT_DATE_0")).append(":</td><td>");
                  html.append(lastClusterEventDate).append("</td></tr>").append("\n");
                  html.append("</table>").append("\n");
                  item.set(detailId, html.toString());
               }
            } else if (detailId.equals("ds")) {
               status = this.key("GUI_CLUSTER_SERVER_STATUS_OK_0");
               if (server.getStatus(this.getLocale()) != null) {
                  status = server.getStatus(this.getLocale());
               }

               item.set(detailId, status);
            } else if (detailId.equals("dl")) {
               CmsClusterLicenseData license = server.getLicenseData();
               StringBuffer html = new StringBuffer();
               if (license.isAccessible()) {
                  html.append("<table border='0'>").append("\n");
                  html.append("<tr><td>").append(this.key("GUI_LICENSE_TYPE_0")).append(":</td><td>");
                  html.append(license.getType()).append("</td></tr>").append("\n");
                  html.append("<tr><td>").append(this.key("GUI_LICENSE_NAME_0")).append(":</td><td>");
                  html.append(license.getName()).append("</td></tr>").append("\n");
                  html.append("<tr><td>").append(this.key("GUI_LICENSE_DIST_0")).append(":</td><td>");
                  html.append(license.getDistribution()).append("</td></tr>").append("\n");
                  if (!license.isActivated()) {
                     html.append("<tr><td>").append(this.key("GUI_LICENSE_ACTKEY_0")).append(":</td><td>");
                     html.append(license.getActivationKey()).append("</td></tr>").append("\n");
                  }

                  if (license.isTimeLimited()) {
                     html.append("<tr><td>").append(this.key("GUI_LICENSE_TIMELEFT_0")).append(":</td><td>");
                     html.append(license.getFormatedTimeLeft()).append("</td></tr>").append("\n");
                  }

                  html.append("</table>").append("\n");
               } else {
                  html.append(this.key("GUI_LICENSE_TYPE_NOACCESS_0"));
               }

               item.set(detailId, html.toString());
            }
         } catch (Exception var9) {
         }
      }

   }

   protected List getListItems() {
      List ret = new ArrayList();
      Iterator itServers = CmsClusterManager.getInstance().getConfiguration().getAllServers().iterator();

      while(itServers.hasNext()) {
         CmsClusterServer server = (CmsClusterServer)itServers.next();
         CmsListItem item = this.getList().newItem(server.getName());
         item.set("cn", server.getName());
         item.set("cp", server.getIp());
         item.set("cu", server.getUrl());
         item.set("cc", new Boolean(server.isAccessible()));
         ret.add(item);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager.getWpServer() == null || manager.getThisServer() == null) {
         manager.reInitializeCluster();
      }

      super.initWorkplaceRequestValues(settings, request);
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setListItemComparator(new CmsListItemActionIconComparator());
      CmsListDirectAction iconAction = new CmsListDirectAction("ai") {
         public String getIconPath() {
            return CmsClusterManager.getInstance().getWpServer().getName().equals(this.getItem().getId()) ? "tools/ocee-cluster/buttons/wpserver.png" : "tools/ocee-cluster/buttons/server.png";
         }

         public CmsMessageContainer getName() {
            return CmsClusterManager.getInstance().getWpServer().getName().equals(this.getItem().getId()) ? Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_WP_NAME_0") : Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_NAME_0");
         }
      };
      iconAction.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_HELP_0"));
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition stateCol = new CmsListColumnDefinition("cs");
      stateCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_STATE_0"));
      stateCol.setWidth("20");
      stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      stateCol.setListItemComparator(new CmsListItemActionIconComparator());
      CmsListDirectAction okAction = new CmsListDirectAction("as") {
         public boolean isVisible() {
            try {
               return CmsClusterManager.getInstance().getServer(this.getItem().getId()).getStatus(CmsClusterServerList.this.getLocale()) == null;
            } catch (Throwable var2) {
               return false;
            }
         }
      };
      okAction.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_OK_NAME_0"));
      okAction.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_OK_HELP_0"));
      okAction.setIconPath("tools/ocee-cluster/buttons/state_ok.png");
      okAction.setEnabled(false);
      stateCol.addDirectAction(okAction);
      CmsListDirectAction errAction = new CmsListDirectAction("ase") {
         public boolean isVisible() {
            try {
               return CmsClusterManager.getInstance().getServer(this.getItem().getId()).getStatus(CmsClusterServerList.this.getLocale()) != null;
            } catch (Throwable var2) {
               return true;
            }
         }
      };
      errAction.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ERR_NAME_0"));
      errAction.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ERR_HELP_0"));
      errAction.setIconPath("tools/ocee-cluster/buttons/state_err.png");
      errAction.setEnabled(false);
      stateCol.addDirectAction(errAction);
      metadata.addColumn(stateCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_NAME_0"));
      nameCol.setWidth("30%");
      if (CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
         CmsListDefaultAction defActionConf = new CmsListDefaultAction("dac") {
            public boolean isVisible() {
               return (Boolean)this.getItem().get("cc");
            }
         };
         defActionConf.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_DEFACTION_CONF_NAME_0"));
         defActionConf.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_DEFACTION_CONF_HELP_0"));
         nameCol.addDefaultAction(defActionConf);
         CmsListDefaultAction defActionNoConf = new CmsListDefaultAction("dan") {
            public boolean isVisible() {
               return !(Boolean)this.getItem().get("cc");
            }
         };
         defActionNoConf.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_DEFACTION_NOCONF_NAME_0"));
         defActionNoConf.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_DEFACTION_NOCONF_HELP_0"));
         defActionNoConf.setEnabled(false);
         nameCol.addDefaultAction(defActionNoConf);
      }

      metadata.addColumn(nameCol);
      CmsListColumnDefinition ipCol = new CmsListColumnDefinition("cp");
      ipCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_IP_0"));
      ipCol.setWidth("20%");
      metadata.addColumn(ipCol);
      CmsListColumnDefinition urlCol = new CmsListColumnDefinition("cu");
      urlCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_URL_0"));
      urlCol.setWidth("50%");
      metadata.addColumn(urlCol);
      CmsListColumnDefinition confCol = new CmsListColumnDefinition("cc");
      confCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_URL_0"));
      confCol.setVisible(false);
      metadata.addColumn(confCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails eventDetails = new CmsListItemDetails("de");
      eventDetails.setAtColumn("cn");
      eventDetails.setVisible(false);
      eventDetails.setShowActionName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_SHOW_EVENT_NAME_0"));
      eventDetails.setShowActionHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_SHOW_EVENT_HELP_0"));
      eventDetails.setHideActionName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_HIDE_EVENT_NAME_0"));
      eventDetails.setHideActionHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_HIDE_EVENT_HELP_0"));
      eventDetails.setName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_EVENT_NAME_0"));
      eventDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_EVENT_NAME_0")));
      metadata.addItemDetails(eventDetails);
      CmsListItemDetails stateDetails = new CmsListItemDetails("ds");
      stateDetails.setAtColumn("cn");
      stateDetails.setVisible(false);
      stateDetails.setShowActionName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_SHOW_STATE_NAME_0"));
      stateDetails.setShowActionHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_SHOW_STATE_HELP_0"));
      stateDetails.setHideActionName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_HIDE_STATE_NAME_0"));
      stateDetails.setHideActionHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_HIDE_STATE_HELP_0"));
      stateDetails.setName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_STATE_NAME_0"));
      stateDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_STATE_NAME_0")));
      metadata.addItemDetails(stateDetails);
      CmsListItemDetails licenseDetails = new CmsListItemDetails("dl");
      licenseDetails.setAtColumn("cn");
      licenseDetails.setVisible(false);
      licenseDetails.setShowActionName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_SHOW_LICENSE_NAME_0"));
      licenseDetails.setShowActionHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_SHOW_LICENSE_HELP_0"));
      licenseDetails.setHideActionName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_HIDE_LICENSE_NAME_0"));
      licenseDetails.setHideActionHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_HIDE_LICENSE_HELP_0"));
      licenseDetails.setName(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_LICENSE_NAME_0"));
      licenseDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_CLUSTER_SERVERS_DETAIL_LICENSE_NAME_0")));
      metadata.addItemDetails(licenseDetails);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction exportAction = new CmsListMultiAction("me");
      exportAction.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_MACTION_EXPORT_NAME_0"));
      exportAction.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_MACTION_EXPORT_HELP_0"));
      exportAction.setConfirmationMessage(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_MACTION_EXPORT_CONF_0"));
      exportAction.setIconPath("tools/ocee-cluster/buttons/multi_export.png");
      metadata.addMultiAction(exportAction);
   }
}
