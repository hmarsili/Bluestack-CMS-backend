/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/I_CmsJspTagContentContainer.java,v $
 * Date   : $Date: 2011/03/23 14:51:35 $
 * Version: $Revision: 1.15 $
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

package org.opencms.jsp;

import org.opencms.xml.I_CmsXmlDocument;

/**
 * Provides access to a <code>{@link org.opencms.xml.I_CmsXmlDocument}</code> document that was loaded by a parent tag.<p> 
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 6.0.0 
 * 
 * @deprecated this interface has been renamed to <code>{@link org.opencms.jsp.I_CmsXmlContentContainer}</code>
 */
public interface I_CmsJspTagContentContainer extends I_CmsXmlContentContainer {

    /**
     * Returns the currently loaded OpenCms XML content document.<p>
     *
     * @return the currently loaded OpenCms XML content document
     */
    I_CmsXmlDocument getXmlDocument();
}