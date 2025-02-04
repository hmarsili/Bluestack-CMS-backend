package org.opencms.ocee.replication.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsReplicationSourcesList extends A_CmsListDialog {
   public static final String LIST_ACTION_ICON = "air";
   public static final String LIST_COLUMN_ICON = "cir";
   public static final String LIST_COLUMN_NAME = "cnr";
   public static final String LIST_DETAIL_RESOURCES = "dr";
   public static final String LIST_ID = "lrso";
   public static final String PATH_BUTTONS = "tools/ocee-replication/buttons/";

   public CmsReplicationSourcesList(CmsJspActionElement jsp) {
      super(jsp, "lrso", Messages.get().container("GUI_SOURCES_LIST_NAME_0"), "cnr", CmsListOrderEnum.ORDER_ASCENDING, (String)null);
   }

   public CmsReplicationSourcesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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

   protected void fillDetails(String detailId) {
      Iterator itServers = this.getList().getAllContent().iterator();

      while(itServers.hasNext()) {
         CmsListItem item = (CmsListItem)itServers.next();

         try {
            if (detailId.equals("dr")) {
               Iterator itResources = ((List)CmsReplicationManager.getInstance().getConfiguration().getReplicationSources().get(item.getId())).iterator();

               StringBuffer html;
               for(html = new StringBuffer(512); itResources.hasNext(); html.append("\n")) {
                  html.append(itResources.next());
                  if (itResources.hasNext()) {
                     html.append("<br>");
                  }
               }

               item.set(detailId, html.toString());
            }
         } catch (Exception var6) {
         }
      }

   }

   protected List getListItems() {
      List ret = new ArrayList();
      Iterator itSources = CmsReplicationManager.getInstance().getConfiguration().getReplicationSources().keySet().iterator();

      while(itSources.hasNext()) {
         String source = (String)itSources.next();
         CmsListItem item = this.getList().newItem(source);
         item.set("cnr", source);
         ret.add(item);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("cir");
      iconCol.setName(Messages.get().container("GUI_SOURCES_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconAction = new CmsListDirectAction("air");
      iconAction.setName(Messages.get().container("GUI_SOURCES_LIST_ACTION_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_SOURCES_LIST_ACTION_ICON_HELP_0"));
      iconAction.setIconPath("tools/ocee-replication/buttons/source.png");
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cnr");
      nameCol.setName(Messages.get().container("GUI_SOURCES_LIST_COLS_NAME_0"));
      nameCol.setWidth("100%");
      metadata.addColumn(nameCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails sourcesDetails = new CmsListItemDetails("dr");
      sourcesDetails.setAtColumn("cnr");
      sourcesDetails.setVisible(false);
      sourcesDetails.setShowActionName(Messages.get().container("GUI_SOURCES_DETAIL_SHOW_RESOURCES_NAME_0"));
      sourcesDetails.setShowActionHelpText(Messages.get().container("GUI_SOURCES_DETAIL_SHOW_RESOURCES_HELP_0"));
      sourcesDetails.setHideActionName(Messages.get().container("GUI_SOURCES_DETAIL_HIDE_RESOURCES_NAME_0"));
      sourcesDetails.setHideActionHelpText(Messages.get().container("GUI_SOURCES_DETAIL_HIDE_RESOURCES_HELP_0"));
      sourcesDetails.setName(Messages.get().container("GUI_SOURCES_DETAIL_RESOURCES_NAME_0"));
      sourcesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_SOURCES_DETAIL_RESOURCES_NAME_0")));
      metadata.addItemDetails(sourcesDetails);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }
}
