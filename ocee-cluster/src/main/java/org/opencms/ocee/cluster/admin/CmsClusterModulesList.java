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
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.module.CmsModule;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.tools.modules.CmsModulesList;
import org.opencms.workplace.tools.modules.CmsModulesListGroupFormatter;

public class CmsClusterModulesList extends CmsModulesList {
   public static final String LIST_DETAIL_PARAMETERS = "ldp";
   private String m_paramServer;

   public CmsClusterModulesList(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterModulesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() throws IOException, ServletException {
      if (this.getParamListAction().equals("md")) {
         List names = new ArrayList();
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            names.add(listItem.getId());
         }

         if (!this.getServer().isWpServer()) {
            CmsClusterRemoteCmdHelper.deleteModules(this.getCms(), this.getServer(), names);
         } else {
            Map params = new HashMap();
            params.put("module", names.toString().substring(1, names.toString().length() - 1));
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
      String module = this.getSelectedItem().getId();
      HashMap params;
      if (!this.getParamListAction().equals("ae") && !this.getParamListAction().equals("ao")) {
         if (this.getParamListAction().equals("ad")) {
            if (!this.getServer().isWpServer()) {
               CmsClusterRemoteCmdHelper.deleteModules(this.getCms(), this.getServer(), Collections.singletonList(module));
            } else {
               params = new HashMap();
               params.put("module", module);
               params.put("action", "initial");
               params.put("style", "new");
               this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/modules/reports/delete.jsp", params);
            }
         } else {
            this.throwListUnsupportedActionException();
         }
      } else {
         params = new HashMap();
         params.put("module", module);
         params.put("action", "initial");
         params.put("server", this.getParamServer());
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/editparameters", params);
      }

      this.listSave();
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void fillDetails(String detailId) {
      Map data;
      if (detailId.equals("da")) {
         data = CmsClusterRemoteCmdHelper.getModuleAuthorInfos(this.getCms(), this.getServer());
      } else if (detailId.equals("resourcestinfo")) {
         data = CmsClusterRemoteCmdHelper.getModuleResources(this.getCms(), this.getServer());
      } else if (detailId.equals("dd")) {
         data = CmsClusterRemoteCmdHelper.getModuleDependencies(this.getCms(), this.getServer());
      } else if (detailId.equals("restypesinfo")) {
         data = CmsClusterRemoteCmdHelper.getModuleResourceTypes(this.getCms(), this.getServer());
      } else {
         if (!detailId.equals("ldp")) {
            return;
         }

         data = CmsClusterRemoteCmdHelper.getModuleParameters(this.getCms(), this.getServer(), (String)null);
      }

      List moduleNames = this.getList().getAllContent();
      Iterator i = moduleNames.iterator();
      CmsMessages messages = org.opencms.workplace.tools.modules.Messages.get().getBundle(this.getLocale());

      while(true) {
         CmsListItem item;
         StringBuffer html;
         label85:
         while(true) {
            if (!i.hasNext()) {
               return;
            }

            item = (CmsListItem)i.next();
            html = new StringBuffer(32);
            if (detailId.equals("da")) {
               html.append(data.get(item.getId() + ".authorName"));
               html.append("&nbsp;(");
               html.append(data.get(item.getId() + ".authorEmail"));
               html.append(")");
               break;
            }

            if (detailId.equals("resourcestinfo")) {
               Iterator j = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".resources"), ",", true).iterator();

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
               resNames = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".dependencies.name"), ",", true);
               resIds = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".dependencies.version"), ",", true);
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
               resNames = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".parameters.key"), ",", true);
               resIds = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".parameters.value"), ",", true);
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
               resNames = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".resourceTypes.typeName"), ",", true);
               resIds = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".resourceTypes.typeId"), ",", true);

               for(j = 0; j < resNames.size(); ++j) {
                  html.append(messages.key("GUI_MODULES_LABEL_RESTYPES_DETAIL_0"));
                  html.append(":&nbsp;");
                  html.append(resNames.get(j));
                  html.append("&nbsp;ID:");
                  html.append(resIds.get(j));
                  html.append("<br>");
               }

               List expNames = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".explorerTypes.name"), ",", true);
               List expRefs = CmsStringUtil.splitAsList((String)data.get(item.getId() + ".explorerTypes.reference"), ",", true);
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

         item.set(detailId, html.toString());
      }
   }

   protected List getListItems() {
      List ret = new ArrayList();
      List modules = CmsClusterRemoteCmdHelper.getModules(this.getCms(), this.getServer());
      Iterator i = modules.iterator();

      while(i.hasNext()) {
         CmsModule module = (CmsModule)i.next();
         CmsListItem item = this.getList().newItem(module.getName());
         item.set("cn", module.getName());
         item.set("cc", module.getNiceName());
         item.set("cv", module.getVersion());
         item.set("cg", module.getGroup());
         ret.add(item);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition viewCol = new CmsListColumnDefinition("ce");
      viewCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_EDIT_0"));
      viewCol.setWidth("20");
      viewCol.setSorteable(false);
      viewCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      CmsListDirectAction viewAction = new CmsListDirectAction("ae");
      viewAction.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_EDITMODULEPARAMETERS_ADMIN_TOOL_NAME_0"));
      viewAction.setHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_EDITMODULEPARAMETERS_ADMIN_TOOL_HELP_0"));
      viewAction.setIconPath("tools/modules/buttons/modules.png");
      viewCol.addDirectAction(viewAction);
      metadata.addColumn(viewCol);
      CmsListColumnDefinition delCol = new CmsListColumnDefinition("cd");
      delCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_DELETE_0"));
      delCol.setWidth("20");
      delCol.setSorteable(false);
      delCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      CmsListDirectAction delModule = new CmsListDirectAction("ad");
      delModule.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_ACTION_DELETE_NAME_0"));
      delModule.setConfirmationMessage(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_ACTION_DELETE_CONF_0"));
      delModule.setIconPath("list/delete.png");
      delModule.setEnabled(true);
      delModule.setHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_ACTION_DELETE_HELP_0"));
      delCol.addDirectAction(delModule);
      metadata.addColumn(delCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_NAME_0"));
      nameCol.setWidth("30%");
      nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      CmsListDefaultAction nameColAction = new CmsListDefaultAction("ao");
      nameColAction.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_EDITMODULEPARAMETERS_ADMIN_TOOL_NAME_0"));
      nameColAction.setHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_EDITMODULEPARAMETERS_ADMIN_TOOL_HELP_0"));
      nameCol.addDefaultAction(nameColAction);
      metadata.addColumn(nameCol);
      CmsListColumnDefinition nicenameCol = new CmsListColumnDefinition("cc");
      nicenameCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_NICENAME_0"));
      nicenameCol.setWidth("50%");
      nicenameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      metadata.addColumn(nicenameCol);
      CmsListColumnDefinition groupCol = new CmsListColumnDefinition("cg");
      groupCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_GROUP_0"));
      groupCol.setWidth("10%");
      groupCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      CmsModulesListGroupFormatter groupFormatter = new CmsModulesListGroupFormatter();
      groupCol.setFormatter(groupFormatter);
      metadata.addColumn(groupCol);
      CmsListColumnDefinition versionCol = new CmsListColumnDefinition("cv");
      versionCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_VERSION_0"));
      versionCol.setWidth("10%");
      versionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      metadata.addColumn(versionCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      super.setIndependentActions(metadata);
      CmsListItemDetails modulesParameterDetails = new CmsListItemDetails("ldp");
      modulesParameterDetails.setAtColumn("cn");
      modulesParameterDetails.setVisible(false);
      modulesParameterDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_MODULES_LABEL_PARAMETERS_0")));
      modulesParameterDetails.setShowActionName(Messages.get().container("GUI_MODULES_DETAIL_SHOW_PARAMETERS_NAME_0"));
      modulesParameterDetails.setShowActionHelpText(Messages.get().container("GUI_MODULES_DETAIL_SHOW_PARAMETERS_HELP_0"));
      modulesParameterDetails.setHideActionName(Messages.get().container("GUI_MODULES_DETAIL_HIDE_PARAMETERS_NAME_0"));
      modulesParameterDetails.setHideActionHelpText(Messages.get().container("GUI_MODULES_DETAIL_HIDE_PARAMETERS_HELP_0"));
      modulesParameterDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_MODULES_LABEL_PARAMETERS_0")));
      metadata.addItemDetails(modulesParameterDetails);
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (this.getServer() == null) {
         throw new Exception();
      }
   }

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }
}
