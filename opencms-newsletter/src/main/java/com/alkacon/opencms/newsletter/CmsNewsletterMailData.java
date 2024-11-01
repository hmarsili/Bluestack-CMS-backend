package com.alkacon.opencms.newsletter;

import javax.mail.MessagingException;
import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

public class CmsNewsletterMailData extends A_CmsNewsletterMailData {
   protected static final String NODE_CONFFILE = "ConfFile";
   protected static final String NODE_HTML = "Html";
   protected static final String NODE_MAILFOOT = "MailFoot";
   protected static final String NODE_MAILHEAD = "MailHead";
   protected static final String NODE_TEXT = "Text";
   public static final String RESOURCETYPE_NEWSLETTER_NAME = "alkacon-newsletter";
   protected static final String XPATH_CONFIG = "Config/";
   private static final Log LOG = CmsLog.getLog(CmsNewsletterMailData.class);

   public CmsNewsletterMailData() {
   }

   public CmsNewsletterMailData(String fileName, CmsGroup group, CmsJspActionElement jsp) throws CmsException {
      this.initialize(jsp, group, fileName);
   }

   public Email getEmail() throws CmsException {
      String from = this.getContent().getStringValue(this.getCms(), "From", this.getLocale());
      String subject = this.getContent().getStringValue(this.getCms(), "Subject", this.getLocale());
      String text = this.getContent().getStringValue(this.getCms(), "Text", this.getLocale());
      boolean isHtmlMail = Boolean.valueOf(this.getContent().getStringValue(this.getCms(), "Config/Html", this.getLocale()));
      if (isHtmlMail) {
         CmsHtmlMail mail = new CmsHtmlMail();

         try {
            mail.setFrom(from);
         } catch (EmailException var9) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_EMAIL_FROM_2", from, this.getContent().getFile().getRootPath()));
            }
         }

         mail.setSubject(subject);
         try {
			mail.setHtmlMsg(this.getEmailContent());
		} catch (EmailException e) {
			 if (LOG.isErrorEnabled()) {
	               LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_EMAIL_HTML_2", this.getEmailContent(), this.getContent().getFile().getRootPath()));
	            }
		} 

         try {
            text = CmsHtmlExtractor.extractText(text, this.getCms().getRequestContext().getEncoding());
         } catch (Exception var7) {
         }

         try {
        	 mail.setTextMsg(text);
         } catch (EmailException e) {
			 if (LOG.isErrorEnabled()) {
	               LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_EMAIL_TEXT_2", text, this.getContent().getFile().getRootPath()));
	         }
         }
         
         mail.setCharset(this.getCms().getRequestContext().getEncoding());
         return mail;
      } else {
         CmsSimpleMail mail = new CmsSimpleMail();

         try {
            mail.setFrom(from);
         } catch (EmailException var10) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("LOG_ERROR_NEWSLETTER_EMAIL_FROM_2", from, this.getContent().getFile().getRootPath()));
            }
         }

         mail.setSubject(subject);

         try {
            text = CmsHtmlExtractor.extractText(text, this.getCms().getRequestContext().getEncoding());
         } catch (Exception var8) {
         }

         try {
        	 mail.setMsg(text);
         } catch (EmailException var10) {
         
         }
         
         mail.setCharset(this.getCms().getRequestContext().getEncoding());
         return mail;
      }
   }

   public String getEmailContentPreview() throws CmsException {
      String result = this.getEmailContent();
      if (result.indexOf("</body>") == -1) {
         StringBuffer previewHtml = new StringBuffer(result.length() + 256);
         previewHtml.append("<html><head></head><body style=\"background-color: #FFF;\">\n<pre style=\"font-family: Courier New, monospace; font-size: 13px; color: #000;\">");
         previewHtml.append(result);
         previewHtml.append("</pre>\n</body></html>");
         result = previewHtml.toString();
      }

      return result;
   }

   public String getResourceTypeName() {
      return "alkacon-newsletter";
   }

   protected String getEmailContent() throws CmsException {
      String text = this.getContent().getStringValue(this.getCms(), "Text", this.getLocale());
      boolean isHtmlMail = Boolean.valueOf(this.getContent().getStringValue(this.getCms(), "Config/Html", this.getLocale()));
      if (isHtmlMail) {
         StringBuffer mailHtml = new StringBuffer(4096);
         String mailHead = "";
         String mailFoot = "";
         boolean foundExternalConfig = false;
         if (this.getContent().hasValue("Config/ConfFile", this.getLocale())) {
            String path = this.getContent().getStringValue(this.getCms(), "Config/ConfFile", this.getLocale());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(path) && this.getCms().existsResource(path) && !CmsResource.isFolder(path)) {
               CmsFile mailConfig = this.getCms().readFile(path);
               CmsXmlContent mailContent = CmsXmlContentFactory.unmarshal(this.getCms(), mailConfig);
               if (mailContent.hasValue("MailHead", this.getLocale())) {
                  mailHead = mailContent.getStringValue(this.getCms(), "MailHead", this.getLocale());
                  mailFoot = mailContent.getStringValue(this.getCms(), "MailFoot", this.getLocale());
                  foundExternalConfig = true;
               }
            }
         }

         if (!foundExternalConfig) {
            mailHead = this.getContent().getStringValue(this.getCms(), "Config/MailHead", this.getLocale());
            mailFoot = this.getContent().getStringValue(this.getCms(), "Config/MailFoot", this.getLocale());
         }

         mailHtml.append(mailHead);
         mailHtml.append(text);
         mailHtml.append(mailFoot);
         return mailHtml.toString();
      } else {
         try {
            return CmsHtmlExtractor.extractText(text, this.getCms().getRequestContext().getEncoding());
         } catch (Exception var10) {
            return text;
         }
      }
   }
}
