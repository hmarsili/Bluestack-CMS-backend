package com.alkacon.opencms.newsletter.admin;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListSearchAction;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.tools.accounts.A_CmsUsersList;

public class CmsSubscribersList extends A_CmsUsersList {
   public static final String LIST_ID = "llu";

   public CmsSubscribersList(CmsJspActionElement jsp) {
      super(jsp, "llu", Messages.get().container("GUI_SUBSCRIBERS_LIST_NAME_0"));
   }

   public CmsSubscribersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected String getGroupIcon() {
      return "tools/newsletter/buttons/mailinglist.png";
   }

   protected List getUsers() throws CmsException {
      return OpenCms.getOrgUnitManager().getUsers(this.getCms(), this.getParamOufqn(), false);
   }

   protected CmsUser readUser(String name) throws CmsException {
      return this.getCms().readUser(name);
   }

   protected void setColumns(CmsListMetadata metadata) {
      super.setColumns(metadata);
      metadata.getColumnDefinition("cr").setVisible(false);
      metadata.getColumnDefinition("ci").setVisible(false);
      metadata.getColumnDefinition("cm").setVisible(false);
      metadata.getColumnDefinition("cl").setVisible(false);
      metadata.getColumnDefinition("cn").setVisible(false);
      CmsListColumnDefinition viewCol = metadata.getColumnDefinition("ce");
      viewCol.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_ICON_HELP_0"));
      CmsListColumnDefinition deleteCol = metadata.getColumnDefinition("cd");
      deleteCol.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_DELETE_HELP_0"));
      CmsListColumnDefinition mailinglistsCol = metadata.getColumnDefinition("cg");
      mailinglistsCol.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_MAILINGLISTS_0"));
      mailinglistsCol.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_MAILINGLISTS_HELP_0"));
      I_CmsListDirectAction mailinglistsAction = mailinglistsCol.getDirectAction("ag");
      mailinglistsAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_MAILINGLISTS_NAME_0"));
      mailinglistsAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_MAILINGLISTS_HELP_0"));
      CmsListColumnDefinition displayCol = metadata.getColumnDefinition("cdn");
      displayCol.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_EMAIL_0"));
      displayCol.setWidth("100%");
      CmsListDefaultAction defEditAction = displayCol.getDefaultAction("de");
      defEditAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_EDIT_NAME_0"));
      defEditAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_EDIT_HELP_0"));
      CmsListColumnDefinition activateCol = metadata.getColumnDefinition("ca");
      activateCol.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_ACTIVATE_HELP_0"));
      I_CmsListDirectAction activateAction = activateCol.getDirectAction("aa");
      activateAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_ACTIVATE_HELP_0"));
      activateAction.setConfirmationMessage(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_ACTIVATE_CONF_0"));
      I_CmsListDirectAction deactivateAction = activateCol.getDirectAction("ac");
      deactivateAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_DEACTIVATE_HELP_0"));
      deactivateAction.setConfirmationMessage(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_DEACTIVATE_CONF_0"));
   }

   protected void setDeleteAction(CmsListColumnDefinition deleteCol) {
      CmsListDirectAction deleteAction = new CmsListDirectAction("ad");
      deleteAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_DELETE_NAME_0"));
      deleteAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_DELETE_HELP_0"));
      deleteAction.setConfirmationMessage(Messages.get().container("GUI_SUBSCRIBERS_LIST_ACTION_DELETE_CONF_0"));
      deleteAction.setIconPath("list/delete.png");
      deleteCol.addDirectAction(deleteAction);
   }

   protected void setEditAction(CmsListColumnDefinition editCol) {
      CmsListDirectAction editAction = new CmsListDirectAction("ae");
      editAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_EDIT_NAME_0"));
      editAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_EDIT_HELP_0"));
      editAction.setIconPath("tools/newsletter/buttons/subscriber.png");
      editCol.addDirectAction(editAction);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails mailinglistsDetails = new CmsListItemDetails("dg");
      mailinglistsDetails.setAtColumn("cdn");
      mailinglistsDetails.setVisible(false);
      mailinglistsDetails.setShowActionName(Messages.get().container("GUI_SUBSCRIBERS_DETAIL_SHOW_MAILINGLISTS_NAME_0"));
      mailinglistsDetails.setShowActionHelpText(Messages.get().container("GUI_SUBSCRIBERS_DETAIL_SHOW_MAILINGLISTS_HELP_0"));
      mailinglistsDetails.setHideActionName(Messages.get().container("GUI_SUBSCRIBERS_DETAIL_HIDE_MAILINGLISTS_NAME_0"));
      mailinglistsDetails.setHideActionHelpText(Messages.get().container("GUI_SUBSCRIBERS_DETAIL_HIDE_MAILINGLISTS_HELP_0"));
      mailinglistsDetails.setName(Messages.get().container("GUI_SUBSCRIBERS_DETAIL_MAILINGLISTS_NAME_0"));
      mailinglistsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_SUBSCRIBERS_DETAIL_MAILINGLISTS_NAME_0")));
      metadata.addItemDetails(mailinglistsDetails);
      CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition("cdn"));
      searchAction.addColumn(metadata.getColumnDefinition("cn"));
      metadata.setSearchAction(searchAction);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction deleteMultiAction = new CmsListMultiAction("md");
      deleteMultiAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_DELETE_NAME_0"));
      deleteMultiAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_DELETE_HELP_0"));
      deleteMultiAction.setConfirmationMessage(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_DELETE_CONF_0"));
      deleteMultiAction.setIconPath("list/multi_delete.png");
      metadata.addMultiAction(deleteMultiAction);
      CmsListMultiAction activateUser = new CmsListMultiAction("ma");
      activateUser.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_ACTIVATE_NAME_0"));
      activateUser.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_ACTIVATE_HELP_0"));
      activateUser.setConfirmationMessage(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_ACTIVATE_CONF_0"));
      activateUser.setIconPath("list/multi_activate.png");
      metadata.addMultiAction(activateUser);
      CmsListMultiAction deactivateUser = new CmsListMultiAction("mc");
      deactivateUser.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_DEACTIVATE_NAME_0"));
      deactivateUser.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_DEACTIVATE_HELP_0"));
      deactivateUser.setConfirmationMessage(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_DEACTIVATE_CONF_0"));
      deactivateUser.setIconPath("list/multi_deactivate.png");
      metadata.addMultiAction(deactivateUser);
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
