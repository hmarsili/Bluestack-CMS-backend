package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.tools.accounts.CmsNotGroupUsersList;

public class CmsNotMailinglistSubscribersList extends CmsNotGroupUsersList {
   public CmsNotMailinglistSubscribersList(CmsJspActionElement jsp) {
      super(jsp, "lngul");
      this.getList().setName(Messages.get().container("GUI_NOTMAILINGLISTSUBSCRIBERS_LIST_NAME_0"));
   }

   public CmsNotMailinglistSubscribersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected void initializeDetail(String detailId) {
      super.initializeDetail(detailId);
      if (detailId.equals("doo")) {
         this.getList().getMetadata().getColumnDefinition("co").setVisible(false);
         this.getList().getMetadata().getColumnDefinition("co").setPrintable(false);
      }

   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      super.setColumns(metadata);
      CmsListColumnDefinition iconCol = metadata.getColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_ICON_0"));
      iconCol.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_ICON_HELP_0"));
      I_CmsListDirectAction iconAction = iconCol.getDirectAction("ai");
      iconAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_AVAILABLE_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_AVAILABLE_HELP_0"));
      iconAction.setIconPath("tools/newsletter/buttons/subscriber.png");
      CmsListColumnDefinition nameCol = metadata.getColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_EMAIL_0"));
      nameCol.setWidth("100%");
      CmsListDefaultAction addAction = nameCol.getDefaultAction("da");
      addAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_ADD_NAME_0"));
      addAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_ADD_HELP_0"));
      I_CmsListDirectAction stateAction = metadata.getColumnDefinition("cs").getDirectAction("aa");
      stateAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_ADD_NAME_0"));
      stateAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_ADD_HELP_0"));
      metadata.getColumnDefinition("cf").setVisible(false);
   }

   protected void setIconAction(CmsListColumnDefinition iconCol) {
      CmsListDirectAction iconAction = new CmsListDefaultAction("ai");
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      super.setMultiActions(metadata);
      CmsListMultiAction addMultiAction = metadata.getMultiAction("ma");
      addMultiAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_ADD_NAME_0"));
      addMultiAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_ADD_HELP_0"));
      addMultiAction.setConfirmationMessage(Messages.get().container("GUI_SUBSCRIBERS_LIST_MACTION_ADD_CONF_0"));
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
