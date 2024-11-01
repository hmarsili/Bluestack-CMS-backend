package com.alkacon.opencms.newsletter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

public abstract class A_CmsNewsletterMailData implements I_CmsNewsletterMailData {
   protected static final String NODE_BCC = "BCC";
   protected static final String NODE_FROM = "From";
   protected static final String NODE_SUBJECT = "Subject";
   private static final Log LOG = CmsLog.getLog(A_CmsNewsletterMailData.class);
   private CmsObject m_cms;
   private CmsXmlContent m_content;
   private CmsGroup m_group;
   private CmsJspActionElement m_jsp;
   private Locale m_locale;

   public CmsXmlContent getContent() {
      return this.m_content;
   }

   public abstract Email getEmail() throws CmsException;

   public abstract String getEmailContentPreview() throws CmsException;

   public List getRecipients() throws CmsException {
      List recipients = new ArrayList();
      Iterator i = this.getCms().getUsersOfGroup(this.getGroup().getName()).iterator();

      while(true) {
         CmsUser user;
         do {
            if (!i.hasNext()) {
               if (this.getContent().hasValue("BCC", this.getLocale())) {
                  try {
                     recipients.add(new InternetAddress(this.getContent().getStringValue(this.getCms(), "BCC", this.getLocale())));
                  } catch (MessagingException var5) {
                     if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_EMAIL_BCC_2", this.getContent().getStringValue(this.getCms(), "BCC", this.getLocale()), this.getContent().getFile().getRootPath()));
                     }
                  }
               }

               return recipients;
            }

            user = (CmsUser)i.next();
         } while(!CmsNewsletterManager.isActiveUser(user, this.getGroup().getName()));

         try {
            recipients.add(new InternetAddress(user.getEmail()));
         } catch (MessagingException var6) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_EMAIL_3", user.getEmail(), user.getName(), this.getContent().getFile().getRootPath()));
            }
         }
      }
   }

   public abstract String getResourceTypeName();

   public void initialize(CmsJspActionElement jsp, CmsGroup group, String fileName) throws CmsException {
      this.m_cms = jsp.getCmsObject();
      CmsFile file = this.getCms().readFile(fileName);
      this.m_content = CmsXmlContentFactory.unmarshal(this.getCms(), file);
      this.m_group = group;
      this.m_jsp = jsp;
      this.m_locale = OpenCms.getLocaleManager().getDefaultLocale(this.getCms(), fileName);
   }

   public boolean isSendable() throws CmsException {
      CmsFile file = this.getContent().getFile();
      String resourceName = this.getCms().getSitePath(file);
      CmsLock lock = this.getCms().getLock(file);
      boolean unLocked = false;
      if (lock.isNullLock()) {
         unLocked = true;
         this.getCms().lockResource(resourceName);
         lock = this.getCms().getLock(file);
      }

      if (lock.isOwnedBy(this.getCms().getRequestContext().currentUser())) {
         String value = "" + System.currentTimeMillis() + '|';
         value = value + this.getGroup().getId();
         CmsProperty property = new CmsProperty("newsletter", value, (String)null, true);
         this.getCms().writePropertyObject(resourceName, property);

         try {
            this.getCms().unlockResource(resourceName);
            unLocked = false;
            OpenCms.getPublishManager().publishResource(this.getCms(), resourceName);
         } catch (Exception var8) {
         }

         if (unLocked) {
            this.getCms().unlockResource(resourceName);
         }

         return true;
      } else {
         return false;
      }
   }

   protected CmsObject getCms() {
      return this.m_cms;
   }

   protected CmsGroup getGroup() {
      return this.m_group;
   }

   protected CmsJspActionElement getJsp() {
      return this.m_jsp;
   }

   protected Locale getLocale() {
      return this.m_locale;
   }
}
