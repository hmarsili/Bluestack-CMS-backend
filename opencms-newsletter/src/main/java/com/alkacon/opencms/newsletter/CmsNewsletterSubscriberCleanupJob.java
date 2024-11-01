package com.alkacon.opencms.newsletter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.security.CmsOrganizationalUnit;

public class CmsNewsletterSubscriberCleanupJob implements I_CmsScheduledJob {
   public static final String PARAM_MAXAGE = "maxage";
   private static final Log LOG = CmsLog.getLog(CmsNewsletterSubscriberCleanupJob.class);

   public String launch(CmsObject cms, Map parameters) throws Exception {
      String maxAgeStr = (String)parameters.get("maxage");

      float maxAge;
      try {
         maxAge = Float.parseFloat(maxAgeStr);
      } catch (Exception var8) {
         maxAge = 168.0F;
      }

      long expireDate = System.currentTimeMillis() - (long)(maxAge * 60.0F * 60.0F * 1000.0F);
      int count = this.removeInactiveSubscribers(cms, expireDate);
      return Messages.get().getBundle().key("LOG_NEWSLETTER_CLEANUP_FINISHED_COUNT_1", new Integer(count));
   }

   private int removeInactiveSubscribers(CmsObject cms, long expireDate) {
      int count = 0;

      try {
         List newsletterUnits = CmsNewsletterManager.getOrgUnits(cms);
         Iterator i = newsletterUnits.iterator();

         while(i.hasNext()) {
            CmsOrganizationalUnit ou = (CmsOrganizationalUnit)i.next();
            List users = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), false);
            Iterator k = users.iterator();

            while(k.hasNext()) {
               CmsUser user = (CmsUser)k.next();
               Boolean active = (Boolean)user.getAdditionalInfo("AlkNewsletter_ActiveUser:");
               if (active != null && !active && user.getDateCreated() < expireDate) {
                  cms.deleteUser(user.getName());
                  ++count;
               }
            }
         }
      } catch (CmsException var12) {
         if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key("LOG_NEWSLETTER_CLEANUP_ERROR_DELETING_0"), var12);
         }
      }

      return count;
   }
}
