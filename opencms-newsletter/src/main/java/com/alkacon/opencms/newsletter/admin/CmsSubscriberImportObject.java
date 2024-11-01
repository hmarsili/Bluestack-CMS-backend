package com.alkacon.opencms.newsletter.admin;

import com.alkacon.opencms.newsletter.CmsNewsletterManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

public class CmsSubscriberImportObject {
   private List m_convertedLines;
   private String m_importEmail;
   private List m_invalidLines;

   public List getConvertedLines() {
      return this.m_convertedLines == null ? Collections.EMPTY_LIST : this.m_convertedLines;
   }

   public List getEmailAddresses() {
      List result = new ArrayList();
      this.m_convertedLines = new ArrayList();
      this.m_invalidLines = new ArrayList();
      BufferedReader bufferedReader = new BufferedReader(new StringReader(this.getImportEmail()));

      try {
         String line;
         while((line = bufferedReader.readLine()) != null) {
            if (CmsNewsletterManager.isValidEmail(line)) {
               result.add(line);
            } else {
               List lineEntries = CmsStringUtil.splitAsList(line, ' ');
               Iterator i = lineEntries.iterator();
               boolean foundEmail = false;

               while(i.hasNext()) {
                  String testEntry = (String)i.next();
                  if (CmsNewsletterManager.isValidEmail(testEntry)) {
                     result.add(testEntry);
                     this.m_convertedLines.add(new String[]{line, testEntry});
                     foundEmail = true;
                     break;
                  }
               }

               if (!foundEmail) {
                  this.m_invalidLines.add(line);
               }
            }
         }

         bufferedReader.close();
      } catch (IOException var8) {
      }

      return result;
   }

   public String getImportEmail() {
      return this.m_importEmail;
   }

   public List getInvalidLines() {
      return this.m_invalidLines == null ? Collections.EMPTY_LIST : this.m_invalidLines;
   }

   public void setImportEmail(String importEmail) throws Exception {
      this.m_importEmail = importEmail;
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(importEmail)) {
         throw new CmsException(Messages.get().container("ERR_SUBSCRIBER_IMPORT_NO_CONTENT_0"));
      }
   }
}
