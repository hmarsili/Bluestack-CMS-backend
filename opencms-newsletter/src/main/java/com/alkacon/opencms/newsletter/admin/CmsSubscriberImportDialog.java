package com.alkacon.opencms.newsletter.admin;

import com.alkacon.opencms.newsletter.CmsNewsletterManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

public class CmsSubscriberImportDialog extends CmsWidgetDialog {
   public static final String KEY_PREFIX = "subscriber.import";
   public static final String[] PAGES = new String[]{"page1", "page2"};
   private CmsSubscriberImportObject m_importObject;
   private String m_paramGroupid;
   private String m_paramOufqn;

   public CmsSubscriberImportDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsSubscriberImportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      List errors = new ArrayList();
      List emailsToSubscribe = this.m_importObject.getEmailAddresses();
      if (emailsToSubscribe.size() == 0) {
         errors.add(new CmsException(Messages.get().container("ERR_SUBSCRIBER_IMPORT_NO_EMAIL_0")));
      } else {
         Iterator i = emailsToSubscribe.iterator();

         while(i.hasNext()) {
            String email = (String)i.next();
            String userName = this.getParamOufqn() + email;

            try {
               try {
                  this.getCms().readUser(userName);
               } catch (CmsException var8) {
                  CmsUser user = this.getCms().createUser(userName, CmsNewsletterManager.getPassword(), "", new HashMap());
                  user.setEmail(email);
                  this.getCms().writeUser(user);
               }

               CmsGroup mailGroup = this.getCms().readGroup(new CmsUUID(this.getParamGroupid()));
               if (!this.getCms().getGroupsOfUser(userName, true, false).contains(mailGroup)) {
                  this.getCms().addUserToGroup(userName, mailGroup.getName());
               }
            } catch (CmsException var9) {
               errors.add(var9);
            }
         }
      }

      this.setCommitErrors(errors);
   }

   public String getParamGroupid() {
      return this.m_paramGroupid;
   }

   public String getParamOufqn() {
      return this.m_paramOufqn;
   }

   public void setParamGroupid(String paramGroupid) {
      this.m_paramGroupid = paramGroupid;
   }

   public void setParamOufqn(String ouFqn) {
      if (ouFqn == null) {
         ouFqn = "";
      }

      this.m_paramOufqn = ouFqn;
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("GUI_SUBSCRIBER_IMPORT_LABEL_HINT_BLOCK_0")));
         result.append(this.key("GUI_SUBSCRIBER_IMPORT_LABEL_HINT_TEXT_0"));
         result.append(this.dialogBlockEnd());
         result.append(this.dialogBlockStart(this.key("GUI_SUBSCRIBER_IMPORT_LABEL_DATA_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 0));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      } else if (dialog.equals(PAGES[1])) {
         List emailAddresses = this.m_importObject.getEmailAddresses();
         Iterator i;
         String email;
         if (this.m_importObject.getInvalidLines().size() > 0) {
            result.append(this.dialogBlockStart(this.key("GUI_SUBSCRIBER_IMPORT_LABEL_INVALIDLINES_BLOCK_0")));
            result.append(this.dialogSpacer());
            i = this.m_importObject.getInvalidLines().iterator();

            while(i.hasNext()) {
               email = (String)i.next();
               result.append(email);
               if (i.hasNext()) {
                  result.append("<br/>");
               }
            }

            result.append(this.dialogBlockEnd());
         }

         if (this.m_importObject.getConvertedLines().size() > 0) {
            result.append(this.dialogBlockStart(this.key("GUI_SUBSCRIBER_IMPORT_LABEL_CONVERTEDLINES_BLOCK_0")));
            result.append(this.dialogSpacer());
            i = this.m_importObject.getConvertedLines().iterator();

            while(i.hasNext()) {
               String[] line = (String[])((String[])i.next());
               result.append(line[0]);
               result.append(" ");
               result.append(this.key("GUI_SUBSCRIBER_IMPORT_CONVERTEDLINES_TO_0"));
               result.append(" ");
               result.append(line[1]);
               if (i.hasNext()) {
                  result.append("<br/>");
               }
            }

            result.append(this.dialogBlockEnd());
         }

         result.append(this.dialogBlockStart(this.key("GUI_SUBSCRIBER_IMPORT_LABEL_EMAILS_BLOCK_0")));
         result.append(this.dialogSpacer());
         if (emailAddresses.size() > 0) {
            i = emailAddresses.iterator();

            while(i.hasNext()) {
               email = (String)i.next();
               result.append(email);
               if (i.hasNext()) {
                  result.append("<br/>");
               }
            }
         } else {
            result.append(this.key("ERR_SUBSCRIBER_IMPORT_NO_EMAIL_0"));
         }

         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.initImportObject();
      this.setKeyPrefix("subscriber.import");
      this.addWidget(new CmsWidgetDialogParameter(this.m_importObject, "importEmail", PAGES[0], new CmsTextareaWidget(8)));
   }

   protected String[] getPageArray() {
      return PAGES;
   }

   protected void initImportObject() {
      Object o;
      if (!CmsStringUtil.isEmpty(this.getParamAction()) && !"initial".equals(this.getParamAction())) {
         o = this.getDialogObject();
      } else {
         o = null;
      }

      if (!(o instanceof CmsSubscriberImportObject)) {
         this.m_importObject = new CmsSubscriberImportObject();
      } else {
         this.m_importObject = (CmsSubscriberImportObject)o;
      }

   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      super.initWorkplaceRequestValues(settings, request);
      this.setDialogObject(this.m_importObject);
   }
}
