package org.opencms.ocee.replication.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.ocee.replication.CmsReplicationServer;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsReplicationServersList extends A_CmsListDialog {
   public static final String LIST_ACTION_ICON = "ais";
   public static final String LIST_COLUMN_ICON = "cis";
   public static final String LIST_COLUMN_NAME = "cns";
   public static final String LIST_COLUMN_MODE = "cms";
   public static final String LIST_COLUMN_DEST_POOL = "cdp";
   public static final String LIST_COLUMN_ORG_POOL = "cop";
   public static final String LIST_DETAIL_SOURCES = "ds";
   public static final String LIST_ID = "lrs";
   public static final String PATH_BUTTONS = "tools/ocee-replication/buttons/";
   public static final String LIST_COLUMN_ORG_SERVER = "cos";
   public static final String LIST_COLUMN_DEST_SERVER = "cds";

   public CmsReplicationServersList(CmsJspActionElement jsp) {
      super(jsp, "lrs", Messages.get().container("GUI_SERVERS_LIST_NAME_0"), "cns", CmsListOrderEnum.ORDER_ASCENDING, (String)null);
   }

   public CmsReplicationServersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public String defaultActionHtmlStart() {
      StringBuffer result = new StringBuffer(2048);
      result.append(this.dialogContentStart(this.getParamTitle()));
      return result.toString();
   }

   public void executeListMultiActions() {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() {
      this.throwListUnsupportedActionException();
   }

   protected String defaultActionHtmlEnd() {
      return "";
   }

   protected void fillDetails(String detailId) {
      Iterator itServers = this.getList().getAllContent().iterator();

      while(itServers.hasNext()) {
         CmsListItem item = (CmsListItem)itServers.next();

         try {
            if (detailId.equals("ds")) {
               CmsReplicationServer server = CmsReplicationManager.getInstance().getConfiguration().getReplicationServer(new CmsUUID(item.getId()));
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
         item.set("cns", server.getName());
         item.set("cos", server.getOrgServerName());
         item.set("cds", server.getDestServerName());
         item.set("cop", server.getOrgServerPoolUrl());
         item.set("cdp", server.getDestServerPoolUrl());
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
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("cis");
      iconCol.setName(Messages.get().container("GUI_SERVERS_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconAction = new CmsListDirectAction("ais");
      iconAction.setName(Messages.get().container("GUI_SERVERS_LIST_ACTION_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_SERVERS_LIST_ACTION_ICON_HELP_0"));
      iconAction.setIconPath("tools/ocee-replication/buttons/server.png");
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition orgServerCol = new CmsListColumnDefinition("cos");
      orgServerCol.setName(Messages.get().container("GUI_REPLICATION_LIST_COLS_ORGSERVER_0"));
      orgServerCol.setWidth("18%");
      metadata.addColumn(orgServerCol);
      CmsListColumnDefinition destServerCol = new CmsListColumnDefinition("cds");
      destServerCol.setName(Messages.get().container("GUI_REPLICATION_LIST_COLS_DESTSERVER_0"));
      destServerCol.setWidth("18%");
      metadata.addColumn(destServerCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cns");
      nameCol.setName(Messages.get().container("GUI_SERVERS_LIST_COLS_NAME_0"));
      nameCol.setWidth("18%");
      metadata.addColumn(nameCol);
      CmsListColumnDefinition orgPoolCol = new CmsListColumnDefinition("cop");
      orgPoolCol.setName(Messages.get().container("GUI_SERVERS_LIST_COLS_ORG_POOL_0"));
      orgPoolCol.setWidth("18%");
      metadata.addColumn(orgPoolCol);
      CmsListColumnDefinition destPoolCol = new CmsListColumnDefinition("cdp");
      destPoolCol.setName(Messages.get().container("GUI_SERVERS_LIST_COLS_DEST_POOL_0"));
      destPoolCol.setWidth("18%");
      metadata.addColumn(destPoolCol);
      CmsListColumnDefinition modeCol = new CmsListColumnDefinition("cms");
      modeCol.setName(Messages.get().container("GUI_SERVERS_LIST_COLS_MODE_0"));
      modeCol.setWidth("10%");
      metadata.addColumn(modeCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails sourcesDetails = new CmsListItemDetails("ds");
      sourcesDetails.setAtColumn("cns");
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
