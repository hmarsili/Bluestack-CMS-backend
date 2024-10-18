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
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterServer;
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
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsClusterConfigList extends A_CmsListDialog {
   public static final String LIST_ACTION_ICON = "ai";
   public static final String LIST_ACTION_STATE = "as";
   public static final String LIST_COLUMN_FILE = "cf";
   public static final String LIST_COLUMN_REALFILE = "cr";
   public static final String LIST_COLUMN_ICON = "ci";
   public static final String LIST_COLUMN_SERVER = "cv";
   public static final String LIST_COLUMN_STATE = "cs";
   public static final String LIST_DEFACTION_VIEW = "dv";
   public static final String LIST_IACTION_SHOW = "is";
   public static final String LIST_ID = "lcc";
   private static boolean m_showAll = false;

   public CmsClusterConfigList(CmsJspActionElement jsp) {
      this("lcc", jsp);
   }

   public CmsClusterConfigList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsClusterConfigList(String listId, CmsJspActionElement jsp) {
      super(jsp, listId, Messages.get().container("GUI_CLUSTER_CONFIG_LIST_NAME_0"), "cf", CmsListOrderEnum.ORDER_ASCENDING, "cf");
   }

   public void executeListIndepActions() {
      if (this.getParamListAction().equals("is")) {
         m_showAll = !m_showAll;
         this.refreshList();
      }

      super.executeListIndepActions();
   }

   public void executeListMultiActions() {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() throws IOException, ServletException {
      if (this.getParamListAction().equals("dv")) {
         Map params = new HashMap();
         params.put("server", this.getSelectedItem().get("cv"));
         String configFile = (String)this.getSelectedItem().get("cf");
         if (configFile.equals(this.key("GUI_CONFIG_LIB_FOLDER_DISPLAYNAME_0"))) {
            configFile = "LIB_FOLDER";
         }

         params.put("filename", configFile);
         params.put("style", "new");
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, "/ocee-cluster/config-check/view", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   protected void fillDetails(String detailId) {
   }

   protected List getListItems() {
      List ret = new ArrayList();
      Iterator itFilenames = CmsClusterManager.getLocalConfigFileNames().iterator();

      label32:
      while(itFilenames.hasNext()) {
         String filename = (String)itFilenames.next();
         Iterator itServers = CmsClusterManager.getInstance().getOtherServers().iterator();

         while(true) {
            CmsClusterServer server;
            do {
               if (!itServers.hasNext()) {
                  continue label32;
               }

               server = (CmsClusterServer)itServers.next();
            } while(!m_showAll && server.isIdenticalConfigFile(filename));

            CmsListItem item = this.getList().newItem(server.getName() + filename);
            if (filename.equals("LIB_FOLDER")) {
               item.set("cf", this.key("GUI_CONFIG_LIB_FOLDER_DISPLAYNAME_0"));
            } else {
               item.set("cf", filename);
            }

            item.set("cv", server.getName());
            item.set("cr", filename);
            ret.add(item);
         }
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      CmsClusterServer server = CmsClusterManager.getInstance().getThisServer();
      server.retrieveAllConfigFiles();
      Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

      while(it.hasNext()) {
         server = (CmsClusterServer)it.next();
         server.retrieveAllConfigFiles();
      }

      super.initWorkplaceRequestValues(settings, request);
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconAction = new CmsListDirectAction("ai");
      iconAction.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_ICON_HELP_0"));
      iconAction.setIconPath("tools/ocee-cluster/buttons/file.png");
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition stateCol = new CmsListColumnDefinition("cs");
      stateCol.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_COLS_STATE_0"));
      stateCol.setWidth("20");
      stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      stateCol.setListItemComparator(new CmsListItemActionIconComparator());
      CmsListDirectAction okAction = new CmsListDirectAction("aso") {
         public boolean isVisible() {
            String serverName = this.getItem().get("cv").toString();
            String fileName = this.getItem().get("cr").toString();
            CmsClusterServer server = CmsClusterManager.getInstance().getServer(serverName);
            return server.isIdenticalConfigFile(fileName);
         }
      };
      okAction.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_OK_NAME_0"));
      okAction.setHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_OK_HELP_0"));
      okAction.setIconPath("tools/ocee-cluster/buttons/file_ok.png");
      okAction.setEnabled(false);
      stateCol.addDirectAction(okAction);
      CmsListDirectAction errAction = new CmsListDirectAction("as") {
         public boolean isVisible() {
            String serverName = this.getItem().get("cv").toString();
            String fileName = this.getItem().get("cr").toString();
            CmsClusterServer server = CmsClusterManager.getInstance().getServer(serverName);
            return !server.isIdenticalConfigFile(fileName) && !server.isMissingConfigFile(fileName);
         }
      };
      errAction.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_ERR_NAME_0"));
      errAction.setHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_ERR_HELP_0"));
      errAction.setIconPath("tools/ocee-cluster/buttons/file_diff.png");
      errAction.setEnabled(false);
      stateCol.addDirectAction(errAction);
      CmsListDirectAction ignoreAction = new CmsListDirectAction("asi") {
         public boolean isVisible() {
            String serverName = this.getItem().get("cv").toString();
            String fileName = this.getItem().get("cf").toString();
            CmsClusterServer server = CmsClusterManager.getInstance().getServer(serverName);
            return server.isMissingConfigFile(fileName);
         }
      };
      ignoreAction.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_IGN_NAME_0"));
      ignoreAction.setHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_ACTION_IGN_HELP_0"));
      ignoreAction.setIconPath("tools/ocee-cluster/buttons/file_mis.png");
      ignoreAction.setEnabled(false);
      stateCol.addDirectAction(ignoreAction);
      metadata.addColumn(stateCol);
      CmsListColumnDefinition fileCol = new CmsListColumnDefinition("cf");
      fileCol.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_COLS_FILE_0"));
      fileCol.setWidth("60%");
      CmsListDefaultAction viewAction = new CmsListDefaultAction("dv") {
         public boolean isVisible() {
            String serverName = this.getItem().get("cv").toString();
            String fileName = this.getItem().get("cr").toString();
            CmsClusterServer server = CmsClusterManager.getInstance().getServer(serverName);
            return !server.isIdenticalConfigFile(fileName) && !server.isMissingConfigFile(fileName);
         }
      };
      viewAction.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_DEFACTION_VIEW_NAME_0"));
      viewAction.setHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_DEFACTION_VIEW_HELP_0"));
      fileCol.addDefaultAction(viewAction);
      CmsListDefaultAction disabledViewAction = new CmsListDefaultAction("dvd") {
         public boolean isVisible() {
            String serverName = this.getItem().get("cv").toString();
            String fileName = this.getItem().get("cr").toString();
            CmsClusterServer server = CmsClusterManager.getInstance().getServer(serverName);
            return server.isIdenticalConfigFile(fileName) || server.isMissingConfigFile(fileName);
         }
      };
      disabledViewAction.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_DEFACTION_NO_VIEW_NAME_0"));
      disabledViewAction.setHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_DEFACTION_NO_VIEW_HELP_0"));
      disabledViewAction.setEnabled(false);
      fileCol.addDefaultAction(disabledViewAction);
      metadata.addColumn(fileCol);
      CmsListColumnDefinition realCol = new CmsListColumnDefinition("cr");
      realCol.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_COLS_FILE_0"));
      realCol.setVisible(false);
      realCol.setWidth("0");
      metadata.addColumn(realCol);
      CmsListColumnDefinition serverCol = new CmsListColumnDefinition("cv");
      serverCol.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_COLS_SERVER_0"));
      serverCol.setWidth("40%");
      metadata.addColumn(serverCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails eventDetails = new CmsListItemDetails("is");
      eventDetails.setAtColumn("cf");
      eventDetails.setVisible(false);
      eventDetails.setShowActionName(Messages.get().container("GUI_CLUSTER_CONFIG_DETAIL_SHOW_ALL_NAME_0"));
      eventDetails.setShowActionHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_DETAIL_SHOW_ALL_HELP_0"));
      eventDetails.setHideActionName(Messages.get().container("GUI_CLUSTER_CONFIG_DETAIL_HIDE_ALL_NAME_0"));
      eventDetails.setHideActionHelpText(Messages.get().container("GUI_CLUSTER_CONFIG_DETAIL_HIDE_ALL_HELP_0"));
      eventDetails.setName(Messages.get().container("GUI_CLUSTER_CONFIG_DETAIL_ALL_NAME_0"));
      eventDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_CLUSTER_CONFIG_DETAIL_ALL_NAME_0")));
      metadata.addItemDetails(eventDetails);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }
}
