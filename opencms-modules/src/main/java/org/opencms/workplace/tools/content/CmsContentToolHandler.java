/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsContentToolHandler.java,v $
 * Date   : $Date: 2011/03/23 14:50:18 $
 * Version: $Revision: 1.6 $
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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.workplace.tools.CmsOfflineToolHandler;

/**
 * Element content tool handler that hides the element tools if the current user
 * has not the needed privileges.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsContentToolHandler extends CmsOfflineToolHandler {

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    @Override
    public boolean isEnabled(CmsObject cms) {

        return super.isEnabled(cms)
            && (OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER) || OpenCms.getRoleManager().hasRole(
                cms,
                CmsRole.WORKPLACE_MANAGER));
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    @Override
    public boolean isVisible(CmsObject cms) {

        return OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER)
            || OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER);
    }
}
