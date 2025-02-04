package org.opencms.ocee.replication.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.ocee.replication.CmsReplicationUserData;
import org.opencms.ocee.replication.CmsReplicationUserSettings;
import org.opencms.ocee.replication.CmsReplicationUserSyncAction;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListFormatter;

public class CmsReplicationUserList extends A_CmsListDialog {
   public static final String KEY_SEPARATOR = ",";
   public static final String LIST_ACTION_DISCARD = "ad";
   public static final String LIST_ACTION_ICON = "ai";
   public static final String LIST_ACTION_SHOW = "as";
   public static final String LIST_ACTION_STATUS = "ast";
   public static final String LIST_ACTION_UPDATE = "au";
   public static final String LIST_COLUMN_DISCARD = "cd";
   public static final String LIST_COLUMN_ICON = "ci";
   public static final String LIST_COLUMN_LAST_MODIFICATION = "clm";
   public static final String LIST_COLUMN_LOGIN = "cl";
   public static final String LIST_COLUMN_NAME = "cn";
   public static final String LIST_COLUMN_SERVER = "cs";
   public static final String LIST_COLUMN_STATUS = "cst";
   public static final String LIST_COLUMN_UPDATE = "cu";
   public static final String LIST_ID = "lru";
   private static final String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = "mc";
   private static final String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = "mu";

   public CmsReplicationUserList(CmsJspActionElement jsp) {
      this("lru", jsp);
   }

   public CmsReplicationUserList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsReplicationUserList(String listId, CmsJspActionElement jsp) {
      super(jsp, listId, Messages.get().container("GUI_REPLICATION_USER_LIST_NAME_0"), "cl", CmsListOrderEnum.ORDER_ASCENDING, "cl");
   }

