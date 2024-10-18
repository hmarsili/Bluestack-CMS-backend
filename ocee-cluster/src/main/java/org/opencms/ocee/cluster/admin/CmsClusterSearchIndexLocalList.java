package org.opencms.ocee.cluster.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

public class CmsClusterSearchIndexLocalList extends CmsClusterSearchIndexList {
   public static final String LIST_DEFACTION_COPY = "dac";
   public static final String LIST_LOCAL_ID = "lcil";
   public static final String LIST_MACTION_COPY = "mac";
   public static final String LIST_ACTION_COPY = "acc";
   public static final String LIST_ACTION_ICON = "aic";
   public static final String LIST_COLUMN_COPY = "ccc";
   public static final String LIST_COLUMN_ICON = "cic";

   public CmsClusterSearchIndexLocalList(CmsJspActionElement jsp) {
      super(jsp, "lcil", Messages.get().container("GUI_CLUSTER_SEARCHINDEX_LOCAL_LIST_NAME_0"));
   }

   public CmsClusterSearchIndexLocalList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      if (this.getParamListAction().equals("mac")) {
         List searchIndexes = new ArrayList();
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            searchIndexes.add(OpenCms.getSearchManager().getIndex(listItem.getId()));
         }

         CmsClusterRemoteCmdHelper.writeSearchIndexes(this.getCms(), super.getServer(), searchIndexes);
         this.listSave();
      } else {
         super.executeListMultiActions();
      }

   }

   public void executeListSingleActions() throws CmsRuntimeException, IOException, ServletException {
      String index = this.getSelectedItem().getId();
      if (!this.getParamListAction().equals("acc") && !this.getParamListAction().equals("dac")) {
         super.executeListSingleActions();
      } else {
         CmsClusterRemoteCmdHelper.writeSearchIndexes(this.getCms(), super.getServer(), Collections.singletonList(OpenCms.getSearchManager().getIndex(index)));
         this.listSave();
      }

   }

   protected CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getThisServer();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("cic");
      iconCol.setName(Messages.get().container("GUI_SEARCHINDEX_LIST_COL_ICON_0"));
      iconCol.setHelpText(Messages.get().container("GUI_SEARCHINDEX_LIST_COL_ICON_HELP_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconColAction = new CmsListDirectAction("aic");
      iconColAction.setName(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_ICON_NAME_0"));
      iconColAction.setIconPath("tools/searchindex/icons/small/searchindex.png");
      iconColAction.setEnabled(false);
      iconCol.addDirectAction(iconColAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition copyCol = new CmsListColumnDefinition("ccc");
      copyCol.setName(Messages.get().container("GUI_SEARCHINDEX_LIST_COL_COPY_0"));
      copyCol.setHelpText(Messages.get().container("GUI_SEARCHINDEX_LIST_COL_COPY_HELP_0"));
      copyCol.setWidth("20");
      copyCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      copyCol.setSorteable(false);
      metadata.addColumn(copyCol);
      CmsListDirectAction copyAction = new CmsListDirectAction("acc");
      copyAction.setName(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_NAME_0"));
      copyAction.setHelpText(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_HELP_0"));
      copyAction.setConfirmationMessage(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_CONF_0"));
      copyAction.setIconPath("list/add.png");
      copyCol.addDirectAction(copyAction);
      super.setColumns(metadata);
      CmsListColumnDefinition nameCol = metadata.getColumnDefinition("cn");
      nameCol.setWidth("70%");
      nameCol.removeDefaultAction("asio");
      CmsListDefaultAction nameColAction = new CmsListDefaultAction("dac");
      nameColAction.setName(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_NAME_0"));
      nameColAction.setHelpText(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_HELP_0"));
      nameColAction.setConfirmationMessage(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_CONF_0"));
      nameCol.addDefaultAction(nameColAction);
      CmsListColumnDefinition rebuildCol = metadata.getColumnDefinition("cr");
      rebuildCol.setWidth("15%");
      CmsListColumnDefinition localeCol = metadata.getColumnDefinition("cl");
      localeCol.setWidth("15%");
      metadata.getColumnDefinition("cc").setVisible(false);
      metadata.getColumnDefinition("cp").setVisible(false);
      metadata.getColumnDefinition("cad").setVisible(false);
      metadata.getColumnDefinition("cae").setVisible(false);
      metadata.getColumnDefinition("cis").setVisible(false);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction copyModule = new CmsListMultiAction("mac");
      copyModule.setName(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_NAME_0"));
      copyModule.setHelpText(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_HELP_0"));
      copyModule.setHelpText(Messages.get().container("GUI_SEARCHINDEX_LIST_ACTION_COPY_CONF_0"));
      copyModule.setIconPath("tools/scheduler/buttons/copy.png");
      metadata.addMultiAction(copyModule);
   }
}
