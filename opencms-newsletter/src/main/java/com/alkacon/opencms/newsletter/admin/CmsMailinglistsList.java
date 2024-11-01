package com.alkacon.opencms.newsletter.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.tools.accounts.A_CmsGroupsList;

public class CmsMailinglistsList extends A_CmsGroupsList {
   public static final String LIST_ACTION_SEND = "ase";
   public static final String LIST_COLUMN_SEND = "cse";
   public static final String LIST_ID = "lgl";
   public static final String PATH_BUTTONS = "tools/newsletter/buttons/";

   public CmsMailinglistsList(CmsJspActionElement jsp) {
      super(jsp, "lgl", Messages.get().container("GUI_MAILINGLISTS_LIST_NAME_0"));
   }

   public CmsMailinglistsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {
      if (this.getParamListAction().equals("ase")) {
         String groupId = this.getSelectedItem().getId();
         String groupName = this.getSelectedItem().get("cn").toString();
         Map params = new HashMap();
         params.put("groupid", groupId);
         params.put("oufqn", this.getParamOufqn());
         params.put("groupname", groupName);
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/edit/send", params);
      }

      super.executeListSingleActions();
   }

   protected void fillDetails(String detailId) {
      List groups = this.getList().getAllContent();
      Iterator itGroups = groups.iterator();

      while(itGroups.hasNext()) {
         CmsListItem item = (CmsListItem)itGroups.next();
         String groupName = item.get("cn").toString();
         StringBuffer html = new StringBuffer(512);

         try {
            if (!detailId.equals("du")) {
               continue;
            }

            List users = this.getCms().getUsersOfGroup(groupName, true);

            for(Iterator itUsers = users.iterator(); itUsers.hasNext(); html.append("\n")) {
               CmsUser user = (CmsUser)itUsers.next();
               html.append(user.getSimpleName());
               if (itUsers.hasNext()) {
                  html.append("<br>");
               }
            }
         } catch (Exception var10) {
         }

         item.set(detailId, html.toString());
      }

   }

   protected List getGroups() throws CmsException {
      return OpenCms.getOrgUnitManager().getGroups(this.getCms(), this.getParamOufqn(), false);
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition sendCol = new CmsListColumnDefinition("cse");
      sendCol.setName(Messages.get().container("GUI_NEWSLETTER_LIST_COLS_SEND_0"));
      sendCol.setHelpText(Messages.get().container("GUI_NEWSLETTER_LIST_COLS_SEND_HELP_0"));
      CmsListDirectAction sendAction = new CmsListDirectAction("ase");
      sendAction.setName(Messages.get().container("GUI_NEWSLETTER_LIST_ACTION_SEND_0"));
      sendAction.setHelpText(Messages.get().container("GUI_NEWSLETTER_LIST_ACTION_SEND_HELP_0"));
      sendAction.setIconPath("tools/newsletter/buttons/newsletter_send.png");
      sendCol.addDirectAction(sendAction);
      metadata.addColumn(sendCol);
      super.setColumns(metadata);
      CmsListColumnDefinition editCol = metadata.getColumnDefinition("ce");
      editCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_EDIT_0"));
      editCol.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_EDIT_HELP_0"));
      CmsListColumnDefinition usersCol = metadata.getColumnDefinition("cu");
      usersCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_SUBSCRIBERS_0"));
      usersCol.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_SUBSCRIBERS_HELP_0"));
      CmsListDirectAction usersAction = (CmsListDirectAction)usersCol.getDirectAction("au");
      usersAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_ACTION_SUBSCRIBERS_NAME_0"));
      usersAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_ACTION_SUBSCRIBERS_HELP_0"));
      usersAction.setIconPath("tools/newsletter/buttons/subscriber.png");
      metadata.getColumnDefinition("ca").setVisible(false);
      CmsListColumnDefinition deleteCol = metadata.getColumnDefinition("cd");
      deleteCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_DELETE_0"));
      deleteCol.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_DELETE_HELP_0"));
      CmsListColumnDefinition nameCol = metadata.getColumnDefinition("cdn");
      CmsListDefaultAction defAction = nameCol.getDefaultAction("de");
      defAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_EDIT_NAME_0"));
      defAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_EDIT_HELP_0"));
   }

   protected void setDeleteAction(CmsListColumnDefinition deleteCol) {
      CmsListDirectAction deleteAction = new CmsListDirectAction("ad");
      deleteAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_ACTION_DELETE_NAME_0"));
      deleteAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_ACTION_DELETE_HELP_0"));
      deleteAction.setIconPath("list/delete.png");
      deleteCol.addDirectAction(deleteAction);
   }

   protected void setEditAction(CmsListColumnDefinition editCol) {
      CmsListDirectAction editAction = new CmsListDirectAction("ae");
      editAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_ACTION_EDIT_NAME_0"));
      editAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_ACTION_EDIT_HELP_0"));
      editAction.setIconPath("tools/newsletter/buttons/mailinglist.png");
      editCol.addDirectAction(editAction);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails subscribersDetails = new CmsListItemDetails("du");
      subscribersDetails.setAtColumn("cdn");
      subscribersDetails.setVisible(false);
      subscribersDetails.setShowActionName(Messages.get().container("GUI_MAILINGLISTS_DETAIL_SHOW_SUBSCRIBERS_NAME_0"));
      subscribersDetails.setShowActionHelpText(Messages.get().container("GUI_MAILINGLISTS_DETAIL_SHOW_SUBSCRIBERS_HELP_0"));
      subscribersDetails.setHideActionName(Messages.get().container("GUI_MAILINGLISTS_DETAIL_HIDE_SUBSCRIBERS_NAME_0"));
      subscribersDetails.setHideActionHelpText(Messages.get().container("GUI_MAILINGLISTS_DETAIL_HIDE_SUBSCRIBERS_HELP_0"));
      subscribersDetails.setName(Messages.get().container("GUI_MAILINGLISTS_DETAIL_SUBSCRIBERS_NAME_0"));
      subscribersDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_MAILINGLISTS_DETAIL_SUBSCRIBERS_NAME_0")));
      metadata.addItemDetails(subscribersDetails);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction deleteMultiAction = new CmsListMultiAction("md");
      deleteMultiAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_DELETE_NAME_0"));
      deleteMultiAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_DELETE_HELP_0"));
      deleteMultiAction.setConfirmationMessage(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_DELETE_CONF_0"));
      deleteMultiAction.setIconPath("list/multi_delete.png");
      metadata.addMultiAction(deleteMultiAction);
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
