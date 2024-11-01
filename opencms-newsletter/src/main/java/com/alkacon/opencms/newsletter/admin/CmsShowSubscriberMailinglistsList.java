package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.tools.accounts.CmsShowUserGroupsList;

public class CmsShowSubscriberMailinglistsList extends CmsShowUserGroupsList {
   public CmsShowSubscriberMailinglistsList(CmsJspActionElement jsp) {
      super(jsp, "lsugl");
      this.getList().setName(Messages.get().container("GUI_SUBSCRIBERMAILINGLISTS_LIST_NAME_0"));
   }

   public CmsShowSubscriberMailinglistsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
      CmsListColumnDefinition displayCol = new CmsListColumnDefinition("cdn");
      displayCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_NAME_0"));
      CmsListColumnDefinition descCol = new CmsListColumnDefinition("cd");
      descCol.setName(Messages.get().container("GUI_MAILINGLISTS_LIST_COLS_DESCRIPTION_0"));
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
