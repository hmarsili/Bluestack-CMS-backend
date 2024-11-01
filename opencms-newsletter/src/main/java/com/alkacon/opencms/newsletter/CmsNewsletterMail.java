package com.alkacon.opencms.newsletter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.opencms.main.CmsLog;

public class CmsNewsletterMail extends Thread {
   private static final Log LOG = CmsLog.getLog(CmsNewsletterMail.class);
   private Email m_mail;
   private String m_newsletterName;
   private List m_recipients;

   public CmsNewsletterMail(Email mail, List recipients, String newsletterName) {
      this.m_mail = mail;
      this.m_recipients = recipients;
      this.m_newsletterName = newsletterName;
   }

   public void run() {
      try {
         this.sendMail();
      } catch (Throwable var2) {
         if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_SEND_FAILED_1", this.getNewsletterName()), var2);
         }
      }

   }

   public void sendMail() {
      Iterator i = this.getRecipients().iterator();

      while(i.hasNext()) {
         InternetAddress to = (InternetAddress)i.next();
         List toList = new ArrayList(1);
         toList.add(to);
         Email mail = this.getMail();

         try {
        	 
        	 mail.setTo(toList);
        	 
            mail.send();
         } catch (EmailException e) {
        	 if (LOG.isErrorEnabled()) {
                 LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_EMAIL_SEND_FAILED_2", to.getAddress(), this.getNewsletterName()));
              }
		}
      }

   }

   private Email getMail() {
      return this.m_mail;
   }

   private String getNewsletterName() {
      return this.m_newsletterName;
   }

   private List getRecipients() {
      return this.m_recipients;
   }
}
