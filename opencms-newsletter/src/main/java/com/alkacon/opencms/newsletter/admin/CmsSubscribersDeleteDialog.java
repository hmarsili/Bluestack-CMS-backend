package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.tools.accounts.CmsUserDependenciesList;

public class CmsSubscribersDeleteDialog extends CmsUserDependenciesList {
   public CmsSubscribersDeleteDialog(CmsJspActionElement jsp) {
      super("ludl", jsp);
   }

   public CmsSubscribersDeleteDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected String customHtmlStart() {
      StringBuffer result = new StringBuffer(512);
      result.append(this.dialogBlockStart(this.key("GUI_USER_DEPENDENCIES_NOTICE_0")));
      result.append("\n");
      result.append(this.key("GUI_SUBSCRIBERS_LIST_ACTION_DELETE_CONF_0"));
      result.append(this.dialogBlockEnd());
      return result.toString();
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }
}
