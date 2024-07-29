package org.opencms.security;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

public class CmsUnknownUserException extends CmsAuthentificationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3930126535784075885L;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsUnknownUserException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsUnknownUserException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    @Override
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsUnknownUserException(container, cause);
    }

}
