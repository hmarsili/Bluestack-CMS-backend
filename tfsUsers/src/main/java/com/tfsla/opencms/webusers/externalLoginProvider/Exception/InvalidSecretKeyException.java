package com.tfsla.opencms.webusers.externalLoginProvider.Exception;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDriverManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

public class InvalidSecretKeyException extends CmsException {

	/** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDriverManager.class);
	

    public InvalidSecretKeyException(CmsMessageContainer container) {

        super(container);
    }
    
	public InvalidSecretKeyException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
        // log all sql exceptions
        if (LOG.isWarnEnabled()) {
            LOG.warn(container.key(), this);
        }
    }

	/**
	 * 
	 */
	private static final long serialVersionUID = 7428391963797764006L;

	
}