   public void executeListMultiActions() throws IOException, ServletException {
      HashMap params;
      if (this.getParamListAction().equals("mu")) {
         params = new HashMap();
         params.put("users", this.getParamSelItems());
         params.put("style", "new");
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, "/ocee-replication/users/update", params);
      } else if (this.getParamListAction().equals("mc")) {
         params = new HashMap();
         params.put("users", this.getParamSelItems());
         params.put("style", "new");
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, "/ocee-replication/users/discard", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   public void executeListSingleActions() throws CmsRuntimeException, IOException, ServletException {
      Map params = new HashMap();
      if (!this.getParamListAction().equals("as") && !this.getParamListAction().equals("ai")) {
         if (this.getParamListAction().equals("au")) {
            params.put("users", this.getSelectedItem().getId());
            params.put("style", "new");
            params.put("action", "initial");
            this.getToolManager().jspForwardTool(this, "/ocee-replication/users/update", params);
         } else if (this.getParamListAction().equals("ad")) {
            params.put("users", this.getSelectedItem().getId());
            params.put("style", "new");
            params.put("action", "initial");
            this.getToolManager().jspForwardTool(this, "/ocee-replication/users/discard", params);
         } else {
            this.throwListUnsupportedActionException();
         }
      } else {
         String id = this.getSelectedItem().getId();
         String[] idArr = id.split(",");
         params.put("userid", idArr[0]);
         params.put("settingsid", idArr[1]);
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/showUser", params);
      }

   }

   protected void fillDetails(String detailId) {
   }

   protected String getListItemKey(CmsUser user, CmsReplicationUserSettings settings, CmsReplicationUserSyncAction action) {
      StringBuffer result = new StringBuffer();
      result.append(user.getId().toString());
      result.append(",");
      result.append(settings.getName());
      result.append(",");
      result.append(action.toString());
      return result.toString();
   }

   protected List getListItems() throws CmsException {
      List ret = new ArrayList();
      List users = CmsReplicationManager.getInstance().getSynchronizeUsers(this.getCms());
      Iterator iter = users.iterator();

      while(iter.hasNext()) {
         CmsReplicationUserData user = (CmsReplicationUserData)iter.next();
         ret.add(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(user));
      }

      return ret;
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconAction = new CmsListDirectAction("ai") {
         public boolean isEnabled() {
            String actionId = (String)this.getItem().get("cst");
            return CmsReplicationUserSyncAction.valueOf(actionId) != CmsReplicationUserSyncAction.DELETE;
         }
      };
      iconAction.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_SHOW_HELP_0"));
      iconAction.setIconPath("tools/accounts/buttons/user.png");
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition updateCol = new CmsListColumnDefinition("cu");
      updateCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_UPDATE_0"));
      updateCol.setWidth("20");
      updateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      updateCol.setSorteable(false);
      CmsListDirectAction updateAction = new CmsListDirectAction("au");
      updateAction.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_UPDATE_NAME_0"));
      updateAction.setHelpText(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_UPDATE_HELP_0"));
      updateAction.setIconPath("tools/ocee-replication/buttons/user_update.png");
      updateCol.addDirectAction(updateAction);
      metadata.addColumn(updateCol);
      CmsListColumnDefinition discardCol = new CmsListColumnDefinition("cd");
      discardCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_DISCARD_0"));
      discardCol.setWidth("20");
      discardCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      discardCol.setSorteable(false);
      CmsListDirectAction discardAction = new CmsListDirectAction("ad");
      discardAction.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_DISCARD_NAME_0"));
      discardAction.setHelpText(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_DISCARD_HELP_0"));
      discardAction.setIconPath("tools/ocee-replication/buttons/user_discard.png");
      discardCol.addDirectAction(discardAction);
      metadata.addColumn(discardCol);
      CmsListColumnDefinition loginCol = new CmsReplicationUserListColumnDefinition("cl");
      loginCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_LOGIN_0"));
      loginCol.setWidth("25%");
      CmsListDefaultAction showAction = new CmsListDefaultAction("as") {
         public boolean isEnabled() {
            String actionId = (String)this.getItem().get("cst");
            return CmsReplicationUserSyncAction.valueOf(actionId) != CmsReplicationUserSyncAction.DELETE;
         }
      };
      showAction.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_SHOW_NAME_0"));
      showAction.setHelpText(Messages.get().container("GUI_REPLICATION_USER_LIST_ACTION_SHOW_HELP_0"));
      loginCol.addDefaultAction(showAction);
      metadata.addColumn(loginCol);
      CmsListColumnDefinition nameCol = new CmsReplicationUserListColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_NAME_0"));
      nameCol.setWidth("25%");
      metadata.addColumn(nameCol);
      CmsReplicationUserListColumnDefinition statusCol = new CmsReplicationUserListColumnDefinition("cst");
      statusCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_STATUS_0"));
      statusCol.setWidth("10%");
      statusCol.setCustomFormatter(new I_CmsListFormatter() {
         public String format(Object data, Locale locale) {
            if (data instanceof String) {
               String actionId = (String)data;
               CmsReplicationUserSyncAction action = CmsReplicationUserSyncAction.valueOf(actionId);
               if (action == CmsReplicationUserSyncAction.ADD) {
                  return Messages.get().getBundle(locale).key("GUI_REPLICATION_USER_LIST_STATUS_NEW_0");
               }

               if (action == CmsReplicationUserSyncAction.DELETE) {
                  return Messages.get().getBundle(locale).key("GUI_REPLICATION_USER_LIST_STATUS_DELETED_0");
               }

               if (action == CmsReplicationUserSyncAction.UPDATE) {
                  return Messages.get().getBundle(locale).key("GUI_REPLICATION_USER_LIST_STATUS_CHANGED_0");
               }
            }

            return null;
         }
      });
      metadata.addColumn(statusCol);
      CmsListColumnDefinition lastModCol = new CmsListColumnDefinition("clm");
      lastModCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_LAST_MOD_0"));
      lastModCol.setWidth("25%");
      lastModCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
      metadata.addColumn(lastModCol);
      CmsListColumnDefinition serverCol = new CmsListColumnDefinition("cs");
      serverCol.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_COLS_SERVER_0"));
      serverCol.setWidth("25%");
      metadata.addColumn(serverCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction updateAction = new CmsListMultiAction("mu");
      updateAction.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_MACTION_UPDATE_NAME_0"));
      updateAction.setHelpText(Messages.get().container("GUI_REPLICATION_USER_LIST_MACTION_UPDATE_HELP_0"));
      updateAction.setIconPath("tools/ocee-replication/buttons/multi_user_update.png");
      updateAction.setConfirmationMessage(Messages.get().container("GUI_REPLICATION_USER_LIST_MACTION_UPDATE_CONF_0"));
      metadata.addMultiAction(updateAction);
      CmsListMultiAction commitAction = new CmsListMultiAction("mc");
      commitAction.setName(Messages.get().container("GUI_REPLICATION_USER_LIST_MACTION_COMMIT_NAME_0"));
      commitAction.setHelpText(Messages.get().container("GUI_REPLICATION_USER_LIST_MACTION_COMMIT_HELP_0"));
      commitAction.setIconPath("tools/ocee-replication/buttons/multi_user_discard.png");
      commitAction.setConfirmationMessage(Messages.get().container("GUI_REPLICATION_USER_LIST_MACTION_COMMIT_CONF_0"));
      metadata.addMultiAction(commitAction);
   }

   private CmsListItem o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsReplicationUserData user) {
      CmsListItem item = this.getList().newItem(this.getListItemKey(user.getUser(), user.getSettings(), user.getAction()));
      item.set("cl", user.getUser().getName());
      item.set("cn", user.getUser().getFullName());
      item.set("cst", user.getAction().toString());
      item.set("cs", user.getSettings().getServerName());
      item.set("clm", this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(user.getUser()));
      return item;
   }

   private Date o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsUser user) {
      String value = (String)user.getAdditionalInfo("USER_LASTMODIFIED");
      return value == null ? null : new Date(Long.parseLong(value));
   }
}
