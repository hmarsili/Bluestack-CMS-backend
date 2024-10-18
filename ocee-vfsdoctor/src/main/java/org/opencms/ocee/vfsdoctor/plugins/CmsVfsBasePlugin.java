package org.opencms.ocee.vfsdoctor.plugins;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.util.CmsUUID;

public class CmsVfsBasePlugin extends A_CmsVfsDoctorPlugin {
   public CmsVfsBasePlugin() {
      super(false, false);
   }

   public CmsMessageContainer getDescription() {
      return null;
   }

   public String getName() {
      return "base";
   }

   public CmsMessageContainer getNiceName() {
      return null;
   }

   protected int recover(CmsUUID projectId) {
      return 0;
   }

   protected int validate(CmsUUID projectId) {
      return 0;
   }
}
