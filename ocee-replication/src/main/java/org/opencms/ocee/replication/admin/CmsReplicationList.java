package org.opencms.ocee.replication.admin;

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
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.ocee.replication.CmsReplicationMode;
import org.opencms.ocee.replication.CmsReplicationServer;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsReplicationList extends A_CmsListDialog {
   public static final String LIST_ACTION_CHECK = "acr";
   public static final String LIST_ACTION_REPLICATE_FULL = "arrf";
   public static final String LIST_ACTION_SHOW = "asr";
   public static final String LIST_COLUMN_CHECK = "ccr";
   public static final String LIST_COLUMN_DEST_SERVER = "cds";
   public static final String LIST_COLUMN_MODE = "cms";
   public static final String LIST_COLUMN_NAME = "cn";
   public static final String LIST_COLUMN_ORG_SERVER = "cos";
   public static final String LIST_COLUMN_REPLICATE_FULL = "crrf";
   public static final String LIST_COLUMN_SHOW = "csr";
   public static final String LIST_DEFACTION_SHOW = "das";
   public static final String LIST_DETAIL_DESCRIPTION = "dd";
   public static final String LIST_DETAIL_SOURCES = "ds";
   public static final String LIST_ID = "lrl";
   public static final String PATH_BUTTONS = "tools/ocee-replication/buttons/";

   public CmsReplicationList(CmsJspActionElement jsp) {
      this("lrl", jsp);
   }

   public CmsReplicationList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsReplicationList(String listId, CmsJspActionElement jsp) {
      super(jsp, listId, Messages.get().container("GUI_REPLICATION_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() throws CmsRuntimeException, IOException, ServletException {
      Map params = new HashMap();
      if (!this.getParamListAction().equals("asr") && !this.getParamListAction().equals("das")) {
         if (this.getParamListAction().equals("arrf")) {
            params.put("serverid", this.getParamSelItems());
            this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/full-replication", params);
         } else if (this.getParamListAction().equals("acr")) {
            params.put("serverid", this.getParamSelItems());
            this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/view-server", params);
         }
      } else {
         CmsReplicationServer server = CmsReplicationManager.getInstance().getConfiguration().getReplicationServer(new CmsUUID(this.getSelectedItem().getId()));
         params.put("serverid", server.getServerId());
         params.put("servertitle", server.getDisplayName());
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/resources", params);
      }

   }

   protected void fillDetails(String detailId) {
      Iterator iter = this.getList().getAllContent().iterator();

      while(iter.hasNext()) {
         CmsListItem item = (CmsListItem)iter.next();

         try {
            CmsReplicationServer server = CmsReplicationManager.getInstance().getConfiguration().getReplicationServer(new CmsUUID(item.getId()));
            if (detailId.equals("dd")) {
               StringBuffer html = new StringBuffer(512);
               html.append(server.getDescription());
               item.set(detailId, html.toString());
            } else if (detailId.equals("ds")) {
               Iterator itSources = server.getSources().iterator();

               StringBuffer html;
               for(html = new StringBuffer(512); itSources.hasNext(); html.append("\n")) {
                  html.append(itSources.next());
                  if (itSources.hasNext()) {
                     html.append("<br>");
                  }
               }

               item.set(detailId, html.toString());
            }
         } catch (Exception var7) {
         }
      }

   }

   protected List getListItems() {
      List ret = new ArrayList();
      Iterator itServers = CmsReplicationManager.getInstance().getConfiguration().getReplicationServersAsList().iterator();

      while(itServers.hasNext()) {
         CmsReplicationServer server = (CmsReplicationServer)itServers.next();
         CmsListItem item = this.getList().newItem(server.getServerId().toString());
         item.set("cn", server.getName());
         item.set("cos", server.getOrgServerName());
         item.set("cds", server.getDestServerName());
         item.set("cms", server.getMode());
         ret.add(item);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition fullReplicateCol = new CmsListColumnDefinition("crrf");
      fullReplicateCol.setName(Messages.get().container("GUI_REPLICATION_LIST_COLS_REPLICATE_FULL_0"));
      fullReplicateCol.setWidth("20");
      fullReplicateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      fullReplicateCol.setSorteable(false);
      CmsListDirectAction fullReplicateAction = new CmsListDirectAction("arrf");
      fullReplicateAction.setName(Messages.get().container("GUI_REPLICATION_LIST_ACTION_REPLICATE_FULL_NAME_0"));
      fullReplicateAction.setHelpText(Messages.get().container("GUI_REPLICATION_LIST_ACTION_REPLICATE_FULL_HELP_0"));
      fullReplicateAction.setIconPath("tools/ocee-replication/buttons/full_replication.png");
      fullReplicateCol.addDirectAction(fullReplicateAction);
      metadata.addColumn(fullReplicateCol);
      CmsListColumnDefinition checkCol = new CmsListColumnDefinition("ccr");
      checkCol.setName(Messages.get().container("GUI_REPLICATION_LIST_COLS_CHECK_0"));
      checkCol.setWidth("20");
      checkCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      checkCol.setSorteable(false);
      CmsListDirectAction checkResourcesAction = new CmsListDirectAction("acr");
      checkResourcesAction.setName(Messages.get().container("GUI_REPLICATION_LIST_ACTION_CHECK_NAME_0"));
      checkResourcesAction.setHelpText(Messages.get().container("GUI_REPLICATION_LIST_ACTION_CHECK_HELP_0"));
      checkResourcesAction.setIconPath("tools/ocee-replication/buttons/browse.png");
      checkCol.addDirectAction(checkResourcesAction);
      metadata.addColumn(checkCol);
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("csr");
      iconCol.setName(Messages.get().container("GUI_SERVERS_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction showResourcesAction = new CmsListDirectAction("asr") {
         public boolean isVisible() {
            if (this.getItem() == null) {
               return super.isVisible();
            } else {
               CmsReplicationMode mode = (CmsReplicationMode)this.getItem().get("cms");
               return mode == CmsReplicationMode.MANUAL;
            }
         }
      };
      showResourcesAction.setName(Messages.get().container("GUI_REPLICATION_LIST_ACTION_SHOW_NAME_0"));
      showResourcesAction.setHelpText(Messages.get().container("GUI_REPLICATION_LIST_ACTION_SHOW_HELP_0"));
      showResourcesAction.setIconPath("tools/ocee-replication/buttons/incremental_replication.png");
      iconCol.addDirectAction(showResourcesAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition orgServerCol = new CmsListColumnDefinition("cos");
      orgServerCol.setName(Messages.get().container("GUI_REPLICATION_LIST_COLS_ORGSERVER_0"));
      orgServerCol.setWidth("25%");
      CmsListDefaultAction defShowAction = new CmsListDefaultAction("das") {
         public boolean isEnabled() {
            if (this.getItem() == null) {
               return super.isVisible();
            } else {
               CmsReplicationMode mode = (CmsReplicationMode)this.getItem().get("cms");
               return mode == CmsReplicationMode.MANUAL;
            }
         }
      };
      defShowAction.setName(Messages.get().container("GUI_REPLICATION_LIST_DEFACTION_SHOW_NAME_0"));
      defShowAction.setHelpText(Messages.get().container("GUI_REPLICATION_LIST_DEFACTION_SHOW_HELP_0"));
      orgServerCol.addDefaultAction(defShowAction);
      metadata.addColumn(orgServerCol);
      CmsListColumnDefinition destServerCol = new CmsListColumnDefinition("cds");
      destServerCol.setName(Messages.get().container("GUI_REPLICATION_LIST_COLS_DESTSERVER_0"));
      destServerCol.setWidth("25%");
      metadata.addColumn(destServerCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_REPLICATION_LIST_COLS_NAME_0"));
      nameCol.setWidth("25%");
      metadata.addColumn(nameCol);
      CmsListColumnDefinition modeCol = new CmsListColumnDefinition("cms");
      modeCol.setName(Messages.get().container("GUI_SERVERS_LIST_COLS_MODE_0"));
      modeCol.setWidth("25%");
      metadata.addColumn(modeCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails descDetails = new CmsListItemDetails("dd");
      descDetails.setAtColumn("cos");
      descDetails.setVisible(false);
      descDetails.setShowActionName(Messages.get().container("GUI_REPLICATION_DETAIL_SHOW_DESCRIPTION_NAME_0"));
      descDetails.setShowActionHelpText(Messages.get().container("GUI_REPLICATION_DETAIL_SHOW_DESCRIPTION_HELP_0"));
      descDetails.setHideActionName(Messages.get().container("GUI_REPLICATION_DETAIL_HIDE_DESCRIPTION_NAME_0"));
      descDetails.setHideActionHelpText(Messages.get().container("GUI_REPLICATION_DETAIL_HIDE_DESCRIPTION_HELP_0"));
      descDetails.setName(Messages.get().container("GUI_REPLICATION_DETAIL_DESCRIPTION_NAME_0"));
      descDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_REPLICATION_DETAIL_DESCRIPTION_NAME_0")));
      metadata.addItemDetails(descDetails);
      CmsListItemDetails sourcesDetails = new CmsListItemDetails("ds");
      sourcesDetails.setAtColumn("cos");
      sourcesDetails.setVisible(false);
      sourcesDetails.setShowActionName(Messages.get().container("GUI_SERVERS_DETAIL_SHOW_SOURCES_NAME_0"));
      sourcesDetails.setShowActionHelpText(Messages.get().container("GUI_SERVERS_DETAIL_SHOW_SOURCES_HELP_0"));
      sourcesDetails.setHideActionName(Messages.get().container("GUI_SERVERS_DETAIL_HIDE_SOURCES_NAME_0"));
      sourcesDetails.setHideActionHelpText(Messages.get().container("GUI_SERVERS_DETAIL_HIDE_SOURCES_HELP_0"));
      sourcesDetails.setName(Messages.get().container("GUI_SERVERS_DETAIL_SOURCES_NAME_0"));
      sourcesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_SERVERS_DETAIL_SOURCES_NAME_0")));
      metadata.addItemDetails(sourcesDetails);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }
}
