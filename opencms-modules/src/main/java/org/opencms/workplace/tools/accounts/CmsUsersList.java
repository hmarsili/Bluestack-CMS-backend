/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsUsersList.java,v $
 * Date   : $Date: 2011/03/23 14:51:06 $
 * Version: $Revision: 1.22 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main system user account management view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.22 $ 
 * 
 * @since 6.0.0 
 */
public class CmsUsersList extends A_CmsUsersList {

    /** list id constant. */
    public static final String LIST_ID = "lsu";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUsersList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_USERS_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getGroupIcon()
     */
    @Override
    protected String getGroupIcon() {

        return PATH_BUTTONS + "group.png";
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getUsers()
     */
    @Override
    protected List getUsers() throws CmsException {

        return CmsPrincipal.filterCore(OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#readUser(java.lang.String)
     */
    @Override
    protected CmsUser readUser(String name) throws CmsException {

        return getCms().readUser(name);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setDeleteAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {

        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_HELP_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setEditAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setEditAction(CmsListColumnDefinition editCol) {

        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_HELP_0));
        editAction.setIconPath(PATH_BUTTONS + "user.png");
        editCol.addDirectAction(editAction);
    }
}