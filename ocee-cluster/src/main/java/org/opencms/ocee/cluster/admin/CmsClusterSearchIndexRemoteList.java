package org.opencms.ocee.cluster.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListMetadata;

public class CmsClusterSearchIndexRemoteList extends CmsClusterSearchIndexList {
   public static final String LIST_REMOTE_ID = "lcir";
   public static final String LIST_ACTION_ICON = "aic";
   public static final String LIST_COLUMN_ICON = "cic";

   public CmsClusterSearchIndexRemoteList(CmsJspActionElement jsp) {
      super(jsp, "lcir", Messages.get().container("GUI_CLUSTER_SEARCHINDEX_REMOTE_LIST_NAME_0"));
   }

   public CmsClusterSearchIndexRemoteList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
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
      super.setColumns(metadata);
      CmsListColumnDefinition nameCol = metadata.getColumnDefinition("cn");
      nameCol.setWidth("70%");
      nameCol.removeDefaultAction("asio");
      CmsListColumnDefinition rebuildCol = metadata.getColumnDefinition("cr");
      rebuildCol.setWidth("15%");
      CmsListColumnDefinition localeCol = metadata.getColumnDefinition("cl");
      localeCol.setWidth("15%");
      metadata.getColumnDefinition("cc").setVisible(false);
      metadata.getColumnDefinition("cp").setVisible(false);
      metadata.getColumnDefinition("cae").setVisible(false);
      metadata.getColumnDefinition("cis").setVisible(false);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      super.setMultiActions(metadata);
      metadata.getMultiAction("mar").setVisible(false);
   }
}
