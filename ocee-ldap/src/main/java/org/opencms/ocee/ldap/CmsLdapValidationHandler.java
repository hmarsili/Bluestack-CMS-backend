package org.opencms.ocee.ldap;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.security.CmsDefaultValidationHandler;
import org.opencms.security.Messages;
import org.opencms.util.CmsStringUtil;

public class CmsLdapValidationHandler extends CmsDefaultValidationHandler {
    public void checkEmail(String email) throws CmsIllegalArgumentException {
    }

    public void checkUserName(String userName) throws CmsIllegalArgumentException {
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            throw new CmsIllegalArgumentException(Messages.get().container("ERR_BAD_USERNAME_EMPTY_0", userName));
        }
    }
}
