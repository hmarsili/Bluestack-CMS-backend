/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/I_CmsXmlContentContainer.java,v $
 * Date   : $Date: 2011/03/23 14:51:36 $
 * Version: $Revision: 1.7 $
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

import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;

/**
 * Provides access to a <code>{@link org.opencms.xml.I_CmsXmlDocument}</code> document that was previously loaded by a parent tag.<p> 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.2.0 
 */
public interface I_CmsXmlContentContainer {

    /**
     * Returns the name of the currently used XML content collector.<p>
     * 
     * @return the name of the currently used XML content collector
     */
    String getCollectorName();

    /**
     * Returns the parameters of the currently used XML content collector.<p>
     * 
     * @return the parameters of the currently used XML content collector
     */
    String getCollectorParam();

    /**
     * Returns the list of all currently loaded XML content documents (instances of <code>{@link I_CmsXmlDocument}</code>).<p>
     * 
     * @return the list of all currently loaded XML content documents
     */
    List getCollectorResult();

    /**
     * Returns the resource name in the VFS for the currently loaded XML content document.<p>
     *
     * @return the resource name in the VFS for the currently loaded XML content document
     */
    String getResourceName();

    /**
     * Returns the currently loaded OpenCms XML content document.<p>
     *
     * @return the currently loaded OpenCms XML content document
     */
    I_CmsXmlDocument getXmlDocument();

    /**
     * Returns the currently selected element name in the loaded XML content document.<p>
     * 
     * @return the currently selected element name in the loaded XML content document
     */
    String getXmlDocumentElement();

    /**
     * Returns the currently selected locale used for acessing the content in the loaded XML content document.<p>
     * 
     * @return the currently selected locale used for acessing the content in the loaded XML content document
     */
    Locale getXmlDocumentLocale();

    /**
     * Content iteration method to be used by JSP scriptlet code.<p>
     * 
     * Calling this method will insert "direct edit" HTML to the output page (if required).<p>
     * 
     * @return <code>true</code> if more content is to be iterated
     * 
     * @throws JspException in case something goes wrong
     */
    boolean hasMoreContent() throws JspException;

    /**
     * Returns <code>true</code> if this container is used as a content preloader.<p> 
     * 
     * A content preloader is used to load content without looping through it.<p> 
     * 
     * @return <code>true</code> if this container is used as a content preloader
     */
    boolean isPreloader();
}