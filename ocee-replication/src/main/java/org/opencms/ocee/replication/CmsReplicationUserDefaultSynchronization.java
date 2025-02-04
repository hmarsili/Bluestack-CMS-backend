package org.opencms.ocee.replication;

import org.opencms.file.CmsUser;
import org.opencms.util.CmsStringUtil;

public class CmsReplicationUserDefaultSynchronization implements I_CmsReplicationUserSynchronization {
   public CmsUser update(CmsUser localUser, CmsUser remoteUser) {
      return remoteUser;
   }

   public CmsUser commit(CmsUser localUser, CmsUser remoteUser) {
      return localUser;
   }

   public boolean needsUpdate(CmsUser localUser, CmsUser remoteUser) {
      if (localUser != null && remoteUser != null) {
         String value = (String)remoteUser.getAdditionalInfo("USER_LASTMODIFIED");
         if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            return false;
         } else {
            long lastModified = Long.parseLong(value);
            value = (String)localUser.getAdditionalInfo("USER_LASTMODIFIED");
            long localModified = Long.MIN_VALUE;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
               localModified = Long.parseLong(value);
            }

            return lastModified > localModified;
         }
      } else {
         return true;
      }
   }
}
