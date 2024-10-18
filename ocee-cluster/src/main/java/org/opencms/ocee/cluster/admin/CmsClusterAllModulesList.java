package org.opencms.ocee.cluster.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.module.CmsModule;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;

public class CmsClusterAllModulesList extends CmsClusterModulesList {
   public static final String LIST_COLUMN_SERVER = "csv";

   public CmsClusterAllModulesList(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterAllModulesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() throws IOException, ServletException {
      if (this.getParamListAction().equals("md")) {
         List localModules = new ArrayList();
         List remoteModules = new ArrayList();
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            if (this.getServer((String)listItem.get("csv")).isWpServer()) {
               localModules.add(listItem.get("cn"));
            } else {
               remoteModules.add(listItem);
            }
         }

         Iterator itRemotes = remoteModules.iterator();

         while(itRemotes.hasNext()) {
            CmsListItem listItem = (CmsListItem)itRemotes.next();
            CmsClusterRemoteCmdHelper.deleteModules(this.getCms(), this.getServer((String)listItem.get("csv")), Collections.singletonList(listItem.get("cn")));
         }

         if (!localModules.isEmpty()) {
            Map params = new HashMap();
            params.put("module", localModules.toString().substring(1, localModules.toString().length() - 1));
            params.put("action", "initial");
            params.put("style", "new");
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/modules/reports/delete.jsp", params);
         }
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public void executeListSingleActions() throws IOException, ServletException {
      String module = (String)this.getSelectedItem().get("cn");
      if (!this.getParamListAction().equals("ae") && !this.getParamListAction().equals("ao")) {
         if (this.getParamListAction().equals("ad")) {
            CmsClusterServer server = this.getServer((String)this.getSelectedItem().get("csv"));
            if (!server.isWpServer()) {
               CmsClusterRemoteCmdHelper.deleteModules(this.getCms(), server, Collections.singletonList(module));
            } else {
               Map params = new HashMap();
               params.put("module", module);
               params.put("action", "initial");
               params.put("style", "new");
               this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/modules/reports/delete.jsp", params);
            }
         } else {
            this.throwListUnsupportedActionException();
         }
      } else {
         Map params = new HashMap();
         params.put("module", module);
         params.put("server", this.getSelectedItem().get("csv"));
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/editparameters", params);
      }

      this.listSave();
   }

   protected void fillDetails(String detailId) {
      CmsClusterServer server = CmsClusterManager.getInstance().getThisServer();
      this.fillDetails(detailId, server);
      Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

      while(it.hasNext()) {
         CmsClusterServer srv = (CmsClusterServer)it.next();
         this.fillDetails(detailId, srv);
      }

   }

   protected List getListItems() {
      List ret = new ArrayList();
      CmsClusterServer server = CmsClusterManager.getInstance().getThisServer();
      ret.addAll(this.getListItems(server));
      Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

      while(it.hasNext()) {
         CmsClusterServer srv = (CmsClusterServer)it.next();
         ret.addAll(this.getListItems(srv));
      }

      return ret;
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

   protected void validateParamaters() throws Exception {
   }

   private void fillDetails(String detailId, CmsClusterServer server) {
      Map data;
      if (detailId.equals("da")) {
         data = CmsClusterRemoteCmdHelper.getModuleAuthorInfos(this.getCms(), server);
      } else if (detailId.equals("resourcestinfo")) {
         data = CmsClusterRemoteCmdHelper.getModuleResources(this.getCms(), server);
      } else if (detailId.equals("dd")) {
         data = CmsClusterRemoteCmdHelper.getModuleDependencies(this.getCms(), server);
      } else if (detailId.equals("restypesinfo")) {
         data = CmsClusterRemoteCmdHelper.getModuleResourceTypes(this.getCms(), server);
      } else {
         if (!detailId.equals("ldp")) {
            return;
         }

         data = CmsClusterRemoteCmdHelper.getModuleParameters(this.getCms(), server, (String)null);
      }

      List modules = CmsClusterRemoteCmdHelper.getModules(this.getCms(), server);
      Iterator i = modules.iterator();
      CmsMessages messages = org.opencms.workplace.tools.modules.Messages.get().getBundle(this.getLocale());

      while(true) {
         CmsModule module;
         StringBuffer html;
         label85:
         while(true) {
            if (!i.hasNext()) {
               return;
            }

            module = (CmsModule)i.next();
            String moduleName = module.getName();
            html = new StringBuffer(32);
            if (detailId.equals("da")) {
               html.append(data.get(moduleName + ".authorName"));
               html.append("&nbsp;(");
               html.append(data.get(moduleName + ".authorEmail"));
               html.append(")");
               break;
            }

            if (detailId.equals("resourcestinfo")) {
               Iterator j = CmsStringUtil.splitAsList((String)data.get(moduleName + ".resources"), ",", true).iterator();

               while(true) {
                  if (!j.hasNext()) {
                     break label85;
                  }

                  String resource = (String)j.next();
                  html.append(resource);
                  html.append("<br>");
               }
            }

            List resNames;
            List resIds;
            int j;
            if (detailId.equals("dd")) {
               resNames = CmsStringUtil.splitAsList((String)data.get(moduleName + ".dependencies.name"), ",", true);
               resIds = CmsStringUtil.splitAsList((String)data.get(moduleName + ".dependencies.version"), ",", true);
               j = 0;

               while(true) {
                  if (j >= resNames.size()) {
                     break label85;
                  }

                  html.append(resNames.get(j));
                  html.append("&nbsp;Version:");
                  html.append(resIds.get(j));
                  html.append("<br>");
                  ++j;
               }
            }

            if (detailId.equals("ldp")) {
               resNames = CmsStringUtil.splitAsList((String)data.get(moduleName + ".parameters.key"), ",", true);
               resIds = CmsStringUtil.splitAsList((String)data.get(moduleName + ".parameters.value"), ",", true);
               j = 0;

               while(true) {
                  if (j >= resNames.size()) {
                     break label85;
                  }

                  html.append(resNames.get(j));
                  html.append("=");
                  html.append(resIds.get(j));
                  html.append("<br>");
                  ++j;
               }
            }

            if (detailId.equals("restypesinfo")) {
               resNames = CmsStringUtil.splitAsList((String)data.get(moduleName + ".resourceTypes.typeName"), ",", true);
               resIds = CmsStringUtil.splitAsList((String)data.get(moduleName + ".resourceTypes.typeId"), ",", true);

               for(j = 0; j < resNames.size(); ++j) {
                  html.append(messages.key("GUI_MODULES_LABEL_RESTYPES_DETAIL_0"));
                  html.append(":&nbsp;");
                  html.append(resNames.get(j));
                  html.append("&nbsp;ID:");
                  html.append(resIds.get(j));
                  html.append("<br>");
               }

               List expNames = CmsStringUtil.splitAsList((String)data.get(moduleName + ".explorerTypes.name"), ",", true);
               List expRefs = CmsStringUtil.splitAsList((String)data.get(moduleName + ".explorerTypes.reference"), ",", true);
               j = 0;

               while(true) {
                  if (j >= expNames.size()) {
                     break label85;
                  }

                  html.append(messages.key("GUI_MODULES_LABEL_EXPLORERSETTINGSS_DETAIL_0"));
                  html.append(":&nbsp;");
                  html.append(expNames.get(j));
                  html.append("&nbsp;(");
                  html.append(expRefs.get(j));
                  html.append(")<br>");
                  ++j;
               }
            }
         }

         this.getList().getItem(server.getName() + module.getName()).set(detailId, html.toString());
      }
   }

   private List getListItems(CmsClusterServer server) {
      List ret = new ArrayList();
      List modules = CmsClusterRemoteCmdHelper.getModules(this.getCms(), server);
      Iterator i = modules.iterator();

      while(i.hasNext()) {
         CmsModule module = (CmsModule)i.next();
         CmsListItem item = this.getList().newItem(server.getName() + module.getName());
         item.set("cn", module.getName());
         item.set("cc", module.getNiceName());
         item.set("cv", module.getVersion());
         item.set("cg", module.getGroup());
         item.set("csv", server.getName());
         ret.add(item);
      }

      return ret;
   }

   private CmsClusterServer getServer(String serverName) {
      return CmsClusterManager.getInstance().getServer(serverName);
   }
}
