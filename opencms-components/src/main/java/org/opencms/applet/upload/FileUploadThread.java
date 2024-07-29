
package org.opencms.applet.upload;

/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/FileUploadThread.java,v $
 * Date   : $Date: 2011/03/23 14:56:55 $
 * Version: $Revision: 1.17 $
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

/**
 * File Upload Applet Thread, creates the upload animation.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.17 $ 
 * 
 * @since 6.0.0 
 */
public class FileUploadThread extends Thread {

    /** The upload applet. */
    FileUploadApplet m_fileupload;

    /**
     * Initializes the thread. <p>
     * 
     * @param fileupload reference to the upload applet
     */
    public void init(FileUploadApplet fileupload) {

        m_fileupload = fileupload;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        while (true) {
            m_fileupload.moveFloater();
            try {
                sleep(50);
            } catch (Exception e) {
                // noop
            }
        }
    }
}