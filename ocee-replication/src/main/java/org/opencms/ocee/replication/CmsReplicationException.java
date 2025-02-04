package org.opencms.ocee.replication;

import org.opencms.file.CmsDataAccessException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

public class CmsReplicationException extends CmsDataAccessException {
   private static final long o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = -8356114269497765746L;

   public CmsReplicationException(CmsMessageContainer container) {
      super(container);
   }

   public CmsReplicationException(CmsMessageContainer container, Throwable cause) {
      super(container, cause);
   }

   public CmsException createException(CmsMessageContainer container, Throwable cause) {
      return new CmsReplicationException(container, cause);
   }
}
