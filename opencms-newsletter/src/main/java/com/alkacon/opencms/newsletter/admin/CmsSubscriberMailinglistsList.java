package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.tools.accounts.CmsUserGroupsList;

public class CmsSubscriberMailinglistsList extends CmsUserGroupsList {
   public static final String PATH_BUTTONS = "tools/newsletter/buttons/";

   public CmsSubscriberMailinglistsList(CmsJspActionElement jsp) {
      super(jsp, "lugl");
      this.getList().setName(Messages.get().container("GUI_SUBSCRIBERMAILINGLISTS_LIST_NAME_0"));
   }

   public CmsSubscriberMailinglistsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      super.setColumns(metadata);
      CmsListColumnDefinition iconCol = metadata.getColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_ICON_0"));
      iconCol.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_ICON_HELP_0"));
      I_CmsListDirectAction dirAction = iconCol.getDirectAction("aid");
      dirAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_DIRECT_NAME_0"));
      dirAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_DIRECT_HELP_0"));
      dirAction.setIconPath("tools/newsletter/buttons/mailinglist.png");
      CmsListColumnDefinition displayCol = metadata.getColumnDefinition("cdn");
      displayCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_NAME_0"));
      CmsListDefaultAction removeAction = displayCol.getDefaultAction("dr");
      removeAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_REMOVE_NAME_0"));
      removeAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_REMOVE_HELP_0"));
      CmsListColumnDefinition descCol = metadata.getColumnDefinition("cd");
      descCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_DESCRIPTION_0"));
      CmsListColumnDefinition stateCol = metadata.getColumnDefinition("cs");
      stateCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_STATE_0"));
      stateCol.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_STATE_HELP_0"));
      I_CmsListDirectAction dirStateAction = stateCol.getDirectAction("ar");
      dirStateAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_REMOVE_NAME_0"));
      dirStateAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_REMOVE_HELP_0"));
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction removeMultiAction = new CmsListMultiAction("mr");
      removeMultiAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_REMOVE_NAME_0"));
      removeMultiAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_REMOVE_HELP_0"));
      removeMultiAction.setConfirmationMessage(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_REMOVE_CONF_0"));
      removeMultiAction.setIconPath("list/multi_minus.png");
      metadata.addMultiAction(removeMultiAction);
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
