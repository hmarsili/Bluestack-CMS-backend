package com.alkacon.opencms.newsletter.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.tools.accounts.CmsShowGroupUsersList;

public class CmsShowMailinglistSubscribersList extends CmsShowGroupUsersList {
   public CmsShowMailinglistSubscribersList(CmsJspActionElement jsp) {
      super(jsp, "lsgul");
      this.getList().setName(Messages.get().container("GUI_MAILINGLISTSUBSCRIBERS_LIST_NAME_0"));
   }

   public CmsShowMailinglistSubscribersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListSingleActions() throws IOException, ServletException {
      String userId = this.getSelectedItem().getId();
      Map params = new HashMap();
      params.put("action", "initial");
      params.put("userid", userId);
      params.put("oufqn", this.getParamOufqn());
      if (this.getParamListAction().equals("ae")) {
         this.getToolManager().jspForwardTool(this, "/newsletter/orgunit/subscribers/edit/user", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   public String getIconPath(CmsListItem item) {
      return "tools/newsletter/buttons/subscriber.png";
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
      CmsListColumnDefinition nameCol = metadata.getColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_COLS_EMAIL_0"));
      nameCol.setWidth("100%");
      CmsListDefaultAction defAction = nameCol.getDefaultAction("ae");
      defAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_EDIT_NAME_0"));
      defAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_DEFACTION_EDIT_HELP_0"));
      I_CmsListDirectAction iconAction = iconCol.getDirectAction("ai");
      iconAction.setName(Messages.get().container("GUI_SUBSCRIBERS_LIST_INMAILINGLIST_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_SUBSCRIBERS_LIST_INMAILINGLIST_HELP_0"));
      metadata.getColumnDefinition("cf").setVisible(false);
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
