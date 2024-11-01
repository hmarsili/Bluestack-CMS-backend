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
import org.opencms.workplace.tools.accounts.CmsNotUserGroupsList;

public class CmsNotSubscriberMailinglistsList extends CmsNotUserGroupsList {
   public CmsNotSubscriberMailinglistsList(CmsJspActionElement jsp) {
      super(jsp, "lnugl");
      this.getList().setName(Messages.get().container("GUI_NOTSUBSCRIBERMAILINGLISTS_LIST_NAME_0"));
   }

   public CmsNotSubscriberMailinglistsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
      I_CmsListDirectAction iconAction = iconCol.getDirectAction("ai");
      iconAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_AVAILABLE_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_AVAILABLE_HELP_0"));
      iconAction.setIconPath("tools/newsletter/buttons/mailinglist.png");
      CmsListColumnDefinition displayCol = metadata.getColumnDefinition("cdn");
      displayCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_NAME_0"));
      CmsListDefaultAction addAction = displayCol.getDefaultAction("da");
      addAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_ADD_NAME_0"));
      addAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_ADD_HELP_0"));
      CmsListColumnDefinition descCol = metadata.getColumnDefinition("cd");
      descCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_DESCRIPTION_0"));
      CmsListColumnDefinition stateCol = metadata.getColumnDefinition("cs");
      stateCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_STATE_0"));
      stateCol.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_STATE_HELP_0"));
      I_CmsListDirectAction dirStateAction = stateCol.getDirectAction("aa");
      dirStateAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_ADD_NAME_0"));
      dirStateAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_DEFACTION_ADD_HELP_0"));
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      super.setMultiActions(metadata);
      CmsListMultiAction addMultiAction = metadata.getMultiAction("ma");
      addMultiAction.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_ADD_NAME_0"));
      addMultiAction.setHelpText(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_ADD_HELP_0"));
      addMultiAction.setConfirmationMessage(Messages.get().container("GUI_MAILINGLISTS_LIST_MACTION_ADD_CONF_0"));
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
