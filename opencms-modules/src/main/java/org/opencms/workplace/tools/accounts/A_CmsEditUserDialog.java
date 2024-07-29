/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsEditUserDialog.java,v $
 * Date   : $Date: 2011/03/23 14:51:03 $
 * Version: $Revision: 1.17 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordInfo;
import org.opencms.security.CmsRole;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsPasswordWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Dialog to edit new or existing user in the administration view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.17 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsEditUserDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "user";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the user id. */
    public static final String PARAM_USERID = "userid";

    /** Session parameter name for the pwd info object. */
    private static final String PWD_OBJECT = "PWD_INFO";

    /** Session parameter name for the user object. */
    private static final String USER_OBJECT = "USER";

    /** The user object that is edited on this dialog. */
    protected CmsUser m_user;

    /** The starting group for the new user. */
    private String m_group;

    /** The default language for the new user. */
    private String m_language;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /** The password information object. */
    private CmsPasswordInfo m_pwdInfo;

    /** The starting site for the new user. */
    private String m_site;

    private Boolean m_forcePasswordChange;
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public A_CmsEditUserDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Commits the edited user to the db.<p>
     */
    @Override
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // if new create it first
            if (isNewUser()) {
                m_pwdInfo.validate();
                CmsUser newUser = createUser(
                    m_paramOufqn + m_user.getSimpleName(),
                    m_pwdInfo.getNewPwd(),
                    m_user.getDescription(),
                    m_user.getAdditionalInfo());
                newUser.setFirstname(m_user.getFirstname());
                newUser.setLastname(m_user.getLastname());
                newUser.setEmail(m_user.getEmail());
                newUser.setAddress(m_user.getAddress());
                newUser.setManaged(m_user.isManaged());
                newUser.setEnabled(m_user.isEnabled());
                if (isForceChangePassword()) {
                	newUser.setLastPasswordChanged(Long.MIN_VALUE);
                }
                else {
                	newUser.setLastPasswordChanged(System.currentTimeMillis());
                }
                m_user = newUser;
            } else {
            	if (isForceChangePassword()) {
            		m_user.setLastPasswordChanged(Long.MIN_VALUE);
                }
            	if (CmsStringUtil.isNotEmpty(m_pwdInfo.getNewPwd())) {
            		
            	
	                m_pwdInfo.validate();
	                
	                if (!isForceChangePassword()) {
                		m_user.setLastPasswordChanged(System.currentTimeMillis());
                    }
	                getCms().setPassword(m_user.getName(), m_pwdInfo.getNewPwd());
	            }
            }
            // test the group name
            if (isNewUser() && CmsStringUtil.isNotEmptyOrWhitespaceOnly(getGroup())) {
                getCms().readGroup(getGroup());
            }
            // write the edited user
            writeUser(m_user);
            // set starting membership
            if (isNewUser()) {
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getGroup())) {
                    getCms().addUserToGroup(m_user.getName(), getGroup());
                }
            }
            // set starting settings
            CmsUserSettings settings = new CmsUserSettings(m_user);
            settings.setLocale(CmsLocaleManager.getLocale(getLanguage()));
            settings.setStartSite(getSite());
            // set starting project
            if (isNewUser()) {
                try {
                    String prj = getCms().readProject(
                        getParamOufqn() + OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject()).getName();
                    settings.setStartProject(prj);
                } catch (CmsException e) {
                    // use root ou project, if project not found
                    settings.setStartProject(OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject());
                }
            }
            settings.save(getCms());

            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(getListClass());
            }
        } catch (Throwable t) {
            errors.add(t);
        }

        if (errors.isEmpty() && isNewUser()) {
            if ((getParamCloseLink() != null) && (getParamCloseLink().indexOf("path=" + getListRootPath()) > -1)) {
                // set closelink
                Map argMap = new HashMap();
                argMap.put(PARAM_USERID, m_user.getId());
                argMap.put("oufqn", m_paramOufqn);
                setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), getListRootPath() + "/edit", argMap));
            }
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the description of the parent ou.<p>
     * 
     * @return the description of the parent ou
     */
    public String getAssignedOu() {

        try {
            CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn());
            return ou.getDisplayName(getLocale());
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * Returns the localized description of the user if the description is a key.<p>
     * 
     * @return the localized description of the user if the description is a key
     */
    public String getDescription() {

        return m_user.getDescription(getLocale());
    }

    /**
     * Returns the group.<p>
     *
     * @return the group
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * Returns the language.<p>
     *
     * @return the language
     */
    public String getLanguage() {

        return m_language;
    }

    /**
     * Returns the simple name of the user object.<p>
     * 
     * @return the simple name of the user object
     */
    public String getName() {

        if (m_user.getSimpleName().endsWith(CmsOrganizationalUnit.SEPARATOR)) {
            return "";
        }
        return m_user.getSimpleName();
    }

    /**
     * Returns the organizational unit fqn parameter value.<p>
     * 
     * @return the organizational unit fqn parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Returns the user id parameter value.<p>
     * 
     * @return the user id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
    }

    /**
     * Returns the password information.<p>
     *
     * @return the password information
     */
    public CmsPasswordInfo getPwdInfo() {

        return m_pwdInfo;
    }

    /**
     * Returns the site.<p>
     *
     * @return the site
     */
    public String getSite() {

        return m_site;
    }

    /**
     * Returns the selfManagement.<p>
     *
     * @return the selfManagement
     */
    public boolean isSelfManagement() {

        return !m_user.isManaged();
    }
    
    public boolean isForceChangePassword() {
    	
    	if (m_forcePasswordChange==null)
    		m_forcePasswordChange = ( m_user.getLastPasswordChanged()==Long.MIN_VALUE);
    	return m_forcePasswordChange;
    }

    /**
     * Return if user is enabled.<p>
     * 
     * @return enabled status
     */
    public boolean isEnabled() {

        return m_user.isEnabled();
    }

    /**
     * This method is only needed for displaying reasons.<p>
     * 
     * @param assignedOu nothing to do with this parameter
     */
    public void setAssignedOu(String assignedOu) {

        // nothing will be done here, just to avoid warnings
        assignedOu.length();
    }

    /**
     * Sets the description for the user.<p>
     * 
     * @param description the description for the user
     */
    public void setDescription(String description) {

        m_user.setDescription(description);
    }

    /**
     * Sets the group.<p>
     *
     * @param group the group to set
     */
    public void setGroup(String group) {

        m_group = group;
    }

    /**
     * Sets the language.<p>
     *
     * @param language the language to set
     */
    public void setLanguage(String language) {

        m_language = language;
    }

    /**
     * Sets the name of the user object.<p>
     * 
     * @param name the name of the user object
     */
    public void setName(String name) {

        m_user.setName(getParamOufqn() + name);
    }

    /**
     * Sets the organizational unit fqn parameter value.<p>
     * 
     * @param ouFqn the organizational unit fqn parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * Sets the user id parameter value.<p>
     * 
     * @param userId the user id parameter value
     */
    public void setParamUserid(String userId) {

        m_paramUserid = userId;
    }

    /**
     * Sets the selfManagement.<p>
     *
     * @param selfManagement the selfManagement to set
     */
    public void setSelfManagement(boolean selfManagement) {

        m_user.setManaged(!selfManagement);
    }

    public void setForceChangePassword(boolean forceChangePassword) {
    	m_forcePasswordChange=forceChangePassword;
    }
    
    /**
     * Sets if user is enabled.<p>
     * 
     * @param enabled is the user enabled
     */
    public void setEnabled(boolean enabled) {

        m_user.setEnabled(enabled);
    }

    /**
     * Sets the site.<p>
     *
     * @param site the site to set
     */
    public void setSite(String site) {

        m_site = site;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            boolean webuserOu = false;
            try {
                webuserOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).hasFlagWebuser();
            } catch (CmsException e) {
                // ignore
            }
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 5));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_ADDRESS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(6, 9));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            int row = isNewUser() ? 12 : 11;
            if (!webuserOu) {
                if (getSites().isEmpty()) {
                    row -= 1;
                }
                result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_SETTINGS_BLOCK_0)));
                result.append(createWidgetTableStart());
                result.append(createDialogRowsHtml(10, row));
                result.append(createWidgetTableEnd());
                result.append(dialogBlockEnd());
            } else {
                row = 9;
            }
            row++;
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_AUTHENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            if (isPwdChangeAllowed(m_user)) {
                result.append(createDialogRowsHtml(row, row + 4));
            } else {
                result.append(createDialogRowsHtml(row, row + 1));
            }
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates a new user.<p>
     * 
     * @param name the name
     * @param pwd the password
     * @param desc the description
     * @param info the additional information map
     * 
     * @return the new user
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract CmsUser createUser(String name, String pwd, String desc, Map info) throws CmsException;

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initUserObject();
        boolean webuserOu = false;
        try {
            webuserOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).hasFlagWebuser();
        } catch (CmsException e) {
            webuserOu = m_user.isWebuser();
        }
        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        if (isNewUser() && isEditable(m_user)) {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
        }
        if (isEditable(m_user)) {
            addWidget(new CmsWidgetDialogParameter(this, "description", "", PAGES[0], new CmsTextareaWidget(), 0, 1));
            addWidget(new CmsWidgetDialogParameter(m_user, "lastname", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "firstname", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "email", PAGES[0], new CmsInputWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "address", "", PAGES[0], new CmsInputWidget(), 0, 1));
            addWidget(new CmsWidgetDialogParameter(m_user, "zipcode", "", PAGES[0], new CmsInputWidget(), 0, 1));
            addWidget(new CmsWidgetDialogParameter(m_user, "city", "", PAGES[0], new CmsInputWidget(), 0, 1));
            addWidget(new CmsWidgetDialogParameter(m_user, "country", "", PAGES[0], new CmsInputWidget(), 0, 1));
            if (!webuserOu) {
                addWidget(new CmsWidgetDialogParameter(this, "language", PAGES[0], new CmsSelectWidget(getLanguages())));
                if (!getSites().isEmpty()) {
                    addWidget(new CmsWidgetDialogParameter(this, "site", PAGES[0], new CmsSelectWidget(getSites())));
                }
                if (isNewUser()) {
                    addWidget(new CmsWidgetDialogParameter(this, "group", PAGES[0], new CmsGroupWidget(
                        null,
                        null,
                        getParamOufqn())));
                }
            }
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "lastname", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "firstname", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "email", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "address", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "zipcode", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "city", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "country", PAGES[0], new CmsDisplayWidget()));
            if (!webuserOu) {
                addWidget(new CmsWidgetDialogParameter(this, "language", PAGES[0], new CmsDisplayWidget()));
                if (!getSites().isEmpty()) {
                    addWidget(new CmsWidgetDialogParameter(this, "site", PAGES[0], new CmsDisplayWidget()));
                }
            }
        }
        addWidget(new CmsWidgetDialogParameter(m_user, "enabled", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "selfManagement", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "forceChangePassword", PAGES[0], new CmsCheckboxWidget()));
        if (isPwdChangeAllowed(m_user)) {
            addWidget(new CmsWidgetDialogParameter(m_pwdInfo, "newPwd", PAGES[0], new CmsPasswordWidget()));
            addWidget(new CmsWidgetDialogParameter(m_pwdInfo, "confirmation", PAGES[0], new CmsPasswordWidget()));
        }
    }

    /**
     * Returns the dialog class name of the list to refresh.<p> 
     * 
     * @return the list dialog class name
     */
    protected abstract String getListClass();

    /**
     * Returns the root path for the list tool.<p>
     * 
     * @return the root path
     */
    protected abstract String getListRootPath();

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the user object to work with depending on the dialog state and request parameters.<p>
     * 
     * Two initializations of the user object on first dialog call are possible:
     * <ul>
     * <li>edit an existing user</li>
     * <li>create a new user</li>
     * </ul>
     */
    protected void initUserObject() {

        Object o = null;
        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // edit an existing user, get the user object from db
                m_user = getCms().readUser(new CmsUUID(getParamUserid()));
                m_pwdInfo = new CmsPasswordInfo();
                CmsUserSettings settings = new CmsUserSettings(m_user);
                m_language = settings.getLocale().toString();
                m_site = settings.getStartSite();
                return;
            } else {
                // this is not the initial call, get the user object from session            
                o = getDialogObject();
                Map dialogObject = (Map)o;
                m_user = (CmsUser)dialogObject.get(USER_OBJECT);
                m_pwdInfo = (CmsPasswordInfo)dialogObject.get(PWD_OBJECT);
                CmsUserSettings settings = new CmsUserSettings(m_user);
                m_language = settings.getLocale().toString();
                m_site = settings.getStartSite();
                // test
                m_user.getId();
                return;
            }
        } catch (Exception e) {
            // noop
        }
        // create a new user object
        m_user = new CmsUser();
        m_pwdInfo = new CmsPasswordInfo();
        m_group = "";
        try {
            m_group = getCms().readGroup(getParamOufqn() + OpenCms.getDefaultUsers().getGroupUsers()).getName();
        } catch (CmsException e) {
            // ignore
        }
        m_language = CmsLocaleManager.getDefaultLocale().toString();
        m_site = OpenCms.getSiteManager().getDefaultSite().getSiteRoot();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the user and pwd (may be changed because of the widget values)
        Map dialogObject = new HashMap();
        dialogObject.put(USER_OBJECT, m_user);
        dialogObject.put(PWD_OBJECT, m_pwdInfo);
        setDialogObject(dialogObject);
    }

    /**
     * Tests if the given user is editable or not.<p>
     * 
     * Not editable means that the user can only be activated and deactivated.<p>
     * 
     * @param user the user to test 
     * 
     * @return the editable flag
     */
    protected abstract boolean isEditable(CmsUser user);

    /**
     * Checks if the new user dialog has to be displayed.<p>
     * 
     * @return <code>true</code> if the new user dialog has to be displayed
     */
    protected boolean isNewUser() {

        return getCurrentToolPath().equals(getListRootPath() + "/new");
    }

    /**
     * Indicates if the pwd can be edited or not.<p>
     * 
     * @param user the edited cms user
     * 
     * @return <code>true</code> if the pwd can be edited
     */
    protected boolean isPwdChangeAllowed(CmsUser user) {

        return user.isUser(); // always true, just to avoid warning 
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        if (!isNewUser()) {
            // test the needed parameters
            getCms().readUser(new CmsUUID(getParamUserid())).getName();
        }
    }

    /**
     * Writes a user to the db.<p>
     * 
     * @param user the user to write
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract void writeUser(CmsUser user) throws CmsException;

    /**
     * Returns a list of options for the locale selector.<p>
     * 
     * @return a list of options for the locale selector
     */
    private List getLanguages() {

        List locales = new ArrayList();

        Locale defLocale = null;
        if ((m_user != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_user.getName())) {
            defLocale = new CmsUserSettings(m_user).getLocale();
        }
        if (defLocale == null) {
            defLocale = getCms().getRequestContext().getLocale();
        }

        Iterator itLocales = OpenCms.getLocaleManager().getAvailableLocales().iterator();
        while (itLocales.hasNext()) {
            Locale locale = (Locale)itLocales.next();
            boolean selected = locale.equals(defLocale);
            locales.add(new CmsSelectWidgetOption(locale.toString(), selected, locale.getDisplayName(getLocale()), null));
        }
        return locales;
    }

    /**
     * Returns a list of options for the site selector.<p>
     * 
     * @return a list of options for the site selector
     */
    private List getSites() {

        List sites = new ArrayList();
        List sitesList = OpenCms.getSiteManager().getAvailableSites(getCms(), true, getParamOufqn());

        String defSite = null;
        if ((m_user != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_user.getName())) {
            defSite = new CmsUserSettings(m_user).getStartSite();
        }
        if (defSite == null) {
            defSite = getCms().getRequestContext().getSiteRoot();
        }
        if (!defSite.endsWith("/")) {
            defSite += "/";
        }

        Iterator itSites = sitesList.iterator();
        while (itSites.hasNext()) {
            CmsSite site = (CmsSite)itSites.next();
            String siteRoot = site.getSiteRoot();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(siteRoot)) {
                if (sitesList.size() > 1) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_user.getName())) {
                        if (!OpenCms.getRoleManager().hasRole(getCms(), m_user.getName(), CmsRole.DEVELOPER)) {
                            // skip the root site if not accessible
                            continue;
                        }
                    }
                }
            }
            if (!siteRoot.endsWith("/")) {
                siteRoot += "/";
            }
            boolean selected = defSite.equals(siteRoot);
            sites.add(new CmsSelectWidgetOption(siteRoot, selected, site.getTitle(), null));
        }
        return sites;
    }
}