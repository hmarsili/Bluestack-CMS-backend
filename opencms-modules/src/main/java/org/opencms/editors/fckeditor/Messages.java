/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/fckeditor/Messages.java,v $
 * Date   : $Date: 2011/03/23 14:53:09 $
 * Version: $Revision: 1.8 $
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

package org.opencms.editors.fckeditor;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.2.0 
 */
public final class Messages extends A_CmsMessageBundle {
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_TITLE_FCKEDITOR_0 = "GUI_EDITOR_TITLE_FCKEDITOR_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_AJAX_STATE_WAIT_0 = "GUI_AJAX_STATE_WAIT_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_AJAX_STATE_ERROR_0 = "GUI_AJAX_STATE_ERROR_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_AJAX_STATE_GIVEUP_0 = "GUI_AJAX_STATE_GIVEUP_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_WRITE_OUT_0 = "LOG_ERR_WRITE_OUT_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.editors.fckeditor.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}
