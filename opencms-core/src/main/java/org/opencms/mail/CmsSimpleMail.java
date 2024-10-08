/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsSimpleMail.java,v $
 * Date   : $Date: 2011/03/23 14:52:24 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.mail;

import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import javax.mail.AuthenticationFailedException;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * This class is used to send simple text internet email messages without
 * attachments.<p>
 * 
 * It uses the Apache Commons Email API and extends the provided classes to
 * conveniently generate emails using the OpenCms configuration.<p>
 * 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.19 $
 * 
 * @since 6.0.0
 */
public class CmsSimpleMail extends SimpleEmail {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSimpleMail.class);

    /**
     * Default constructor of a CmsSimpleMail.<p>
     * 
     * The mail host name and the mail from address are set to the OpenCms
     * default values of the configuration.<p>
     * 
     */
    public CmsSimpleMail() {

        // call super constructor
        super();
        // set the host to the default mail host
        CmsMailHost host = OpenCms.getSystemInfo().getMailSettings().getDefaultMailHost();
        setHostName(host.getHostname());
        this.setSmtpPort(host.getPort());
        
        // check if username and password are provided
        String userName = host.getUsername();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userName)) {
            // authentication needed, set user name and password
            setAuthentication(userName, host.getPassword());
        }
        try {
            // set default mail from address
            setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
        } catch (EmailException e) {
            // default email address is not valid, log error
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_SENDER_ADDRESS_0), e);
        }
    }

    /**
     * Overrides to add a better message for authentication exception.<p>
     * 
     * @see org.apache.commons.mail.Email#send()
     */
    @Override
    public String send() {

        String messageID = null;
        try {
            messageID = super.send();
        } catch (EmailException e) {
            // check if original Exception is of type SendFailedException which
            // should have been thrown by javax.mail.Transport.send()
            if (e.getCause() instanceof AuthenticationFailedException) {
                CmsMailHost host = OpenCms.getSystemInfo().getMailSettings().getDefaultMailHost();
                // wrong user credentials in opencms-system.xml: mail api does not provide a message for authentication exception

                CmsRuntimeException rte = new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_SEND_EMAIL_AUTHENTICATE_2,
                    host.getUsername(),
                    host.getHostname()));
                rte.initCause(e);
                throw rte;

            } else {
                CmsRuntimeException rte = new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_SEND_EMAIL_CONFIG_0));
                rte.initCause(e);
                throw rte;
            }
        }
        return messageID;
    }
}
