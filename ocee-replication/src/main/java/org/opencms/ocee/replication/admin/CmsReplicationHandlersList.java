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
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.ocee.replication.I_CmsReplicationHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsReplicationHandlersList extends A_CmsListDialog {
   public static final int ACTION_SETTINGS = 141;
   public static final String LIST_ACTION_ICON = "ai";
   public static final String LIST_COLUMN_CLASS = "cc";
   public static final String LIST_COLUMN_ICON = "ci";
   public static final String LIST_COLUMN_NAME = "cn";
   public static final String LIST_ID = "lrh";
   public static final String PATH_BUTTONS = "tools/ocee-replication/buttons/";
   public static final String SETTINGS_ACTION = "settings";

   public CmsReplicationHandlersList(CmsJspActionElement jsp) {
      super(jsp, "lrh", Messages.get().container("GUI_HANDLERS_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, (String)null);
   }

   public CmsReplicationHandlersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionDialog() throws JspException, ServletException, IOException {
      switch(this.getAction()) {
      case 141:
         Map params = new HashMap();
         params.put("action", "initial");
         params.put("style", "new");
         this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-replication/export-settings.jsp", params);
         break;
      default:
         super.actionDialog();
      }

   }

   public void executeListMultiActions() {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() {
      this.throwListUnsupportedActionException();
   }

   protected String customHtmlStart() {
      StringBuffer result = new StringBuffer(512);
      result.append(this.dialogBlockStart(Messages.get().container("GUI_REPLICATION_EXPORT_BLOCK_0").key(this.getLocale())));
      result.append("<table><tr><td>");
      result.append("<form name='actions' method='post' action='");
      result.append(this.getDialogRealUri());
      result.append("' class='nomargin' onsubmit=\"return submitAction('ok', null, 'actions');\">\n");
      result.append(this.allParamsAsHidden());
      result.append("<input name='");
      result.append("settings");
      result.append("' type='button' value='");
      result.append(Messages.get().container("GUI_REPLICATION_SETTINGS_EDIT_0").key(this.getLocale()));
      result.append("' onclick=\"submitAction('");
      result.append("settings");
      result.append("', form);\" class='dialogbutton'>\n");
      result.append("</form>\n");
      result.append("</td><td>");
      String exportFolder = CmsReplicationManager.getInstance().getConfiguration().getExportFolder();
      if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(exportFolder)) {
         result.append(Messages.get().container("GUI_REPLICATION_EXPORT_ENABLED_1", exportFolder).key(this.getLocale()));
      } else {
         result.append(Messages.get().container("GUI_REPLICATION_EXPORT_DISABLED_0").key(this.getLocale()));
      }

      result.append("</td></tr></table>\n");
      result.append(this.dialogBlockEnd());
      return result.toString();
   }

   protected String defaultActionHtmlEnd() {
      return "";
   }

   protected void fillDetails(String detailId) {
   }

   protected List getListItems() {
      List ret = new ArrayList();
      List handlers = CmsReplicationManager.getInstance().getConfiguration().getReplicationHandlers();
      Iterator itHandlers = handlers.iterator();

      while(itHandlers.hasNext()) {
         I_CmsReplicationHandler handler = (I_CmsReplicationHandler)itHandlers.next();
         CmsListItem item = this.getList().newItem(handler.getClass().getName());
         item.set("cn", handler.getName().key(this.getLocale()));
         item.set("cc", handler.getClass().getName());
         ret.add(item);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      super.initWorkplaceRequestValues(settings, request);
      if ("settings".equals(this.getParamAction())) {
         this.setAction(141);
      }

   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_HANDLERS_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconAction = new CmsListDirectAction("ai");
      iconAction.setName(Messages.get().container("GUI_HANDLERS_LIST_ACTION_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_HANDLERS_LIST_ACTION_ICON_HELP_0"));
      iconAction.setIconPath("tools/ocee-replication/buttons/handler.png");
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_HANDLERS_LIST_COLS_NAME_0"));
      nameCol.setWidth("50%");
      metadata.addColumn(nameCol);
      CmsListColumnDefinition classCol = new CmsListColumnDefinition("cc");
      classCol.setName(Messages.get().container("GUI_HANDLERS_LIST_COLS_CLASS_0"));
      classCol.setWidth("50%");
      metadata.addColumn(classCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }
}
