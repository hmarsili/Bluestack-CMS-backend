/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsLogin.java,v $
 * Date   : $Date: 2011/03/23 16:10:42 $
 * Version: $Revision: 1.48 $
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

package org.opencms.workplace;

import org.opencms.db.CmsLoginMessage;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Handles the login of Users to the OpenCms workplace.<p> 
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.48 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLogin extends CmsJspLoginBean {

    /** Action constant: Default action, display the dialog. */
    public static final int ACTION_DISPLAY = 0;

    /** Action constant: Login successful. */
    public static final int ACTION_LOGIN = 1;

    /** Action constant: Logout. */
    public static final int ACTION_LOGOUT = 2;

    /** The parameter name for the "login" action. */
    public static final String PARAM_ACTION_LOGIN = "login";

    /** The parameter name for the "logout" action. */
    public static final String PARAM_ACTION_LOGOUT = "logout";

    /** The html id for the login form. */
    public static final String PARAM_FORM = "ocLoginForm";

    /** The parameter name for the organizational unit. */
    public static final String PARAM_OUFQN = "ocOuFqn";

    /** The parameter name for the password. */
    public static final String PARAM_PASSWORD = "ocPword";

    /** The parameter name for the organizational unit. */
    public static final String PARAM_PREDEF_OUFQN = "ocPredefOuFqn";

    /** The parameter name for the user name. */
    public static final String PARAM_USERNAME = "ocUname";

    /** The oufqn cookie name. */
    private static final String COOKIE_OUFQN = "OpenCmsOuFqn";

    /** The username cookie name. */
    private static final String COOKIE_USERNAME = "OpenCmsUserName";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLogin.class);

    /** The action to perform. */
    private int m_action;

    /** The value of the "login" action parameter. */
    private String m_actionLogin;

    /** The value of the "logout" action parameter. */
    private String m_actionLogout;

    /** The locale to use for display, this will not be the workplace locale, but the browser locale. */
    private Locale m_locale;

    /** The message to display with the dialog in a JavaScrip alert. */
    private CmsMessageContainer m_message;

    /** The selected organizational unit. */
    private CmsOrganizationalUnit m_ou;

    /** The value of the organizational unit parameter. */
    private String m_oufqn;

    /** The list of all organizational units. */
    private List m_ous;

    /** The value of the password parameter. */
    private String m_password;

    /** The redirect URL after a successful login. */
    private String m_requestedResource;

    /** The value of the user name parameter. */
    private String m_username;

    /**
     * Public constructor for login page.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsLogin(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

        // this page must never be cached
        res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, System.currentTimeMillis());
        CmsRequestUtil.setNoCacheHeaders(res);

        // divine the best locale from the users browser settings
        CmsAcceptLanguageHeaderParser parser = new CmsAcceptLanguageHeaderParser(
            req,
            OpenCms.getWorkplaceManager().getDefaultLocale());
        List acceptedLocales = parser.getAcceptedLocales();
        List workplaceLocales = OpenCms.getWorkplaceManager().getLocales();
        m_locale = OpenCms.getLocaleManager().getFirstMatchingLocale(acceptedLocales, workplaceLocales);
        if (m_locale == null) {
            // no match found - use OpenCms default locale
            m_locale = OpenCms.getWorkplaceManager().getDefaultLocale();
        }
    }

    /**
     * Returns html code for selecting an organizational unit.<p>
     * 
     * @return html code
     */
    public String buildOrgUnitSelector() {

        StringBuffer html = new StringBuffer();
        html.append("<select style='width: 100%;' size='1' ");
        appendId(html, PARAM_OUFQN);
        html.append(">\n");
        Iterator itOus = getOus().iterator();
        while (itOus.hasNext()) {
            CmsOrganizationalUnit ou = (CmsOrganizationalUnit)itOus.next();
            String selected = "";
            if (ou.getName().equals(m_oufqn)
                || (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_oufqn) && ou.getName().equals(m_oufqn.substring(1)))) {
                selected = " selected='selected'";
            }
            html.append("<option value='").append(ou.getName()).append("'").append(selected).append(">");
            html.append(ou.getDisplayName(m_locale));
            html.append("</option>\n");
        }
        html.append("</select>\n");
        return html.toString();
    }

    /**
     * Returns the HTML for the login dialog in it's current state.<p>
     * 
     * @return the HTML for the login dialog
     * 
     * @throws IOException in case a redirect fails
     */
    public String displayDialog() throws IOException {

        if ((OpenCms.getSiteManager().getSites().size() > 1)
            && !OpenCms.getSiteManager().isWorkplaceRequest(getRequest())) {

            // this is a multi site-configuration, but not a request to the configured Workplace site
            StringBuffer loginLink = new StringBuffer();
            loginLink.append(OpenCms.getSiteManager().getWorkplaceSiteMatcher().toString());
            loginLink.append(getFormLink());
            // send a redirect to the workplace site
            getResponse().sendRedirect(loginLink.toString());
            return null;
        }

        CmsObject cms = getCmsObject();

        m_message = null;
        if (cms.getRequestContext().currentUser().isGuestUser()) {
            // user is not currently logged in
            m_action = ACTION_DISPLAY;
            m_username = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_USERNAME);
            if (m_username != null) {
                // remove white spaces, can only lead to confusion on user name
                m_username = m_username.trim();
            }
            m_password = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_PASSWORD);
            m_actionLogin = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_ACTION_LOGIN);
            m_oufqn = getRequest().getParameter(PARAM_OUFQN);
            if (m_oufqn == null) {
                m_oufqn = getPreDefOuFqn();
            }
            // try to get some info from a cookie
            getCookieData();
        } else {
            // user is already logged in
            m_oufqn = cms.getRequestContext().getOuFqn();
            m_action = ACTION_LOGIN;
            m_actionLogout = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_ACTION_LOGOUT);
        }

        // initialize the right ou
        if (m_oufqn == null) {
            m_oufqn = CmsOrganizationalUnit.SEPARATOR;
        }
        m_ou = null;
        try {
            m_ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), m_oufqn);
        } catch (CmsException e) {
            m_oufqn = CmsOrganizationalUnit.SEPARATOR;
            try {
                m_ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), m_oufqn);
            } catch (CmsException exc) {
                LOG.error(exc.getLocalizedMessage(), exc);
            }
        }

        // initialize the requested resource
        m_requestedResource = CmsRequestUtil.getNotEmptyParameter(
            getRequest(),
            CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
        if (m_requestedResource == null) {
            // no resource was requested, use default workplace URI
            m_requestedResource = CmsFrameset.JSP_WORKPLACE_URI;
        } else {
            if (m_actionLogin != null) {
                m_requestedResource = CmsEncoder.decode(m_requestedResource);
            }
        }

        if (Boolean.valueOf(m_actionLogin).booleanValue()) {
            // login was requested
            if ((m_username == null) && (m_password == null)) {
                m_message = Messages.get().container(Messages.GUI_LOGIN_NO_DATA_0);
            } else if (m_username == null) {
                m_message = Messages.get().container(Messages.GUI_LOGIN_NO_NAME_0);
            } else if (m_password == null) {
                m_message = Messages.get().container(Messages.GUI_LOGIN_NO_PASSWORD_0);
            } else if ((m_username != null) && (m_password != null)) {

                // try to login with the given user information
                login((m_oufqn == null ? CmsOrganizationalUnit.SEPARATOR : m_oufqn) + m_username, m_password);

                if (getLoginException() == null) {
                    // the login was successful
                    m_action = ACTION_LOGIN;

                    // set the default project of the user
                    CmsUserSettings settings = new CmsUserSettings(cms);
                    try {
                        CmsProject project = cms.readProject(settings.getStartProject());
                        if (OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, project.getOuFqn(), false).contains(
                            project)) {
                            // user has access to the project, set this as current project
                            cms.getRequestContext().setCurrentProject(project);
                        }
                    } catch (CmsException e) {
                        // unable to set the startup project, bad but not critical
                        LOG.warn(Messages.get().getBundle().key(
                            Messages.LOG_LOGIN_NO_STARTUP_PROJECT_2,
                            m_username,
                            settings.getStartProject()), e);
                    }
                } else {
                    // there was an error during login

                    if (org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_2 == getLoginException().getMessageContainer().getKey()) {
                        // the user account is disabled
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_DISABLED_0);
                    } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_4 == getLoginException().getMessageContainer().getKey()) {
                        // the user account is temporarily disabled because of too many login failures
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_TEMP_DISABLED_0);
                    } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1 == getLoginException().getMessageContainer().getKey()) {
                        // all logins have been disabled be the Administration
                        CmsLoginMessage loginMessage = OpenCms.getLoginManager().getLoginMessage();
                        if (loginMessage != null) {
                            m_message = Messages.get().container(
                                Messages.GUI_LOGIN_FAILED_WITH_MESSAGE_1,
                                loginMessage.getMessage());
                        }
                    }
                    if (m_message == null) {
                        // any other error - display default message
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_0);
                    }
                }
            }
        } else if (Boolean.valueOf(m_actionLogout).booleanValue()) {
            m_action = ACTION_LOGOUT;
            // after logout this will automatically redirect to the login form again
            logout();
            return null;
        }

        if (m_action == ACTION_LOGIN) {
            // clear message
            m_message = null;
            // login is successful, check if the requested resource can be read
            CmsUriSplitter splitter = new CmsUriSplitter(m_requestedResource, true);
            String resource = splitter.getPrefix();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(resource)) {
                // bad resource name, use workplace as default
                resource = CmsFrameset.JSP_WORKPLACE_URI;
            }
            if (!getCmsObject().existsResource(resource, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                // requested resource does either not exist or is not readable by user
                if (CmsFrameset.JSP_WORKPLACE_URI.equals(resource)) {
                    // we know the Workplace exists, so the user does not have access to the Workplace
                    // probably this is a "Guest" user in a default setup where "Guest" has no access to the Workplace
                    m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_NO_WORKPLACE_PERMISSIONS_0);
                    m_action = ACTION_DISPLAY;
                } else if (getCmsObject().existsResource(CmsFrameset.JSP_WORKPLACE_URI)) {
                    // resource does either not exist or is not readable, but general workplace permissions are granted
                    m_message = Messages.get().container(Messages.GUI_LOGIN_UNKNOWN_RESOURCE_1, m_requestedResource);
                    m_requestedResource = CmsFrameset.JSP_WORKPLACE_URI;
                } else {
                    // resource does not exist and no general workplace permissions granted
                    m_message = Messages.get().container(
                        Messages.GUI_LOGIN_FAILED_NO_TARGET_PERMISSIONS_1,
                        m_requestedResource);
                    m_action = ACTION_DISPLAY;
                }
            }
            if (m_action == ACTION_DISPLAY) {
                // the login was invalid
                m_requestedResource = null;
                // destroy the generated session
                HttpSession session = getRequest().getSession(false);
                if (session != null) {
                    session.invalidate();
                }
            } else {
                // successfully logged in, so set the cookie
                setCookieData();
            }
        }

        return displayLoginForm();
    }

    /**
     * Gets the login info from the cookies.<p>
     */
    public void getCookieData() {

        // get the user name cookie
        Cookie userNameCookie = getCookie(COOKIE_USERNAME);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userNameCookie.getValue())) {
            // only set the data is needed
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_username)) {
                m_username = userNameCookie.getValue();
            }
        }
        if ("null".equals(m_username)) {
            m_username = null;
        }
        // get the user name cookie
        Cookie ouFqnCookie = getCookie(COOKIE_OUFQN);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ouFqnCookie.getValue())) {
            // only set the data is needed
            if (m_oufqn == null) {
                m_oufqn = ouFqnCookie.getValue();
            }
        }
        if ("null".equals(m_oufqn)) {
            m_oufqn = null;
        }
    }

    /**
     * @see org.opencms.jsp.CmsJspLoginBean#getFormLink()
     */
    @Override
    public String getFormLink() {

        if (getPreDefOuFqn() == null) {
            return super.getFormLink();
        }
        String preDefOuFqn = (String)getRequest().getAttribute(PARAM_PREDEF_OUFQN);
        try {
            OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), preDefOuFqn);
        } catch (CmsException e) {
            // organizational unit does not exist
            return super.getFormLink();
        }
        return link("/system/login" + CmsEncoder.escapeXml(preDefOuFqn));
    }

    /**
     * Sets the login cookies.<p>
     */
    public void setCookieData() {

        // set the user name cookie
        Cookie userNameCookie = getCookie(COOKIE_USERNAME);
        userNameCookie.setValue(m_username);
        setCookie(userNameCookie);

        // set the user name cookie
        Cookie ouFqnCookie = getCookie(COOKIE_OUFQN);
        ouFqnCookie.setValue(m_oufqn);
        setCookie(ouFqnCookie);
    }

    /**
     * Appends the JavaScript for the login screen
     * to the given HTML buffer.<p>
     * 
     * @param html the html buffer to append the script to
     * @param message the message to display after an unsuccessful login
     */
    protected void appendDefaultLoginScript(StringBuffer html, CmsMessageContainer message) {

        html.append("<script type=\"text/javascript\">\n");
        if (message != null) {
            html.append("function showAlert() {\n");
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(message.key(m_locale)));
            html.append("\");\n");
            html.append("}\n");
        }
        html.append("var orgUnitShow = false;\n");
        html.append("function orgUnitSelection() {\n");
        html.append("\tif (!orgUnitShow) {\n");
        html.append("\t\tdocument.getElementById('ouSelId').style.display = 'block';\n");
        html.append("\t\tdocument.getElementById('ouLabelId').style.display = 'block';\n");
        html.append("\t\tdocument.getElementById('ouBtnId').value = '");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SELECT_OFF_0));
        html.append("';\n");
        html.append("\t} else {\n");
        html.append("\t\tdocument.getElementById('ouSelId').style.display = 'none';\n");
        html.append("\t\tdocument.getElementById('ouLabelId').style.display = 'none';\n");
        html.append("\t\tdocument.getElementById('ouBtnId').value = '");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SELECT_ON_0));
        html.append("';\n");
        html.append("\t}\n");
        html.append("\torgUnitShow = !orgUnitShow;\n");
        html.append("\tdocument.getElementById('titleId').style.display = 'block';\n");
        html.append("\tdocument.getElementById('titleIdOu').style.display = 'none';\n");
        html.append("}\n");

        html.append("function doOnload() {\n");
        html.append("\tdocument.");
        html.append(PARAM_FORM);
        html.append(".");
        html.append(PARAM_USERNAME);
        html.append(".select();\n");
        html.append("\tdocument.");
        html.append(PARAM_FORM);
        html.append(".");
        html.append(PARAM_USERNAME);
        html.append(".focus();\n");
        if (message != null) {
            html.append("\tshowAlert();\n");
        }
        html.append("}\n");

        html.append("</script>\n");
    }

    /**
     * Appends the HTML form name/id code for the given id to the given html.<p>
     * 
     * @param html the html where to append the id to
     * @param id the id to append
     */
    protected void appendId(StringBuffer html, String id) {

        html.append(" name=\"");
        html.append(id);
        html.append("\" id=\"");
        html.append(id);
        html.append("\" ");
    }

    /**
     * Appends the JavaScript that opens the Workplace window after a successful login
     * to the given HTML buffer.<p>
     * 
     * @param html the html buffer to append the script to
     * @param requestedResource the requested resource to open in a new window
     * @param message the message to display if the originally requested resource is not available
     */
    protected void appendWorkplaceOpenerScript(StringBuffer html, String requestedResource, CmsMessageContainer message) {

        String winId = "OpenCms" + System.currentTimeMillis();

        html.append("<script type=\"text/javascript\">\n");

        html.append("function doOnload() {\n");

        // display missing resource warning if required
        if (message != null) {
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(message.key(m_locale)));
            html.append("\");\n");
        }

        // display login message if required
        CmsLoginMessage loginMessage = OpenCms.getLoginManager().getLoginMessage();
        if ((loginMessage != null) && (loginMessage.isActive())) {
            String msg;
            if (loginMessage.isLoginForbidden()) {
                // login forbidden for normal users, current user must be Administrator
                msg = Messages.get().container(
                    Messages.GUI_LOGIN_SUCCESS_WITH_MESSAGE_2,
                    loginMessage.getMessage(),
                    new Date(loginMessage.getTimeEnd())).key(m_locale);
            } else {
                // just display the message
                msg = loginMessage.getMessage();
            }
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(msg));
            html.append("\");\n");
        }

        html.append("\tvar openUri = \"");
        html.append(link(requestedResource));
        html.append("\";\n");
        html.append("\tvar workplaceWin = openWorkplace(openUri, \"");
        html.append(winId);
        html.append("\");\n");
        html.append("\tif (window.name != \"");
        html.append(winId);
        html.append("\") {\n");
        html.append("\t\twindow.opener = workplaceWin;\n");
        html.append("\t\tif (workplaceWin != null) {\n");
        html.append("\t\t\twindow.close();\n");
        html.append("\t\t}\n");
        html.append("\t}\n");
        html.append("}\n");

        html.append("function openWorkplace(url, name) {\n");
        html.append("\tvar isInWin = (window.name.match(/^OpenCms\\d+$/) != null);\n");
        html.append("\tif (window.innerHeight) {\n");
        // Mozilla
        html.append("\t\tvar winHeight = window.innerHeight;\n");
        html.append("\t\tvar winWidth = window.innerWidth;\n");
        html.append("\t} else if (document.documentElement && document.documentElement.clientHeight) {\n");
        // IE 6 "strict" mode
        html.append("\t\tvar winHeight = document.documentElement.clientHeight;\n");
        html.append("\t\tvar winWidth = document.documentElement.clientWidth;\n");
        html.append("\t} else if (document.body && document.body.clientHeight) {\n");
        // IE 5, IE 6 "relaxed" mode
        html.append("\t\tvar winHeight = document.body.clientWidth;\n");
        html.append("\t\tvar winWidth = document.body.clientHeight;\n");
        html.append("\t}\n");
        html.append("\tif (window.screenY) {\n");
        // Mozilla
        html.append("\t\tvar winTop = window.screenY;\n");
        html.append("\t\tvar winLeft = window.screenX;\n");
        html.append("\t\tif (! isInWin) {\n");
        html.append("\t\t\twinTop += 25;\n");
        html.append("\t\t\twinLeft += 25;\n");
        html.append("\t\t}\n");
        html.append("\t} else if (window.screenTop) {\n");
        // IE
        html.append("\t\tvar winTop = window.screenTop;\n");
        html.append("\t\tvar winLeft = window.screenLeft;\n");
        html.append("\t}\n");
        html.append("\n");

        if (requestedResource.startsWith(CmsWorkplace.VFS_PATH_WORKPLACE)) {
            html.append("\tvar openerStr = \"width=\" + winWidth + \",height=\" + winHeight + \",left=\" + winLeft + \",top=\" + winTop + \",scrollbars=no,location=no,toolbar=no,menubar=no,directories=no,status=yes,resizable=yes\";\n");
        } else {
            html.append("\tvar openerStr = \"width=\" + winWidth + \",height=\" + winHeight + \",left=\" + winLeft + \",top=\" + winTop + \",scrollbars=yes,location=yes,toolbar=yes,menubar=yes,directories=no,status=yes,resizable=yes\";\n");
        }
        html.append("\tvar OpenCmsWin = window.open(url, name, openerStr);\n");
        html.append("\n");
        html.append("\ttry{\n");
        html.append("\t\tif (! OpenCmsWin.opener) {\n");
        html.append("\t\t\tOpenCmsWin.opener = self;\n");
        html.append("\t\t}\n");
        html.append("\t\tif (OpenCmsWin.focus) {\n");
        html.append("\t\t\tOpenCmsWin.focus();\n");
        html.append("\t\t}\n");
        html.append("\t} catch (e) {}\n");
        html.append("\n");
        html.append("\treturn OpenCmsWin;\n");
        html.append("}\n");

        html.append("</script>\n");
    }

    /**
     * Returns the HTML for the login form.<p>
     * 
     * @return the HTML for the login form
     */
    protected String displayLoginForm() {

        StringBuffer html = new StringBuffer(4096);

        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
        html.append("<html><head>\n");
        html.append("<title>");

        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_TITLE_0));
        html.append("CmsMedios V7.5");

        html.append("</title>\n");

        String encoding = getRequestContext().getEncoding();
        html.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
        html.append(encoding);
        html.append("\">\n");

        // append workplace css
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append(CmsWorkplace.getStyleUri(this, "workplace.css"));
        html.append("\">\n");

        // append favicon relation
        html.append("<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"");
        html.append(CmsWorkplace.getSkinUri()).append("commons/favicon.ico");
        html.append("\">\n");

        if (m_action == ACTION_DISPLAY) {
            // append default script
            appendDefaultLoginScript(html, m_message);
        } else if (m_action == ACTION_LOGIN) {
            // append window opener script
            appendWorkplaceOpenerScript(html, m_requestedResource, m_message);
        }

        html.append("</head>\n");

        html.append("<body class=\"dialog\" onload=\"doOnload();\">\n");

        html.append("<div style=\"text-align: center; padding-top: 50px;\">");
        html.append("<img src=\"");
        html.append(CmsWorkplace.getResourceUri("commons/login_logo.png"));
        html.append("\" alt=\"OpenCms Logo\">");
        html.append("</div>\n");

        html.append("<table class=\"logindialog\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>\n");
        html.append("<table class=\"dialogbox\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>\n");
        html.append("<div class=\"dialoghead\">");

        if (m_oufqn == null) {
            m_oufqn = CmsOrganizationalUnit.SEPARATOR;
        }
        if (m_action == ACTION_DISPLAY) {
            html.append("<div id='titleId'");
            if (!m_oufqn.equals(CmsOrganizationalUnit.SEPARATOR)) {
                html.append(" style='display: none;'");
            }
            html.append(">\n");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_HEADLINE_0));
            html.append("</div>\n");
            html.append("<div id='titleIdOu'");
            if (m_oufqn.equals(CmsOrganizationalUnit.SEPARATOR)) {
                html.append(" style='display: none;'");
            }
            html.append(">\n");
            html.append(Messages.get().getBundle(m_locale).key(
                Messages.GUI_LOGIN_HEADLINE_SELECTED_ORGUNIT_1,
                m_ou.getDescription(getCmsObject().getRequestContext().getLocale())));
            html.append("</div>\n");
        } else if (m_action == ACTION_LOGIN) {
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_HEADLINE_ALREADY_IN_0));
        }

        html.append("</div>\n");

        if (m_action == ACTION_DISPLAY) {
            // start form
            html.append("<form style=\"margin: 0px; padding: 0px;\" action=\"");
            html.append(getFormLink());
            html.append("\"");
            appendId(html, PARAM_FORM);
            html.append("method=\"POST\">\n");
        }

        html.append("<div class=\"dialogcontent\">\n");
        html.append("<table border=\"0\">\n");

        html.append("<tr>\n");
        html.append("<td></td>\n<td colspan=\"2\" style=\"white-space: nowrap;\">\n");
        html.append("<div style=\"padding-bottom: 10px;\">");

        if (m_action == ACTION_DISPLAY) {
            html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_MESSAGE_0)));
        } else if (m_action == ACTION_LOGIN) {
            html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(
                Messages.GUI_LOGIN_MESSAGE_ALREADY_IN_0)));
        }

        html.append("</div>\n");
        html.append("</td>\n");
        html.append("</tr>\n");

        html.append("<tr>\n");

        html.append("<td style=\"width: 60px; text-align: center; vertical-align: top\" rowspan=\"4\">");
        html.append("<img src=\"");
        html.append(CmsWorkplace.getResourceUri("commons/login.png"));
        html.append("\" height=\"48\" width=\"48\" alt=\"\">");
        html.append("</td>\n");

        html.append("<td style=\"white-space: nowrap;\"><b>");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_USERNAME_0));
        html.append("</b>&nbsp;&nbsp;</td>\n");
        html.append("<td style=\"width: 300px; white-space: nowrap;\">");

        if (m_action == ACTION_DISPLAY) {
            // append input for user name
            html.append("<input style=\"width: 300px;\" type=\"text\"");
            appendId(html, PARAM_USERNAME);
            html.append("value=\"");
            html.append(CmsStringUtil.isEmpty(m_username) ? "" : CmsEncoder.escapeXml(m_username));
            html.append("\">");
        } else if (m_action == ACTION_LOGIN) {
            // append name of user that has been logged in
            html.append(getRequestContext().currentUser().getFullName());
        }

        html.append("</td>\n");
        html.append("</tr>\n");

        if (m_action == ACTION_DISPLAY) {
            // append 2 rows: input for user name and login button
            html.append("<tr>\n");
            html.append("<td style=\"white-space: nowrap;\"><b>");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_PASSWORD_0));
            html.append("</b>&nbsp;&nbsp;</td>\n");
            html.append("<td style=\"width: 300px; white-space: nowrap;\">");
            html.append("<input style=\"width: 300px;\" type=\"password\"");
            appendId(html, PARAM_PASSWORD);
            html.append(">");
            html.append("</td>\n");
            html.append("</tr>\n");

            html.append("<tr>\n");
            html.append("<td style=\"white-space: nowrap;\"><div id='ouLabelId' style='display: none;'><b>");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_0)).append(
                "</b>&nbsp;&nbsp;\n");
            html.append("</div></td>\n");
            html.append("<td style=\"width: 300px; white-space: nowrap;\"><div id='ouSelId' style='display: none;'>");
            html.append(buildOrgUnitSelector());
            html.append("</div></td>\n");
            html.append("</tr>\n");
            html.append("<tr>\n");
            html.append("<td>\n");
            html.append("</td>\n");
            html.append("<td style=\"white-space: nowrap;\">\n");
            html.append("<input type=\"hidden\"");
            appendId(html, PARAM_ACTION_LOGIN);
            html.append("value=\"true\">\n");

            if (m_requestedResource != null) {
                html.append("<input type=\"hidden\"");
                appendId(html, CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
                html.append("value=\"");
                html.append(CmsEncoder.encode(m_requestedResource));
                html.append("\">\n");
            }

            html.append("<input class=\"loginbutton\" type=\"submit\" value=\"");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_BUTTON_0));
            html.append("\">\n");

            if ((getOus().size() > 1)
                && ((getPreDefOuFqn() == null) || getPreDefOuFqn().equals(CmsOrganizationalUnit.SEPARATOR))) {
                // options
                html.append("&nbsp;<input id='ouBtnId' class='loginbutton' type='button' value='");
                html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SELECT_ON_0));
                html.append("' onclick='javascript:orgUnitSelection();'>\n");
            }
            html.append("</td>\n");
            html.append("</tr>\n");
        } else if (m_action == ACTION_LOGIN) {
            // append 2 rows: one empty, other for button with re-open window script
            html.append("<tr><td></td><td></td></tr>\n");

            html.append("<tr>\n");
            html.append("<td></td>\n");
            html.append("<td style=\"width:100%; white-space: nowrap;\">\n");
            html.append("<input class=\"loginbutton\" type=\"button\" value=\"");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_BUTTON_ALREADY_IN_0));
            html.append("\" onclick=\"doOnload()\">\n");
            html.append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        html.append("</div>");

        if (m_action == ACTION_DISPLAY) {
            // end form
            html.append("</form>\n");
        }

        html.append("</td></tr></table>\n");
        html.append("</td></tr></table>\n");


        html.append("<noscript>\n");
        html.append("<div style=\"text-align: center; font-size: 14px; border: 2px solid black; margin: 50px; padding: 20px; background-color: red; color: white; white-space: nowrap;\"><b>");
        html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(
            Messages.GUI_LOGIN_NOSCRIPT_1,
            OpenCms.getSiteManager().getWorkplaceSiteMatcher())));
        html.append("</b></div>\n");
        html.append("</noscript>\n");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Returns the cookie with the given name, if not cookie is found a new one is created.<p>
     * 
     * @param name the name of the cookie
     * 
     * @return the cookie
     */
    protected Cookie getCookie(String name) {

        Cookie[] cookies = getRequest().getCookies();
        for (int i = 0; (cookies != null) && (i < cookies.length); i++) {
            if (name.equalsIgnoreCase(cookies[i].getName())) {
                return cookies[i];
            }
        }
        return new Cookie(name, "");
    }

    /**
     * Returns all organizational units in the system.<p>
     * 
     * @return a list of {@link CmsOrganizationalUnit} objects
     */
    protected List getOus() {

        if (m_ous == null) {
            m_ous = new ArrayList();
            try {
                if (getPreDefOuFqn() == null) {
                    m_ous.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), ""));
                    m_ous.addAll(OpenCms.getOrgUnitManager().getOrganizationalUnits(getCmsObject(), "", true));
                    Iterator itOus = m_ous.iterator();
                    while (itOus.hasNext()) {
                        CmsOrganizationalUnit ou = (CmsOrganizationalUnit)itOus.next();
                        if (ou.hasFlagHideLogin() || ou.hasFlagWebuser()) {
                            itOus.remove();
                        }
                    }
                } else {
                    m_ous.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), m_oufqn));
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_ous;
    }

    /**
     * Returns the predefined organizational unit fqn.<p>
     * 
     * This is normally selected by url, and set by the {@link CmsWorkplaceLoginHandler}.<p>
     * 
     * @return the predefined organizational unit fqn
     */
    protected String getPreDefOuFqn() {

        if (Boolean.valueOf(m_actionLogout).booleanValue() && (getRequest().getAttribute(PARAM_PREDEF_OUFQN) == null)) {
            String oufqn = getCmsObject().getRequestContext().getOuFqn();
            if (!oufqn.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
                oufqn = CmsOrganizationalUnit.SEPARATOR + oufqn;
            }
            getRequest().setAttribute(CmsLogin.PARAM_PREDEF_OUFQN, oufqn);
        }
        return (String)getRequest().getAttribute(PARAM_PREDEF_OUFQN);
    }

    /**
     * Sets the cookie in the response.<p>
     * 
     * @param cookie the cookie to set
     */
    protected void setCookie(Cookie cookie) {

        if (getRequest().getAttribute(PARAM_PREDEF_OUFQN) != null) {
            // prevent the use of cookies if using a direct ou login url
            return;
        }
        // set the expiration date of the cookie to six months from today
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, 6);
        int maxAge = (int)((cal.getTimeInMillis() - System.currentTimeMillis()) / 1000);
        cookie.setMaxAge(maxAge);
        // set the path
        cookie.setPath(link("/system/login"));
        // set the cookie
        getResponse().addCookie(cookie);
    }
}