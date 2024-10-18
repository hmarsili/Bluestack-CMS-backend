/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/history/CmsHistoryClearReport.java,v $
 * Date   : $Date: 2011/03/23 14:52:54 $
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
 
package org.opencms.workplace.tools.history;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.list.A_CmsListReport;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

/**
 * Provides a report for clearing the history.<p> 
 *
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.9.1
 */
public class CmsHistoryClearReport extends A_CmsListReport {

    /** Request parameter name for the class name to get the dialog object from. */
    public static final String PARAM_CLASSNAME = "classname";

    /** Request parameter for the class name to get the dialog object from. */
    private String m_paramClassname;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHistoryClearReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHistoryClearReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Returns the request parameter value for the class name to get the dialog object from.<p>
     * 
     * @return the request parameter value for the class name to get the dialog object from
     */
    public String getParamClassname() {

        return m_paramClassname;
    }
    
    /** 
     * Sets the request parameter value for the class name to get the dialog object from.<p>
     * 
     * @param className the request parameter value for the class name to get the dialog object from
     */
    public void setParamClassname(String className) {

        m_paramClassname = className;
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() {

        CmsHistoryClear historyClear = (CmsHistoryClear)((Map)getSettings().getDialogObject()).get(getParamClassname());

        I_CmsReportThread clearHistoryThread = new CmsHistoryClearThread(getCms(), historyClear);

        return clearHistoryThread;
    }

}
