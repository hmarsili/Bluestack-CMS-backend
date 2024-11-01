package com.alkacon.opencms.newsletter.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.accounts.CmsGroupDependenciesList;

public class CmsMailinglistDeleteDialog extends CmsGroupDependenciesList {
   public CmsMailinglistDeleteDialog(CmsJspActionElement jsp) {
      super("lgdl", jsp);
   }

   public CmsMailinglistDeleteDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionDialog() throws JspException, ServletException, IOException {
      switch(this.getAction()) {
      case 131:
         Iterator it = CmsStringUtil.splitAsList(this.getGroupName(), "|", true).iterator();
         CmsObject cms = this.getCms();

         while(it.hasNext()) {
            String name = (String)it.next();

            try {
               List users = cms.getUsersOfGroup(name);
               cms.deleteGroup(name);
               Iterator itUsers = users.iterator();

               while(itUsers.hasNext()) {
                  CmsUser user = (CmsUser)itUsers.next();
                  if (cms.getGroupsOfUser(user.getName(), true).isEmpty()) {
                     cms.deleteUser(user.getId());
                  }
               }
            } catch (CmsException var7) {
               throw new CmsRuntimeException(var7.getMessageContainer(), var7);
            }
         }

         this.setAction(4);
         this.actionCloseDialog();
         break;
      default:
         super.actionDialog();
      }

   }

   protected String customHtmlStart() {
      StringBuffer result = new StringBuffer(512);
      result.append(this.dialogBlockStart(org.opencms.workplace.tools.accounts.Messages.get().container("GUI_GROUP_DEPENDENCIES_NOTICE_0").key(this.getLocale())));
      result.append(this.key("GUI_MAILINGLIST_DELETE_0"));
      result.append(this.dialogBlockEnd());
      return result.toString();
   }

   protected List getListItems() {
      return new ArrayList();
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }
}
