/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsMultiListDialog.java,v $
 * Date   : $Date: 2011/03/23 14:51:20 $
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

package org.opencms.workplace.list;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;

/**
 * Helper class for managing three lists on the same dialog.<p>
 * 
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMultiListDialog {

    /** the workplace instance for the active list. */
    private A_CmsListDialog m_activeWp;

    /** the workplace instances for the lists. */
    private List m_wps;

    /**
     * Default constructor.<p>
     * 
     * @param wps the lists to be displayed
     */
    public CmsMultiListDialog(List wps) {

        m_wps = wps;
        Iterator i = m_wps.iterator();
        while (i.hasNext()) {
            A_CmsListDialog wp = (A_CmsListDialog)i.next();
            if (wp.isActive()) {
                m_activeWp = wp;
            }
        }
        if (m_activeWp == null) {
            m_activeWp = (A_CmsListDialog)m_wps.get(0);
        }
    }

    /**
     * Display method for two list dialogs.<p>
     * 
     * @throws JspException if dialog actions fail
     * @throws IOException if writing to the JSP out fails, or in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public void displayDialog() throws JspException, IOException, ServletException {

        displayDialog(false);
    }

    /**
     * Display method for two list dialogs, executes actions, but only displays if needed.<p>
     * 
     * @param writeLater if <code>true</code> no output is written, 
     *                   you have to call manually the <code>{@link #defaultActionHtml()}</code> method.
     * 
     * @throws JspException if dialog actions fail
     * @throws IOException if writing to the JSP out fails, or in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public void displayDialog(boolean writeLater) throws JspException, IOException, ServletException {

        // perform the active list actions
        m_activeWp.actionDialog();
        if (m_activeWp.isForwarded()) {
            return;
        }

        Iterator i = m_wps.iterator();
        while (i.hasNext()) {
            A_CmsListDialog wp = (A_CmsListDialog)i.next();
            wp.refreshList();
        }

        if (writeLater) {
            return;
        }
        writeDialog();
    }

    /**
     * Returns the activeWp.<p>
     *
     * @return the activeWp
     */
    public A_CmsListDialog getActiveWp() {

        return m_activeWp;
    }

    /**
     * Returns <code>true</code> if one of the lists has been forwarded.<p>
     * 
     * @return <code>true</code> if one of the lists has been forwarded
     */
    public boolean isForwarded() {

        Iterator i = m_wps.iterator();
        while (i.hasNext()) {
            A_CmsListDialog wp = (A_CmsListDialog)i.next();
            if (wp.isForwarded()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes the dialog html code, only if the <code>{@link org.opencms.workplace.CmsDialog#ACTION_DEFAULT}</code> is set.<p>
     * 
     * @throws IOException if writing to the JSP out fails, or in case of errros forwarding to the required result page
     */
    public void writeDialog() throws IOException {

        JspWriter out = m_activeWp.getJsp().getJspContext().getOut();
        out.print(defaultActionHtml());
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     */
    protected String defaultActionHtml() {

        StringBuffer result = new StringBuffer(2048);
        result.append(defaultActionHtmlStart());
        result.append(defaultActionHtmlContent());
        result.append(defaultActionHtmlEnd());
        return result.toString();
    }

    /**
     * Returns the html code for the default action content.<p>
     * 
     * @return html code
     */
    protected String defaultActionHtmlContent() {

        StringBuffer result = new StringBuffer(2048);
        result.append("<table id='twolists' cellpadding='0' cellspacing='0' align='center' width='100%'>\n");
        Iterator i = m_wps.iterator();
        while (i.hasNext()) {
            A_CmsListDialog wp = (A_CmsListDialog)i.next();
            result.append("\t<tr>\n");
            result.append("\t\t<td valign='top'>\n");
            result.append("\t\t\t").append(wp.defaultActionHtmlContent()).append("\n");
            result.append("\t\t</td>\n");
            result.append("\t</tr>\n");
            result.append("\t<tr><td height='20'/></tr>\n");
        }
        result.append("</table>\n");
        return result.toString();
    }

    /**
     * Generates the dialog ending html code.<p>
     * 
     * @return html code
     */
    protected String defaultActionHtmlEnd() {

        return m_activeWp.defaultActionHtmlEnd() + m_activeWp.dialogContentEnd();
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     */
    protected String defaultActionHtmlStart() {

        return m_activeWp.getList().listJs() + m_activeWp.dialogContentStart(getActiveWp().getParamTitle());
    }
}
