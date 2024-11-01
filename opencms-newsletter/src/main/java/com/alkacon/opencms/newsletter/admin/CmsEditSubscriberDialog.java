package com.alkacon.opencms.newsletter.admin;

import com.alkacon.opencms.newsletter.CmsNewsletterManager;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.accounts.A_CmsEditUserDialog;

public class CmsEditSubscriberDialog extends A_CmsEditUserDialog {
   public static final String SB_KEY_PREFIX = "subscriber";

   public CmsEditSubscriberDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsEditSubscriberDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      this.m_user.setName(this.getParamOufqn() + this.m_user.getEmail());
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.m_user.getFirstname())) {
         this.m_user.setFirstname("_");
      }

      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.m_user.getLastname())) {
         this.m_user.setLastname("_");
      }

      this.getPwdInfo().setNewPwd(CmsNewsletterManager.getPassword());
      this.getPwdInfo().setConfirmation(CmsNewsletterManager.getPassword());
      super.actionCommit();
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("GUI_USER_EDITOR_LABEL_IDENTIFICATION_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 3));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected CmsUser createUser(String name, String pwd, String desc, Map info) throws CmsException {
      return this.getCms().createUser(name, pwd, desc, info);
   }

   protected void defineWidgets() {
      this.initUserObject();
      this.setKeyPrefix("subscriber");
      if (this.isNewUser()) {
         this.addWidget(new CmsWidgetDialogParameter(this.m_user, "email", PAGES[0], new CmsInputWidget()));
      } else {
         this.addWidget(new CmsWidgetDialogParameter(this.m_user, "email", PAGES[0], new CmsDisplayWidget()));
      }

      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "lastname", "", PAGES[0], new CmsInputWidget(), 0, 1));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "firstname", "", PAGES[0], new CmsInputWidget(), 0, 1));
      this.addWidget(new CmsWidgetDialogParameter(this.m_user, "enabled", PAGES[0], new CmsCheckboxWidget()));
   }

   protected String getListClass() {
      return CmsSubscribersList.class.getName();
   }

   protected String getListRootPath() {
      return "/newsletter/orgunit/subscribers";
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected boolean isEditable(CmsUser user) {
      return true;
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }

   protected void writeUser(CmsUser user) throws CmsException {
      this.getCms().writeUser(user);
   }
}
