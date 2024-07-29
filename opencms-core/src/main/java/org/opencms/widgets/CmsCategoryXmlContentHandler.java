/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsCategoryXmlContentHandler.java,v $
 * Date   : $Date: 2008/07/14 10:04:27 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.xml.content.CmsDefaultXmlContentHandler;

/**
 *  This handler adds the categories to the current resource and all siblings.<p>
 *  
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $
 *  
 * @since 7.0.5
 * 
 * @deprecated no longer needed since logic was moved to the default handler
 */
public class CmsCategoryXmlContentHandler extends CmsDefaultXmlContentHandler {

    /**
     * Default constructor.<p>
     */
    public CmsCategoryXmlContentHandler() {

        super();
    }
}
