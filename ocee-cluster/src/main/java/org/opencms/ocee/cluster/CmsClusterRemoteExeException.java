package org.opencms.ocee.cluster;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.main.CmsException;

public class CmsClusterRemoteExeException extends CmsException {
   private static final long serialVersionUID = 4038570230383765837L;

   public CmsClusterRemoteExeException(String msg) {
      super(new CmsMessageContainer((I_CmsMessageBundle)null, msg));
   }

   public CmsClusterRemoteExeException(String msg, Throwable cause) {
      super(new CmsMessageContainer((I_CmsMessageBundle)null, msg), cause);
   }

   public CmsException createException(CmsMessageContainer container, Throwable cause) {
      return new CmsClusterRemoteExeException(container.getKey(), cause);
   }
}
