package org.opencms.ocee.ldap;

import org.opencms.file.CmsDataAccessException;
import org.opencms.i18n.CmsMessageContainer;

public class CmsLdapAccessException extends CmsDataAccessException {
    private static final long f0x226a583a = 4789168286729529248L;

    public CmsLdapAccessException(CmsMessageContainer container) {
        super(container);
    }

    public CmsLdapAccessException(CmsMessageContainer container, Throwable cause) {
        super(container, cause);
    }
}
