/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.workplace.demos.widget;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsContextInfo;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * A basic example and proof-of-concept on how to use OpenCms widgets within a custom build form
 * without XML contents.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision$ 
 * 
 * @since 6.0.0 
 */
public class CmsAdminWidgetDemo6 extends CmsWidgetDialog {

    /** Value for the action: display dialog page 1. */
    public static final int ACTION_DISPLAY_PAGE_1 = 301;

    /** Value for the action: display dialog page 2. */
    public static final int ACTION_DISPLAY_PAGE_2 = 302;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "widgetdemo6";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGE_ARRAY = {"page1", "page2"};

    /** The allowed pages for this dialog in a List. */
    public static final List PAGE_LIST = Arrays.asList(PAGE_ARRAY);

    /** The OpenCms context info object used for the job info. */
    CmsContextInfo m_contextInfo;

    /** The job info object that is edited on this dialog. */
    CmsScheduledJobInfo m_jobInfo;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminWidgetDemo6(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminWidgetDemo6(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        // not implemented for this demo

    }

    /**
     * Builds the HTML for the dialog form.<p>
     * 
     * @return the HTML for the dialog form
     */
    @Override
    public String buildDialogForm() {

        StringBuffer result = new StringBuffer(1024);

        try {
            // create the dialog HTML
            result.append(createDialogHtml());
        } catch (Throwable t) {
            // since this is just a simple example...
            t.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Creats the HTML for the buttons on the dialog.<p>
     * 
     * @return the HTML for the buttons on the dialog.<p>
     */
    @Override
    public String dialogButtonsCustom() {

        if (PAGE_ARRAY[1].equals(getParamPage())) {
            // this is the second dialog page
            return dialogButtons(new int[] {BUTTON_OK, BUTTON_BACK, BUTTON_CANCEL}, new String[3]);
        } else {
            return dialogButtons(new int[] {BUTTON_CONTINUE, BUTTON_CANCEL}, new String[2]);
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
    @Override
    public String getCancelAction() {

        // set the default action
        setParamPage(PAGE_ARRAY[0]);

        return DIALOG_SET;
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        Object o = getSettings().getDialogObject();
        if (!(o instanceof CmsScheduledJobInfo)) {
            // create a new job info
            m_jobInfo = new CmsScheduledJobInfo();
            m_contextInfo = new CmsContextInfo();
            m_jobInfo.setContextInfo(m_contextInfo);
        } else {
            // reuse job info object stored in session
            m_jobInfo = (CmsScheduledJobInfo)o;
            m_contextInfo = m_jobInfo.getContextInfo();
        }

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "jobName", PAGE_ARRAY[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "className", PAGE_ARRAY[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "cronExpression", PAGE_ARRAY[0], new CmsInputWidget()));

        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "userName", PAGE_ARRAY[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "projectName", PAGE_ARRAY[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "siteRoot", PAGE_ARRAY[0], new CmsVfsFileWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "requestedUri", PAGE_ARRAY[0], new CmsVfsFileWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "localeName", PAGE_ARRAY[1], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "encoding", PAGE_ARRAY[1], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "remoteAddr", PAGE_ARRAY[1], new CmsInputWidget()));

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "reuseInstance", PAGE_ARRAY[1], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "active", PAGE_ARRAY[1], new CmsCheckboxWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGE_ARRAY;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        addMessages(org.opencms.workplace.demos.Messages.get().getBundleName());
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamPage()) || !PAGE_LIST.contains(getParamPage())) {
            // ensure a valid page is set
            setParamPage(PAGE_ARRAY[0]);
        }

        // fill the widget map
        defineWidgets();
        fillWidgetValues(request);

        // set the action for the JSP switch 
        if (DIALOG_SAVE.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_SAVE);
            List errors = commitWidgetValues();
            if (errors.size() > 0) {
                Iterator i = errors.iterator();
                while (i.hasNext()) {
                    Exception e = (Exception)i.next();
                    System.err.println(e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                    }
                }
                setAction(ACTION_DEFAULT);
            }
        } else if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_CANCEL);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else if (EDITOR_ACTION_ELEMENT_ADD.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_ADD);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else if (EDITOR_ACTION_ELEMENT_REMOVE.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_REMOVE);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else if (DIALOG_BACK.equals(getParamAction())) {

            setAction(ACTION_DEFAULT);
            List errors = commitWidgetValues(PAGE_ARRAY[1]);

            if (errors.size() > 0) {
                setAction(ACTION_ERROR);
                try {
                    includeErrorpage(this, (Throwable)errors.get(0));
                } catch (JspException e) {
                    // ignore
                }
                return;
            }

            setParamPage(PAGE_ARRAY[0]);

        } else if (DIALOG_CONTINUE.equals(getParamAction())) {

            setAction(ACTION_DEFAULT);
            List errors = commitWidgetValues(PAGE_ARRAY[0]);

            if (errors.size() > 0) {
                setAction(ACTION_ERROR);
                try {
                    includeErrorpage(this, (Throwable)errors.get(0));
                } catch (JspException e) {
                    // ignore
                }
                return;
            }

            setParamPage(PAGE_ARRAY[1]);

        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
        }

        // save the current state of the job (may be changed because of the widget values)
        getSettings().setDialogObject(m_jobInfo);
    }
}
