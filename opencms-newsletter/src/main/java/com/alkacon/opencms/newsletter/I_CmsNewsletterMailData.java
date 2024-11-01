package com.alkacon.opencms.newsletter;

import java.util.List;
import org.apache.commons.mail.Email;
import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;

public interface I_CmsNewsletterMailData {
   CmsXmlContent getContent();

   Email getEmail() throws CmsException;

   String getEmailContentPreview() throws CmsException;

   List getRecipients() throws CmsException;

   String getResourceTypeName();

   void initialize(CmsJspActionElement var1, CmsGroup var2, String var3) throws CmsException;

   boolean isSendable() throws CmsException;
}
