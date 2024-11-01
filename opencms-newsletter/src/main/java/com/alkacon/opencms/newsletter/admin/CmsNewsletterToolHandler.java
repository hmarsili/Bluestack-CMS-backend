package com.alkacon.opencms.newsletter.admin;

import com.alkacon.opencms.newsletter.CmsNewsletterManager;
import org.opencms.main.CmsException;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsDefaultToolHandler;

public class CmsNewsletterToolHandler extends CmsDefaultToolHandler {
   public boolean isEnabled(CmsWorkplace wp) {
      if (this.getPath().equals("/newsletter")) {
         try {
            return !CmsNewsletterManager.getOrgUnits(wp.getCms()).isEmpty();
         } catch (CmsException var3) {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean isVisible(CmsWorkplace wp) {
      try {
         return !CmsNewsletterManager.getOrgUnits(wp.getCms()).isEmpty();
      } catch (CmsException var3) {
         return false;
      }
   }
}
