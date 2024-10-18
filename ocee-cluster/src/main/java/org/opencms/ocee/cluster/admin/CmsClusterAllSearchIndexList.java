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
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;

public class CmsClusterAllSearchIndexList extends CmsClusterSearchIndexList {
   public static final String LIST_COLUMN_SERVER = "csv";

   public CmsClusterAllSearchIndexList(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterAllSearchIndexList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      if (this.getParamListAction().equals("mad")) {
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            CmsClusterRemoteCmdHelper.deleteSearchIndex(this.getCms(), this.getServer((String)listItem.get("csv")), (String)listItem.get("cn"));
         }
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {
      String index = (String)this.getSelectedItem().get("cn");
      Map params = new HashMap();
      params.put("server", this.getSelectedItem().get("csv"));
      String action = this.getParamListAction();
      if (action.equals("ad")) {
         CmsClusterRemoteCmdHelper.deleteSearchIndex(this.getCms(), this.getServer((String)this.getSelectedItem().get("csv")), index);
      } else if (action.equals("ae")) {
         params.put("style", "new");
         params.put("indexname", index);
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/singleindex/edit", params);
      } else if (action.equals("asio")) {
         params.put("action", "initial");
         params.put("style", "new");
         params.put("indexname", index);
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/singleindex", params);
      } else if (action.equals("ais")) {
         params.put("action", "initial");
         params.put("style", "new");
         params.put("indexname", index);
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/singleindex/indexsources", params);
      }

      this.listSave();
   }

   protected void fillDetails(String detailId) {
      List items = this.getList().getAllContent();
      Iterator itItems = items.iterator();

      while(true) {
         while(itItems.hasNext()) {
            CmsListItem item = (CmsListItem)itItems.next();
            String idxName = (String)item.get("cn");
            CmsSearchIndex idx = CmsClusterRemoteCmdHelper.getSearchIndex(this.getCms(), this.getServer((String)item.get("csv")), idxName);
            if (detailId.equals("di")) {
               List sources = new ArrayList();
               List allSrcs = CmsClusterRemoteCmdHelper.getIndexSources(this.getCms(), this.getServer((String)item.get("csv")));
               Iterator it = allSrcs.iterator();

               while(it.hasNext()) {
                  CmsSearchIndexSource src = (CmsSearchIndexSource)it.next();
                  if (idx.getSourceNames().contains(src.getName())) {
                     sources.add(src);
                  }
               }

               item.set(detailId, this.fillDetailIndexSource(sources));
            } else if (detailId.equals("df")) {
               item.set(detailId, this.fillDetailFieldConfiguration(idx));
            }
         }

         return;
      }
   }

   protected List getListItems() {
      List items = new ArrayList();
      List servers = new ArrayList(CmsClusterManager.getInstance().getOtherServers());
      servers.add(CmsClusterManager.getInstance().getThisServer());
      Iterator itServers = servers.iterator();

      while(itServers.hasNext()) {
         CmsClusterServer server = (CmsClusterServer)itServers.next();
         List indexes = CmsClusterRemoteCmdHelper.getSearchIndexes(this.getCms(), server);
         Iterator itIndexes = indexes.iterator();

         while(itIndexes.hasNext()) {
            CmsSearchIndex index = (CmsSearchIndex)itIndexes.next();
            CmsListItem item = this.getList().newItem(index.getName() + server.getName());
            item.set("cn", index.getName());
            item.set("cc", index.getFieldConfiguration().getName());
            item.set("cr", index.getRebuildMode());
            item.set("cp", index.getProject());
            item.set("cl", index.getLocale().toString());
            item.set("csv", server.getName());
            items.add(item);
         }
      }

      return items;
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setListItemComparator(new CmsListItemActionIconComparator());
      CmsListDirectAction iconAction = new CmsListDirectAction("ai") {
         public String getIconPath() {
            return CmsClusterManager.getInstance().getWpServer().getName().equals(this.getItem().get("csv")) ? "tools/ocee-cluster/buttons/wpserver.png" : "tools/ocee-cluster/buttons/server.png";
         }

         public CmsMessageContainer getName() {
            return CmsClusterManager.getInstance().getWpServer().getName().equals(this.getItem().get("csv")) ? Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_WP_NAME_0") : Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_NAME_0");
         }
      };
      iconAction.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_HELP_0"));
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      super.setColumns(metadata);
      CmsListColumnDefinition serverCol = new CmsListColumnDefinition("csv");
      serverCol.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_COLS_SERVER_0"));
      serverCol.setWidth("10%");
      metadata.addColumn(serverCol);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      super.setMultiActions(metadata);
      metadata.getMultiAction("mar").setVisible(false);
   }

   protected void validateParamaters() throws Exception {
   }

   private CmsClusterServer getServer(String serverName) {
      return CmsClusterManager.getInstance().getServer(serverName);
   }
}
