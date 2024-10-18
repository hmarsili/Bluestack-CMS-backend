package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsClusterModulesLocalList extends A_CmsListDialog {
   public static final String LIST_ACTION_COPY = "ac";
   public static final String LIST_ACTION_ICON = "aic";
   public static final String LIST_COLUMN_COPY = "cc";
   public static final String LIST_COLUMN_ICON = "cic";
   public static final String LIST_DEFACTION_COPY = "dac";
   public static final String LIST_ID = "lcml";
   public static final String LIST_MACTION_COPY = "mac";
   private String m_paramServer;

   public CmsClusterModulesLocalList(CmsJspActionElement jsp) {
      this(jsp, "lcml");
   }

   public CmsClusterModulesLocalList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsClusterModulesLocalList(CmsJspActionElement jsp, String listId) {
      super(jsp, listId, Messages.get().container("GUI_CLUSTER_MODULES_LOCAL_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void executeListMultiActions() {
      if (this.getParamListAction().equals("mac")) {
         List modules = new ArrayList();
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            modules.add(OpenCms.getModuleManager().getModule(listItem.getId()));
         }

         CmsClusterRemoteCmdHelper.createModules(this.getCms(), this.getServer(), modules);
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public void executeListSingleActions() {
      if (!this.getParamListAction().equals("ac") && !this.getParamListAction().equals("dac")) {
         this.throwListUnsupportedActionException();
      } else {
         String module = this.getSelectedItem().getId();
         CmsClusterRemoteCmdHelper.createModules(this.getCms(), this.getServer(), Collections.singletonList(OpenCms.getModuleManager().getModule(module)));
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
      List moduleNames = this.getList().getAllContent();
      Iterator i = moduleNames.iterator();
      CmsMessages messages = org.opencms.workplace.tools.modules.Messages.get().getBundle(this.getLocale());

      while(true) {
         CmsListItem item;
         StringBuffer html;
         label76:
         while(true) {
            if (!i.hasNext()) {
               return;
            }

            item = (CmsListItem)i.next();
            String moduleName = item.getId();
            CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
            html = new StringBuffer(32);
            if (detailId.equals("da")) {
               html.append(module.getAuthorName());
               html.append("&nbsp;(");
               html.append(module.getAuthorEmail());
               html.append(")");
               break;
            }

            Iterator k;
            if (detailId.equals("resourcestinfo")) {
               k = module.getResources().iterator();

               while(true) {
                  if (!k.hasNext()) {
                     break label76;
                  }

                  String resource = (String)k.next();
                  html.append(resource);
                  html.append("<br>");
               }
            }

            if (detailId.equals("ldp")) {
               k = module.getParameters().entrySet().iterator();

               while(true) {
                  if (!k.hasNext()) {
                     break label76;
                  }

                  Entry entry = (Entry)k.next();
                  html.append(entry.getKey());
                  html.append("=");
                  html.append(entry.getValue());
                  html.append("<br>");
               }
            }

            if (detailId.equals("dd")) {
               k = module.getDependencies().iterator();

               while(true) {
                  if (!k.hasNext()) {
                     break label76;
                  }

                  CmsModuleDependency dep = (CmsModuleDependency)k.next();
                  html.append(dep.getName());
                  html.append("&nbsp;Version:");
                  html.append(dep.getVersion());
                  html.append("<br>");
               }
            }

            if (detailId.equals("restypesinfo")) {
               StringBuffer restypes = new StringBuffer(32);
               Iterator l = module.getResourceTypes().iterator();
               boolean addRestypes = false;

               while(l.hasNext()) {
                  addRestypes = true;
                  I_CmsResourceType resourceType = (I_CmsResourceType)l.next();
                  restypes.append(messages.key("GUI_MODULES_LABEL_RESTYPES_DETAIL_0"));
                  restypes.append(":&nbsp;");
                  restypes.append(resourceType.getTypeName());
                  restypes.append("&nbsp;ID:");
                  restypes.append(resourceType.getTypeId());
                  restypes.append("<br>");
               }

               StringBuffer explorersettings = new StringBuffer(32);
               Iterator m = module.getExplorerTypes().iterator();
               boolean addExplorersettings = false;

               while(m.hasNext()) {
                  addExplorersettings = true;
                  CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)m.next();
                  explorersettings.append(messages.key("GUI_MODULES_LABEL_EXPLORERSETTINGSS_DETAIL_0"));
                  explorersettings.append(":&nbsp;");
                  explorersettings.append(settings.getName());
                  explorersettings.append("&nbsp;(");
                  explorersettings.append(settings.getReference());
                  explorersettings.append(")<br>");
               }

               if (addRestypes) {
                  html.append(restypes);
               }

               if (addExplorersettings) {
                  html.append(explorersettings);
               }
               break;
            }
         }

         item.set(detailId, html.toString());
      }
   }

   protected List getListItems() {
      List ret = new ArrayList();
      Set moduleNames = OpenCms.getModuleManager().getModuleNames();
      Iterator i = moduleNames.iterator();

      while(i.hasNext()) {
         String moduleName = (String)i.next();
         CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
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
      CmsListColumnDefinition copyCol = new CmsListColumnDefinition("cc");
      copyCol.setName(Messages.get().container("GUI_MODULES_LIST_COL_COPY_0"));
      copyCol.setHelpText(Messages.get().container("GUI_MODULES_LIST_COL_COPY_HELP_0"));
      copyCol.setWidth("20");
      copyCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      copyCol.setSorteable(false);
      metadata.addColumn(copyCol);
      CmsListDirectAction copyAction = new CmsListDirectAction("ac");
      copyAction.setName(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_NAME_0"));
      copyAction.setHelpText(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_HELP_0"));
      copyAction.setConfirmationMessage(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_CONF_0"));
      copyAction.setIconPath("list/add.png");
      copyCol.addDirectAction(copyAction);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(org.opencms.workplace.tools.modules.Messages.get().container("GUI_MODULES_LIST_COLS_NAME_0"));
      nameCol.setWidth("70%");
      nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      CmsListDefaultAction nameColAction = new CmsListDefaultAction("dac");
      nameColAction.setName(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_NAME_0"));
      nameColAction.setHelpText(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_HELP_0"));
      nameColAction.setConfirmationMessage(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_CONF_0"));
      nameCol.addDefaultAction(nameColAction);
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
      CmsListMultiAction copyModule = new CmsListMultiAction("mac");
      copyModule.setName(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_NAME_0"));
      copyModule.setHelpText(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_HELP_0"));
      copyModule.setHelpText(Messages.get().container("GUI_MODULES_LIST_ACTION_COPY_CONF_0"));
      copyModule.setIconPath("tools/scheduler/buttons/copy.png");
      metadata.addMultiAction(copyModule);
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
