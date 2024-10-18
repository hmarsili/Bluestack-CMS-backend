package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsClusterModulesRemoteList extends A_CmsListDialog {
   public static final String LIST_ACTION_DELETE = "ad";
   public static final String LIST_ACTION_ICON = "aic";
   public static final String LIST_COLUMN_DELETE = "cd";
   public static final String LIST_COLUMN_ICON = "cic";
   public static final String LIST_ID = "lcmr";
   public static final String LIST_MACTION_DELETE = "md";
   private String m_paramServer;

   public CmsClusterModulesRemoteList(CmsJspActionElement jsp) {
      this(jsp, "lcmr");
   }

   public CmsClusterModulesRemoteList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsClusterModulesRemoteList(CmsJspActionElement jsp, String listId) {
      super(jsp, listId, Messages.get().container("GUI_CLUSTER_MODULES_REMOTE_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void executeListMultiActions() {
      if (this.getParamListAction().equals("md")) {
         List ids = new ArrayList();
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            ids.add(listItem.getId());
         }

         CmsClusterRemoteCmdHelper.deleteModules(this.getCms(), this.getServer(), ids);
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public void executeListSingleActions() {
      if (this.getParamListAction().equals("ad")) {
         String module = this.getSelectedItem().getId();
         CmsClusterRemoteCmdHelper.deleteModules(this.getCms(), this.getServer(), Collections.singletonList(module));
      } else {
         this.throwListUnsupportedActionException();
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
         item.set("cv", module.getVersion());
         ret.add(item);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("cic");
      iconCol.setName(Messages.get().container("GUI_MODULES_LIST_COL_ICON_0"));
      iconCol.setHelpText(Messages.get().container("GUI_MODULES_LIST_COL_ICON_HELP_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconColAction = new CmsListDirectAction("aic");
      iconColAction.setName(Messages.get().container("GUI_MODULES_LIST_ACTION_ICON_NAME_0"));
      iconColAction.setIconPath("tools/modules/buttons/modules.png");
      iconColAction.setEnabled(false);
      iconCol.addDirectAction(iconColAction);
      metadata.addColumn(iconCol);
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
      nameCol.setWidth("70%");
      nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      metadata.addColumn(nameCol);
      CmsListColumnDefinition versionCol = new CmsListColumnDefinition("cv");
      versionCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_VERSION_0"));
      versionCol.setWidth("30%");
      versionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      metadata.addColumn(versionCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails modulesAuthorInfoDetails = new CmsListItemDetails("da");
      modulesAuthorInfoDetails.setAtColumn("cn");
      modulesAuthorInfoDetails.setVisible(false);
      modulesAuthorInfoDetails.setFormatter(new CmsListItemDetailsFormatter(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LABEL_AUTHOR_0")));
      modulesAuthorInfoDetails.setShowActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_AUTHORINFO_NAME_0"));
      modulesAuthorInfoDetails.setShowActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_AUTHORINFO_HELP_0"));
      modulesAuthorInfoDetails.setHideActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_AUTHORINFO_NAME_0"));
      modulesAuthorInfoDetails.setHideActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_AUTHORINFO_HELP_0"));
      metadata.addItemDetails(modulesAuthorInfoDetails);
      CmsListItemDetails resourcesDetails = new CmsListItemDetails("resourcestinfo");
      resourcesDetails.setAtColumn("cn");
      resourcesDetails.setVisible(false);
      resourcesDetails.setFormatter(new CmsListItemDetailsFormatter(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LABEL_RESOURCES_0")));
      resourcesDetails.setShowActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_RESOURCES_NAME_0"));
      resourcesDetails.setShowActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_RESOURCES_HELP_0"));
      resourcesDetails.setHideActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_RESOURCES_NAME_0"));
      resourcesDetails.setHideActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_RESOURCES_HELP_0"));
      metadata.addItemDetails(resourcesDetails);
      CmsListItemDetails dependenciesDetails = new CmsListItemDetails("dd");
      dependenciesDetails.setAtColumn("cn");
      dependenciesDetails.setVisible(false);
      dependenciesDetails.setFormatter(new CmsListItemDetailsFormatter(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LABEL_DEPENDENCIES_0")));
      dependenciesDetails.setShowActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_DEPENDENCIES_NAME_0"));
      dependenciesDetails.setShowActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_DEPENDENCIES_HELP_0"));
      dependenciesDetails.setHideActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_DEPENDENCIES_NAME_0"));
      dependenciesDetails.setHideActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_DEPENDENCIES_HELP_0"));
      metadata.addItemDetails(dependenciesDetails);
      CmsListItemDetails restypesDetails = new CmsListItemDetails("restypesinfo");
      restypesDetails.setAtColumn("cn");
      restypesDetails.setVisible(false);
      restypesDetails.setFormatter(new CmsListItemDetailsFormatter(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LABEL_RESTYPES_0")));
      restypesDetails.setShowActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_RESTYPES_NAME_0"));
      restypesDetails.setShowActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_SHOW_RESTYPES_HELP_0"));
      restypesDetails.setHideActionName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_RESTYPES_NAME_0"));
      restypesDetails.setHideActionHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_DETAIL_HIDE_RESTYPES_HELP_0"));
      metadata.addItemDetails(restypesDetails);
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

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction deleteModules = new CmsListMultiAction("md");
      deleteModules.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_ACTION_MDELETE_NAME_0"));
      deleteModules.setConfirmationMessage(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_ACTION_MDELETE_CONF_0"));
      deleteModules.setIconPath("list/multi_delete.png");
      deleteModules.setEnabled(true);
      deleteModules.setHelpText(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_ACTION_MDELETE_HELP_0"));
      metadata.addMultiAction(deleteModules);
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
