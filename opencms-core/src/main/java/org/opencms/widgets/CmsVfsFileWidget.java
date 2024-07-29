/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsVfsFileWidget.java,v $
 * Date   : $Date: 2011/03/23 14:50:13 $
 * Version: $Revision: 1.25 $
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * Provides a OpenCms VFS file selection widget, for use on a widget dialog.<p>
 *
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.25 $ 
 * 
 * @since 6.0.0 
 */
public class CmsVfsFileWidget extends A_CmsWidget {

    /** Configuration parameter to set the flag to include files in popup resource tree. */
    public static final String CONFIGURATION_EXCLUDEFILES = "excludefiles";

    /** Configuration parameter to set the flag to show the site selector in popup resource tree. */
    public static final String CONFIGURATION_HIDESITESELECTOR = "hidesiteselector";

    /** Configuration parameter to set the flag to include files in popup resource tree. */
    public static final String CONFIGURATION_INCLUDEFILES = "includefiles";

    /** Configuration parameter to prevent the project awareness flag in the popup resource tree. */
    public static final String CONFIGURATION_NOTPROJECTAWARE = "notprojectaware";

    /** Configuration parameter to set the project awareness flag in the popup resource tree. */
    public static final String CONFIGURATION_PROJECTAWARE = "projectaware";

    /** Configuration parameter to set the flag to show the site selector in popup resource tree. */
    public static final String CONFIGURATION_SHOWSITESELECTOR = "showsiteselector";

    /** Configuration parameter to set start site of the popup resource tree. */
    public static final String CONFIGURATION_STARTSITE = "startsite";

    /** Flag to determine if files should be shown in popup window. */
    private boolean m_includeFiles;

    /** Flag to determine project awareness, ie. if resources outside of the current project should be displayed as normal. */
    private boolean m_projectAware;

    /** Flag to determine if the site selector should be shown in popup window. */
    private boolean m_showSiteSelector;

    /** The start site used in the popup window. */
    private String m_startSite;

    /**
     * Creates a new vfs file widget.<p>
     */
    public CmsVfsFileWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new vfs file widget with the parameters to configure the popup tree window behavior.<p>
     * 
     * @param showSiteSelector true if the site selector should be shown in the popup window
     * @param startSite the start site root for the popup window
     */
    public CmsVfsFileWidget(boolean showSiteSelector, String startSite) {

        this(showSiteSelector, startSite, true);
    }

    /**
     * Creates a new vfs file widget with the parameters to configure the popup tree window behavior.<p>
     * 
     * @param showSiteSelector true if the site selector should be shown in the popup window
     * @param startSite the start site root for the popup window
     * @param includeFiles true if files should be shown in the popup window
     */
    public CmsVfsFileWidget(boolean showSiteSelector, String startSite, boolean includeFiles) {

        this(showSiteSelector, startSite, includeFiles, true);
    }

    /**
     * Creates a new vfs file widget with the parameters to configure the popup tree window behavior.<p>
     * 
     * @param showSiteSelector true if the site selector should be shown in the popup window
     * @param startSite the start site root for the popup window
     * @param includeFiles <code>true</code> if files should be shown in the popup window
     * @param projectAware <code>true</code> if resources outside of the current project should be displayed as normal
     */
    public CmsVfsFileWidget(boolean showSiteSelector, String startSite, boolean includeFiles, boolean projectAware) {

        m_showSiteSelector = showSiteSelector;
        m_startSite = startSite;
        m_includeFiles = includeFiles;
        m_projectAware = projectAware;
    }

    /**
     * Creates a new vfs file widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsVfsFileWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        // append site selector flag to configuration
        if (m_showSiteSelector) {
            result.append(CONFIGURATION_SHOWSITESELECTOR);
        } else {
            result.append(CONFIGURATION_HIDESITESELECTOR);
        }

        // append start site to configuration
        if (m_startSite != null) {
            result.append("|");
            result.append(CONFIGURATION_STARTSITE);
            result.append("=");
            result.append(m_startSite);
        }

        // append flag for including files
        result.append("|");
        if (m_includeFiles) {
            result.append(CONFIGURATION_INCLUDEFILES);
        } else {
            result.append(CONFIGURATION_EXCLUDEFILES);
        }

        // append flag for project awareness
        result.append("|");
        if (m_projectAware) {
            result.append(CONFIGURATION_PROJECTAWARE);
        } else {
            result.append(CONFIGURATION_NOTPROJECTAWARE);
        }

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "commons/tree.js"));
        result.append("\n");
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/fileselector.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\tinitVfsFileSelector();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append("function initVfsFileSelector() {\n");
        //initialize tree javascript, does parts of <code>CmsTree.initTree(CmsObject, encoding, skinuri);</code>
        result.append("\tinitResources(\"");
        result.append(OpenCms.getWorkplaceManager().getEncoding());
        result.append("\", \"");
        result.append(CmsWorkplace.VFS_PATH_WORKPLACE);
        result.append("\", \"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("\", \"");
        result.append(OpenCms.getSystemInfo().getOpenCmsContext());
        result.append("\");\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);

        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td style=\"width: 100%;\">");
        result.append("<input style=\"width: 99%;\" class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        result.append(param.getStringValue(cms));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        StringBuffer buttonJs = new StringBuffer(8);
        buttonJs.append("javascript:openTreeWin('EDITOR',  '");
        buttonJs.append(id);
        buttonJs.append("', document, ");
        buttonJs.append(m_showSiteSelector);
        buttonJs.append(", '");
        if (m_startSite != null) {
            buttonJs.append(m_startSite);
        } else {
            buttonJs.append(cms.getRequestContext().getSiteRoot());
        }
        buttonJs.append("', ");
        // include files
        buttonJs.append(m_includeFiles);
        // project awareness
        buttonJs.append(", ");
        buttonJs.append(m_projectAware);
        buttonJs.append(");return false;");

        result.append(widgetDialog.button(
            buttonJs.toString(),
            null,
            "folder",
            org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append("</td>");

        return result.toString();
    }

    /**
     * Returns the start site root shown by the widget when first displayed.<p>
     *
     * If <code>null</code> is returned, the dialog will display the current site of 
     * the current user.<p>
     *
     * @return the start site root shown by the widget when first displayed
     */
    public String getStartSite() {

        return m_startSite;
    }

    /**
     * Returns <code>true</code> if the site selector is shown.<p>
     *
     * The default is <code>true</code>.<p>
     *
     * @return <code>true</code> if the site selector is shown
     */
    public boolean isShowingSiteSelector() {

        return m_showSiteSelector;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsVfsFileWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        m_showSiteSelector = true;
        m_includeFiles = true;
        m_projectAware = true;

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            if (configuration.indexOf(CONFIGURATION_HIDESITESELECTOR) != -1) {
                // site selector should be hidden
                m_showSiteSelector = false;
            }
            int siteIndex = configuration.indexOf(CONFIGURATION_STARTSITE);
            if (siteIndex != -1) {
                // start site is given
                String site = configuration.substring(CONFIGURATION_STARTSITE.length() + 1);
                if (site.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    site = site.substring(0, site.indexOf('|'));
                }
                m_startSite = site;
            }
            if (configuration.indexOf(CONFIGURATION_EXCLUDEFILES) != -1) {
                // files should not be included
                m_includeFiles = false;
            }
            if (configuration.indexOf(CONFIGURATION_NOTPROJECTAWARE) != -1) {
                // resources outside of the current project should not be disabled
                m_projectAware = false;
            }
        }
        super.setConfiguration(configuration);
    }
}
