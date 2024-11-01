package com.alkacon.opencms.newsletter;

import com.alkacon.opencms.commons.CmsStringCrypter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

public class CmsNewsletterSubscriptionBean extends CmsJspActionElement {
   public static final int ACTION_CONFIRMSUBSCRIPTION = 2;
   public static final int ACTION_CONFIRMUNSUBSCRIPTION = 3;
   public static final int ACTION_SUBSCRIBE = 0;
   public static final int ACTION_UNSUBSCRIBE = 1;
   public static final String PARAM_ACTION = "action";
   public static final String PARAM_EMAIL = "email";
   public static final String PARAM_FILE = "file";
   private static final String CRYPT_PASSWORD = "YwqP-82h";
   private static final Log LOG = CmsLog.getLog(CmsNewsletterSubscriptionBean.class);
   private static final String MACRO_EMAIL = "email";
   private static final String MACRO_LINK = "link";
   private static final String MACRO_TITLE = "title";
   private static final String NODE_CONFIRM = "Confirm";
   private static final String NODE_ERROR = "Error";
   private static final String NODE_MAILFROM = "MailFrom";
   private static final String NODE_MAILINGLIST = "MailingList";
   private static final String NODE_MAILSUBJECT = "MailSubject";
   private static final String NODE_MAILTEXT = "MailText";
   private static final String NODE_OK = "Ok";
   private static final String NODE_SUBSCRIBE_ERROR = "SubscribeError";
   private static final String NODE_SUBSCRIBE_OK = "SubscribeOk";
   private static final String NODE_UNSUBSCRIBE_ERROR = "UnSubscribeError";
   private static final String NODE_UNSUBSCRIBE_OK = "UnSubscribeOk";
   private static final String XPATH_1_CONFIRM = "Confirm/";
   private static final String XPATH_1_SUBSCRIBE = "Subscribe/";
   private static final String XPATH_2_MAIL = "Confirm/Mail/";
   private static final String XPATH_2_SUBSCRIBE = "Confirm/Subscribe/";
   private static final String XPATH_2_UNSUBSCRIBE = "Confirm/UnSubscribe/";
   private int m_action;
   private int m_checkedAction;
   private CmsXmlContent m_configContent;
   private String m_email;
   private List m_errors;
   private CmsMessages m_messages;
   private CmsMacroResolver m_resolver;

   public CmsNewsletterSubscriptionBean() {
   }

   public CmsNewsletterSubscriptionBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this.init(context, req, res);
   }

   public String actionConfirmSubscribe() {
      String result = this.getConfigText("Confirm/Subscribe/Error");
      if (CmsStringUtil.isNotEmpty(this.getEmail()) && this.getNewsletterManager().activateNewsletterUser(this.getEmail(), this.getConfigText("MailingList"))) {
         result = this.getConfigText("Confirm/Subscribe/Ok");
      }

      return result;
   }

   public String actionConfirmUnsubscribe() {
      String result = this.getConfigText("Confirm/UnSubscribe/Error");
      if (CmsStringUtil.isNotEmpty(this.getEmail()) && this.getNewsletterManager().deleteNewsletterUser(this.getEmail(), this.getConfigText("MailingList"), this.isConfirmationEnabled())) {
         result = this.getConfigText("Confirm/UnSubscribe/Ok");
      }

      return result;
   }

   public String actionSubscribe() {
      String result = this.getConfigText("Subscribe/SubscribeError");
      if (CmsStringUtil.isNotEmpty(this.getEmail())) {
         String groupName = this.getConfigText("MailingList");
         CmsUser user = this.getNewsletterManager().createNewsletterUser(this.getEmail(), groupName, !this.isConfirmationEnabled());
         if (user != null) {
            if (this.isConfirmationEnabled()) {
               this.setLinkMacro(2, this.getEmail());
               if (this.sendConfirmationMail(this.getConfigText("Confirm/Subscribe/MailSubject"), this.getConfigText("Confirm/Subscribe/MailText"))) {
                  result = this.getConfigText("Subscribe/SubscribeOk");
               }
            } else {
               result = this.getConfigText("Subscribe/SubscribeOk");
            }
         }
      }

      return result.toString();
   }

   public String actionUnsubscribe() {
      String result = this.getConfigText("Subscribe/UnSubscribeError");
      if (CmsStringUtil.isNotEmpty(this.getEmail())) {
         if (this.isConfirmationEnabled()) {
            if (this.getNewsletterManager().markToDeleteNewsletterUser(this.getEmail(), this.getConfigText("MailingList"))) {
               this.setLinkMacro(3, this.getEmail());
               if (this.sendConfirmationMail(this.getConfigText("Confirm/UnSubscribe/MailSubject"), this.getConfigText("Confirm/UnSubscribe/MailText"))) {
                  result = this.getConfigText("Subscribe/UnSubscribeOk");
               }
            }
         } else if (this.getNewsletterManager().deleteNewsletterUser(this.getEmail(), this.getConfigText("MailingList"), false)) {
            result = this.getConfigText("Subscribe/UnSubscribeOk");
         }
      }

      return result;
   }

   public int getAction() {
      return this.m_action;
   }

   public int getCheckedAction() {
      return this.m_checkedAction;
   }

   public String getConfigText(String element) {
      String result = this.getConfigContent().getStringValue(this.getCmsObject(), element, this.getRequestContext().getLocale());
      return this.getMacroResolver().resolveMacros(result);
   }

   public String getEmail() {
      return CmsStringUtil.isEmpty(this.m_email) ? "" : this.m_email;
   }

   public List getErrors() {
      return this.m_errors;
   }

   public CmsMessages getMessages() {
      if (this.m_messages == null) {
         this.m_messages = new CmsMessages(CmsNewsletterManager.MODULE_NAME + ".workplace", this.getRequestContext().getLocale());
      }

      return this.m_messages;
   }

   public String getValidationErrorsHtml(String element) {
      StringBuffer result = new StringBuffer(2048);
      Iterator i = this.getErrors().iterator();

      while(i.hasNext()) {
         String error = (String)i.next();
         result.append("\t<").append(element).append(">");
         result.append(error);
         result.append("</").append(element).append(">\n");
      }

      return result.toString();
   }

   public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      super.init(context, req, res);
      this.m_errors = new ArrayList();
      this.m_action = -1;
      String action = req.getParameter("action");
      if (CmsStringUtil.isNotEmpty(action)) {
         try {
            this.m_action = Integer.parseInt(action);
            this.m_checkedAction = this.m_action;
            this.m_email = req.getParameter("email");
            if (this.m_action == 2 || this.m_action == 3) {
               this.m_email = CmsStringCrypter.decrypt(this.m_email, "YwqP-82h");
            }
         } catch (NumberFormatException var6) {
         }

         this.validate();
      }

   }

   public boolean isConfirmationEnabled() {
      String value = this.getConfigText("Confirm/Confirm");
      return Boolean.valueOf(value);
   }

   public boolean isValid() {
      return this.getErrors().size() == 0;
   }

   public String key(String keyName) {
      return this.getMessages().keyDefault(keyName, "");
   }

   private CmsXmlContent getConfigContent() {
      if (this.m_configContent == null) {
         try {
            String uri = this.getRequestContext().getUri();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(this.getRequest().getParameter("file"))) {
               uri = this.getRequest().getParameter("file");
            }

            CmsFile file = this.getCmsObject().readFile(uri);
            this.m_configContent = CmsXmlContentFactory.unmarshal(this.getCmsObject(), file);
         } catch (CmsException var3) {
         }
      }

      return this.m_configContent;
   }

   private CmsMacroResolver getMacroResolver() {
      if (this.m_resolver == null) {
         this.m_resolver = CmsMacroResolver.newInstance();
         this.m_resolver.setCmsObject(this.getCmsObject());
         this.m_resolver.addMacro("email", this.getEmail());
      }

      return this.m_resolver;
   }

   private CmsNewsletterManager getNewsletterManager() {
      CmsModule module = OpenCms.getModuleManager().getModule(CmsNewsletterManager.MODULE_NAME);
      return (CmsNewsletterManager)module.getActionInstance();
   }

   private boolean sendConfirmationMail(String subject, String text) {
      CmsHtmlMail mail = new CmsHtmlMail();

      try {
         mail.addTo(this.getEmail());
         mail.setFrom(this.getConfigText("Confirm/Mail/MailFrom"));
         mail.setSubject(subject);
         this.setTitleMacro(subject);
         mail.setCharset(this.getCmsObject().getRequestContext().getEncoding());
         StringBuffer msg = new StringBuffer(4096);
         String mailHead = "";
         String mailFoot = "";
         boolean foundExternalConfig = false;
         if (this.getConfigContent().hasValue("Confirm/Mail/ConfFile", this.getRequestContext().getLocale())) {
            String path = this.getConfigText("Confirm/Mail/ConfFile");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(path) && this.getCmsObject().existsResource(path) && !CmsResource.isFolder(path)) {
               CmsFile mailConfig = this.getCmsObject().readFile(path);
               CmsXmlContent mailContent = CmsXmlContentFactory.unmarshal(this.getCmsObject(), mailConfig);
               mailHead = mailContent.getStringValue(this.getCmsObject(), "MailHead", this.getRequestContext().getLocale());
               mailFoot = mailContent.getStringValue(this.getCmsObject(), "MailFoot", this.getRequestContext().getLocale());
               foundExternalConfig = true;
            }
         }

         if (!foundExternalConfig) {
            mailHead = this.getConfigText("Confirm/Mail/MailHead");
            mailFoot = this.getConfigText("Confirm/Mail/MailFoot");
         }

         msg.append(this.getMacroResolver().resolveMacros(mailHead));
         msg.append(text);
         msg.append(this.getMacroResolver().resolveMacros(mailFoot));
         mail.setHtmlMsg(msg.toString());
         mail.setTextMsg(CmsHtmlExtractor.extractText(text, this.getCmsObject().getRequestContext().getEncoding()));
         mail.send();
         return true;
      } catch (Exception var11) {
         if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key("LOG_ERROR_MAIL_CONFIRMATION_1", this.getEmail()), var11);
         }

         return false;
      }
   }

   private void setAction(int action) {
      this.m_action = action;
   }

   private void setLinkMacro(int action, String email) {
      StringBuffer result = new StringBuffer(1024);
      result.append("<a href=\"");
      result.append(OpenCms.getSiteManager().getCurrentSite(this.getCmsObject()).getUrl());
      StringBuffer link = new StringBuffer(1024);
      link.append(this.getRequestContext().getUri());
      link.append("?").append("action").append("=").append(action);
      link.append("&").append("email").append("=").append(CmsStringCrypter.encrypt(email, "YwqP-82h"));
      result.append(this.link(link.toString()));
      result.append("\">");
      result.append(OpenCms.getSiteManager().getCurrentSite(this.getCmsObject()).getUrl());
      result.append(this.link(link.toString()));
      result.append("</a>");
      this.getMacroResolver().addMacro("link", result.toString());
   }

   private void setTitleMacro(String title) {
      this.getMacroResolver().addMacro("title", title);
   }

   private void validate() {
      if (this.getAction() < 0 || this.getAction() > 3) {
         this.m_errors.add(this.key("validation.alknewsletter.error.action"));
         this.setAction(-1);
      }

      boolean resetAction = false;
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getEmail())) {
         this.m_errors.add(this.key("validation.alknewsletter.error.noemail"));
         resetAction = true;
      } else if (!CmsNewsletterManager.isValidEmail(this.getEmail())) {
         this.m_errors.add(this.key("validation.alknewsletter.error.invalidemail"));
         resetAction = true;
      }

      if (!resetAction) {
         if (this.getAction() == 0 && this.getNewsletterManager().existsNewsletterUser(this.getEmail(), this.getConfigText("MailingList"))) {
            this.m_errors.add(this.key("validation.alknewsletter.error.userexists"));
            resetAction = true;
         } else if (this.getAction() == 1 && !this.getNewsletterManager().existsNewsletterUser(this.getEmail(), this.getConfigText("MailingList"))) {
            this.m_errors.add(this.key("validation.alknewsletter.error.usernotexists"));
            resetAction = true;
         }
      }

      if (resetAction && (this.getAction() == 0 || this.getAction() == 1)) {
         this.setAction(-1);
      }

   }
}
