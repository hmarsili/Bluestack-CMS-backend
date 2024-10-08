/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/menu/CmsMirPrSameLockedInactiveMovedAl.java,v $
 * Date   : $Date: 2011/03/23 14:51:55 $
 * Version: $Revision: 1.5 $
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

package org.opencms.workplace.explorer.menu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.workplace.explorer.CmsResourceUtil;

/**
 * Defines a menu item rule that sets the visibility to active if the current resource is changed
 * and locked by the current user or the autolock feature is enabled.<p>
 * 
 * It sets the visibility to inactive for new resources and to invisible for deleted resources,
 * for folders it sets the visibility to active for unchanged resources.<p>
 * 
 * @author Tobias Herrmann  
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 7.0.5
 */
public class CmsMirPrSameLockedInactiveMovedAl extends A_CmsMenuItemRule {

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        try {
            if (!resourceUtil[0].isEditable()
                || !cms.hasPermissions(
                    resourceUtil[0].getResource(),
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.ALL)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_WRITE_0);
            }
        } catch (CmsException e) {
            // error checking permissions, disable entry completely
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_MOVED_0);

    }

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#matches(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    public boolean matches(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        if (resourceUtil[0].isInsideProject() && resourceUtil[0].getResource().getState().isChanged()) {
            CmsLock lock = resourceUtil[0].getLock();
            boolean lockedForPublish = resourceUtil[0].getProjectState().isLockedForPublishing();
            if ((!lockedForPublish && !lock.isShared() && lock.isOwnedInProjectBy(
                cms.getRequestContext().currentUser(),
                cms.getRequestContext().currentProject()))
                || (!lockedForPublish && lock.isNullLock() && OpenCms.getWorkplaceManager().autoLockResources())) {
                try {
                    CmsObject cmsOnline = OpenCms.initCmsObject(cms);
                    cmsOnline.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

                    try {
                        cmsOnline.readResource(resourceUtil[0].getResource().getStructureId());
                        return false;
                    } catch (CmsException ex) {
                        // resource state is changed but not in online-project, so it was moved
                        return true;
                    }
                } catch (CmsException e) {
                    // error reading project
                    return false;
                }
            }

        }
        // resource is not locked by the user in current project or not locked with enabled autolock, rule does not match
        return false;
    }

}
