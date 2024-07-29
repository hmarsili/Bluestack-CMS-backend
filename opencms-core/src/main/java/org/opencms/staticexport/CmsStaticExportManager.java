/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportManager.java,v $
 * Date   : $Date: 2011/03/23 14:52:51 $
 * Version: $Revision: 1.138 $
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

package org.opencms.staticexport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

/**
 * Provides the functionality to export resources from the OpenCms VFS
 * to the file system.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.138 $ 
 * 
 * @since 6.0.0 
 */
public class CmsStaticExportManager implements I_CmsEventListener {

    /**
     * Implements the file filter used to guess the right suffix of a deleted jsp file.<p>
     */
    private static class CmsPrefixFileFilter implements FileFilter {

        /** The base file. */
        private String m_baseName;

        /**
         * Creates a new instance of this filter.<p>
         * 
         * @param fileName the base file to compare with.
         */
        public CmsPrefixFileFilter(String fileName) {

            m_baseName = fileName + ".";
        }

        /**
         * Accepts the given file if its name starts with the name of of the base file (without extension) 
         * and ends with the extension.<p>
         * 
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {

            return f.getName().startsWith(m_baseName)
                && (f.getName().length() > m_baseName.length())
                && (f.getName().indexOf('.', m_baseName.length()) < 0);
        }
    }

    /** Marker for error message attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_MESSAGE = "javax.servlet.error.message";

    /** Marker for error request uri attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

    /** Marker for error servlet name attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";

    /** Marker for error status code attribute. */
    public static final String EXPORT_ATTRIBUTE_ERROR_STATUS_CODE = "javax.servlet.error.status_code";

    /** Name for the backup folder default name. */
    public static final String EXPORT_BACKUP_FOLDER_NAME = "backup";

    /** Name for the default work path. */
    public static final Integer EXPORT_DEFAULT_BACKUPS = new Integer(0);

    /** Name for the folder default index file. */
    public static final String EXPORT_DEFAULT_FILE = "index_export.html";

    /** Name for the default work path. */
    public static final String EXPORT_DEFAULT_WORKPATH = CmsSystemInfo.FOLDER_WEBINF + "temp";

    /** Flag value for links without parameters. */
    public static final int EXPORT_LINK_WITH_PARAMETER = 2;

    /** Flag value for links without parameters. */
    public static final int EXPORT_LINK_WITHOUT_PARAMETER = 1;

    /** Marker for externally redirected 404 uri's. */
    public static final String EXPORT_MARKER = "exporturi";

    /** Time given (in seconds) to the static export handler to finish a publish task. */
    public static final int HANDLER_FINISH_TIME = 60;

    /** Cache value to indicate a true 404 error. */
    private static final String CACHEVALUE_404 = "?404";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStaticExportManager.class);

    /** HTTP header Accept-Charset. */
    private String m_acceptCharsetHeader;

    /** HTTP header Accept-Language. */
    private String m_acceptLanguageHeader;

    /** Cache for the export links. */
    private Map m_cacheExportLinks;

    /** Cache for the export uris. */
    private Map m_cacheExportUris;

    /** Cache for the online links. */
    private Map m_cacheOnlineLinks;

    /** Cache for the secure links. */
    private Map m_cacheSecureLinks;

    /** OpenCms default charset header. */
    private String m_defaultAcceptCharsetHeader;

    /** OpenCms default locale header. */
    private String m_defaultAcceptLanguageHeader;

    /** Matcher for  selecting those resources which should be part of the static export. */
    private CmsExportFolderMatcher m_exportFolderMatcher;

    /** List of export resources which should be part of the static export. */
    private List m_exportFolders;

    /** The additional http headers for the static export. */
    private List m_exportHeaders;

    /** List of all resources that have the "exportname" property set. */
    private Map m_exportnameResources;

    /** Indicates if <code>true</code> is the default value for the property "export". */
    private boolean m_exportPropertyDefault;

    /** Indicates if links in the static export should be relative. */
    private boolean m_exportRelativeLinks;

    /** List of export rules. */
    private List m_exportRules;

    /** List of export suffixes where the "export" property default is always <code>true</code>. */
    private List m_exportSuffixes;

    /** Temporary variable for reading the xml config file. */
    private CmsStaticExportExportRule m_exportTmpRule;

    /** Export url to send internal requests to. */
    private String m_exportUrl;

    /** Export url with unsubstituted context values. */
    private String m_exportUrlConfigured;

    /** Export url to send internal requests to without http://servername. */
    private String m_exportUrlPrefix;

    /** Boolean value if the export is a full static export. */
    private boolean m_fullStaticExport;

    /** Handler class for static export. */
    private I_CmsStaticExportHandler m_handler;

    /** The configured link substitution handler. */
    private I_CmsLinkSubstitutionHandler m_linkSubstitutionHandler;

    /** Lock object for write access to the {@link #cmsEvent(CmsEvent)} method. */
    private Object m_lockCmsEvent;

    /** Lock object for export folder deletion in {@link #scrubExportFolders(I_CmsReport)}. */
    private Object m_lockScrubExportFolders;

    /** Lock object for write access to the {@link #m_exportnameResources} map in {@link #setExportnames()}. */
    private Object m_lockSetExportnames;

    /** Indicates if the quick static export for plain resources is enabled. */
    private boolean m_quickPlainExport;

    /** Remote address. */
    private String m_remoteAddr;

    /** Prefix to use for exported files. */
    private String m_rfsPrefix;

    /** Prefix to use for exported files with unsubstituted context values. */
    private String m_rfsPrefixConfigured;

    /** List of configured rfs rules. */
    private List m_rfsRules;

    /** Temporary variable for reading the xml config file. */
    private CmsStaticExportRfsRule m_rfsTmpRule;

    /** The number of backups stored for the export folder. */
    private Integer m_staticExportBackups;

    /** Indicates if the static export is enabled or disabled. */
    private boolean m_staticExportEnabled;

    /** The path to where the static export will be written. */
    private String m_staticExportPath;

    /** The path to where the static export will be written without the complete rfs path. */
    private String m_staticExportPathConfigured;

    /** The path to where the static export will be written during the static export process. */
    private String m_staticExportWorkPath;

    /** The path to where the static export will be written during the static export process without the complete rfs path. */
    private String m_staticExportWorkPathConfigured;

    /** Vfs Name of a resource used to do a "static export required" test. */
    private String m_testResource;

    /** If there are several identical export paths the usage of temporary directories has to be disabled. */
    private boolean m_useTempDirs = true;

    /** Prefix to use for internal OpenCms files. */
    private String m_vfsPrefix;

    /** Prefix to use for internal OpenCms files with unsubstituted context values. */
    private String m_vfsPrefixConfigured;

    /**
     * Creates a new static export property object.<p>
     * 
     */
    public CmsStaticExportManager() {

        m_lockCmsEvent = new Object();
        m_lockScrubExportFolders = new Object();
        m_lockSetExportnames = new Object();
        m_exportSuffixes = new ArrayList();
        m_exportFolders = new ArrayList();
        m_exportHeaders = new ArrayList();
        m_rfsRules = new ArrayList();
        m_exportRules = new ArrayList();
        m_exportTmpRule = new CmsStaticExportExportRule("", "");
        m_rfsTmpRule = new CmsStaticExportRfsRule("", "", "", "", "", "", null, null);
        m_fullStaticExport = false;
    }

    /**
     * Adds a new export rule to the configuration.<p>
     * 
     * @param name the name of the rule
     * @param description the description for the rule
     */
    public void addExportRule(String name, String description) {

        m_exportRules.add(new CmsStaticExportExportRule(
            name,
            description,
            m_exportTmpRule.getModifiedResources(),
            m_exportTmpRule.getExportResourcePatterns()));
        m_exportTmpRule = new CmsStaticExportExportRule("", "");
    }

    /**
     * Adds a regex to the latest export rule.<p>
     * 
     * @param regex the regex to add
     */
    public void addExportRuleRegex(String regex) {

        m_exportTmpRule.addModifiedResource(regex);
    }

    /**
     * Adds a export uri to the latest export rule.<p>
     * 
     * @param exportUri the export uri to add
     */
    public void addExportRuleUri(String exportUri) {

        m_exportTmpRule.addExportResourcePattern(exportUri);
    }

    /**
     * Adds a new rfs rule to the configuration.<p>
     * 
     * @param name the name of the rule
     * @param description the description for the rule
     * @param source the source regex
     * @param rfsPrefix the url prefix
     * @param exportPath the rfs export path
     * @param exportWorkPath the rfs export work path
     * @param exportBackups the number of backups
     * @param useRelativeLinks the relative links value
     */
    public void addRfsRule(
        String name,
        String description,
        String source,
        String rfsPrefix,
        String exportPath,
        String exportWorkPath,
        String exportBackups,
        String useRelativeLinks) {

        if ((m_staticExportPathConfigured != null) && exportPath.equals(m_staticExportPathConfigured)) {
            m_useTempDirs = false;
        }
        Iterator itRules = m_rfsRules.iterator();
        while (m_useTempDirs && itRules.hasNext()) {
            CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)itRules.next();
            if (exportPath.equals(rule.getExportPathConfigured())) {
                m_useTempDirs = false;
            }
        }
        Boolean relativeLinks = (useRelativeLinks == null ? null : Boolean.valueOf(useRelativeLinks));
        Integer backups = (exportBackups == null ? null : Integer.valueOf(exportBackups));

        m_rfsRules.add(new CmsStaticExportRfsRule(
            name,
            description,
            source,
            rfsPrefix,
            exportPath,
            exportWorkPath,
            backups,
            relativeLinks,
            m_rfsTmpRule.getRelatedSystemResources()));
        m_rfsTmpRule = new CmsStaticExportRfsRule("", "", "", "", "", "", null, null);
    }

    /**
     * Adds a regex of related system resources to the latest rfs-rule.<p>
     * 
     * @param regex the regex to add
     */
    public void addRfsRuleSystemRes(String regex) {

        m_rfsTmpRule.addRelatedSystemRes(regex);
    }

    /**
     * Caches a calculated export uri.<p>
     * 
     * @param rfsName the name of the resource in the "real" file system
     * @param vfsName the name of the resource in the VFS
     * @param parameters the optional parameters for the generation of the resource
     */
    public void cacheExportUri(String rfsName, String vfsName, String parameters) {

        m_cacheExportUris.put(rfsName, new CmsStaticExportData(vfsName, parameters));
    }

    /**
     * Caches a calculated online link.<p>
     * 
     * @param linkName the link
     * @param vfsName the name of the VFS resource 
     */
    public void cacheOnlineLink(Object linkName, Object vfsName) {

        m_cacheOnlineLinks.put(linkName, vfsName);
    }

    /**
     * Implements the CmsEvent interface,
     * the static export properties uses the events to clear 
     * the list of cached keys in case a project is published.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(CmsEvent event) {

        if (!isStaticExportEnabled()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_STATIC_EXPORT_DISABLED_0));
            }
            return;
        }
        I_CmsReport report = null;
        Map data = event.getData();
        if (data != null) {
            report = (I_CmsReport)data.get(I_CmsEventListener.KEY_REPORT);
        }
        if (report == null) {
            report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), getClass());
        }
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_UPDATE_EXPORTS:
                scrubExportFolders(report);
                clearCaches(event);
                break;
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                if (data == null) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(Messages.ERR_EMPTY_EVENT_DATA_0));
                    }
                    return;
                }
                // event data contains a list of the published resources
                CmsUUID publishHistoryId = new CmsUUID((String)data.get(I_CmsEventListener.KEY_PUBLISHID));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_EVENT_PUBLISH_PROJECT_1, publishHistoryId));
                }
                synchronized (m_lockCmsEvent) {
                    getHandler().performEventPublishProject(publishHistoryId, report);
                }
                clearCaches(event);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_EVENT_PUBLISH_PROJECT_FINISHED_1,
                        publishHistoryId));
                }

                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                clearCaches(event);
                break;
            default:
                // no operation
        }
    }

    /**
     * Exports the requested uri and at the same time writes the uri to the response output stream
     * if required.<p>
     * 
     * @param req the current request
     * @param res the current response
     * @param cms an initialized cms context (should be initialized with the "Guest" user only)
     * @param data the static export data set
     * 
     * @return status code of the export operation, status codes are the same as http status codes (200,303,304)
     * 
     * @throws CmsException in case of errors accessing the VFS
     * @throws ServletException in case of errors accessing the servlet 
     * @throws IOException in case of errors writing to the export output stream
     * @throws CmsStaticExportException if static export is disabled
     */
    public int export(HttpServletRequest req, HttpServletResponse res, CmsObject cms, CmsStaticExportData data)
    throws CmsException, IOException, ServletException, CmsStaticExportException {

        String vfsName = data.getVfsName();
        String rfsName = data.getRfsName();
        CmsResource resource = data.getResource();

        // cut the site root from the vfsName and switch to the correct site
        String siteRoot = OpenCms.getSiteManager().getSiteRoot(vfsName);

        CmsI18nInfo i18nInfo = OpenCms.getLocaleManager().getI18nInfo(
            req,
            cms.getRequestContext().currentUser(),
            cms.getRequestContext().currentProject(),
            vfsName);

        String remoteAddr = m_remoteAddr;
        if (remoteAddr == null) {
            remoteAddr = CmsContextInfo.LOCALHOST;
        }
        CmsContextInfo contextInfo = new CmsContextInfo(
            cms.getRequestContext().currentUser(),
            cms.getRequestContext().currentProject(),
            vfsName,
            "/",
            i18nInfo.getLocale(),
            i18nInfo.getEncoding(),
            remoteAddr,
            CmsContextInfo.CURRENT_TIME,
            cms.getRequestContext().getOuFqn());

        cms = OpenCms.initCmsObject(null, contextInfo);

        if (siteRoot != null) {
            vfsName = vfsName.substring(siteRoot.length());
        } else {
            siteRoot = "/";
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_STATIC_EXPORT_SITE_ROOT_2, siteRoot, vfsName));
        }
        cms.getRequestContext().setSiteRoot(siteRoot);

        String oldUri = null;

        // this flag signals if the export method is used for "on demand" or "after publish". 
        // if no request and result stream are available, it was called during "export on publish"
        boolean exportOnDemand = ((req != null) && (res != null));

        CmsStaticExportResponseWrapper wrapRes = null;
        if (res != null) {
            wrapRes = new CmsStaticExportResponseWrapper(res);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SE_RESOURCE_START_1, data));
        }

        CmsFile file;
        // read vfs resource
        if (resource.isFile()) {
            file = cms.readFile(vfsName);
        } else {
            file = cms.readFile(OpenCms.initResource(cms, vfsName, req, wrapRes));
            if (cms.existsResource(vfsName + file.getName())) {
                vfsName = vfsName + file.getName();
            }
            rfsName += EXPORT_DEFAULT_FILE;
        }

        // check loader id for resource
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(file);
        if ((loader == null) || (!loader.isStaticExportEnabled())) {
            Object[] arguments = new Object[] {vfsName, new Integer(file.getTypeId())};
            throw new CmsStaticExportException(Messages.get().container(Messages.ERR_EXPORT_NOT_SUPPORTED_2, arguments));
        }

        // ensure we have exactly the same setup as if called "the usual way"
        // we only have to do this in case of the static export on demand
        if (exportOnDemand) {
            String mimetype = OpenCms.getResourceManager().getMimeType(
                file.getName(),
                cms.getRequestContext().getEncoding());
            if (wrapRes != null) {
                wrapRes.setContentType(mimetype);
            }
            oldUri = cms.getRequestContext().getUri();
            cms.getRequestContext().setUri(vfsName);
        }

        // do the export
        int status = -1;
        // only export those resource where the export property is set
        if (isExportLink(cms, vfsName)) {
            CmsLocaleManager locManager = OpenCms.getLocaleManager();
            List locales = locManager.getDefaultLocales(cms, vfsName);
            boolean exported = false;
            boolean matched = false;
            // iterate over all rules
            Iterator it = getRfsRules().iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                // normal case
                boolean export = rule.getSource().matcher(siteRoot + vfsName).matches();
                matched |= export;
                // system folder case
                export |= (vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM) && rule.match(vfsName));
                if (export) {
                    // the resource has to exported for this rule
                    CmsObject locCms = cms;
                    Locale locale = CmsLocaleManager.getLocale(rule.getName());
                    if (locales.contains(locale)) {
                        // if the locale is in the default locales for the resource
                        // so adjust the locale to use for exporting
                        CmsContextInfo ctxInfo = new CmsContextInfo(cms.getRequestContext());
                        ctxInfo.setLocale(locale);
                        locCms = OpenCms.initCmsObject(cms, ctxInfo);
                    }
                    // read the content in the matching locale
                    byte[] content = loader.export(locCms, file, req, wrapRes);
                    if (content != null) {
                        // write to rfs
                        exported = true;
                        String locRfsName = rfsName;
                        if (locales.contains(locale)) {
                            locRfsName = rule.getLocalizedRfsName(rfsName, "/");
                        }
                        writeResource(req, rule.getExportPath(), locRfsName, resource, content);
                    }
                }
            }
            if (!matched) {
                // no rule matched
                String exportPath = getExportPath(siteRoot + vfsName);
                byte[] content = loader.export(cms, file, req, wrapRes);
                if (content != null) {
                    exported = true;
                    writeResource(req, exportPath, rfsName, resource, content);
                }
            }
            if (exported) {
                // get the wrapper status that was set
                status = (wrapRes != null) ? wrapRes.getStatus() : -1;
                if (status < 0) {
                    // the status was not set, assume everything is o.k.
                    status = HttpServletResponse.SC_OK;
                }
            } else {
                // the resource was not written because it was not modified. 
                // set the status to not modified
                status = HttpServletResponse.SC_NOT_MODIFIED;
            }
        } else {
            // the resource was not used for export, so return HttpServletResponse.SC_SEE_OTHER
            // as a signal for not exported resource
            status = HttpServletResponse.SC_SEE_OTHER;
        }

        // restore context
        // we only have to do this in case of the static export on demand
        if (exportOnDemand) {
            cms.getRequestContext().setUri(oldUri);
        }

        return status;
    }

    /**
     * Starts a complete static export of all resources.<p>
     * 
     * @param purgeFirst flag to delete all resources in the export folder of the rfs
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file   
     * 
     * @throws CmsException in case of errors accessing the VFS
     * @throws IOException in case of errors writing to the export output stream
     * @throws ServletException in case of errors accessing the servlet 
     */
    public synchronized void exportFullStaticRender(boolean purgeFirst, I_CmsReport report)
    throws CmsException, IOException, ServletException {

        // set member to true to get temporary export paths for rules
        m_fullStaticExport = true;
        // save the real export path
        String staticExportPathStore = m_staticExportPath;

        if (m_useTempDirs) {
            // set the export path to the export work path
            m_staticExportPath = m_staticExportWorkPath;
        }

        // delete all old exports if the purgeFirst flag is set
        if (purgeFirst) {
            Map eventData = new HashMap();
            eventData.put(I_CmsEventListener.KEY_REPORT, report);
            CmsEvent clearCacheEvent = new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, eventData);
            OpenCms.fireCmsEvent(clearCacheEvent);

            scrubExportFolders(report);
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            cms.deleteAllStaticExportPublishedResources(EXPORT_LINK_WITHOUT_PARAMETER);
            cms.deleteAllStaticExportPublishedResources(EXPORT_LINK_WITH_PARAMETER);
        }

        // do the export
        CmsAfterPublishStaticExportHandler handler = new CmsAfterPublishStaticExportHandler();
        // export everything
        handler.doExportAfterPublish(null, report);

        // set export path to the original one
        m_staticExportPath = staticExportPathStore;

        // set member to false for further exports
        m_fullStaticExport = false;

        // check if report contents no errors
        if (m_useTempDirs && !report.hasError()) {
            // backup old export folders for default export 
            File staticExport = new File(m_staticExportPath);
            createExportBackupFolders(staticExport, m_staticExportPath, getExportBackups().intValue(), null);

            // change the name of the used temporary export folder to the original default export path
            File staticExportWork = new File(m_staticExportWorkPath);
            staticExportWork.renameTo(new File(m_staticExportPath));

            // backup old export folders of rule based exports
            Iterator it = m_rfsRules.iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                File staticExportRule = new File(rule.getExportPath());
                File staticExportWorkRule = new File(rule.getExportWorkPath());
                // only backup if a temporary folder exists for this rule
                if (staticExportWorkRule.exists()) {
                    createExportBackupFolders(
                        staticExportRule,
                        rule.getExportPath(),
                        rule.getExportBackups().intValue(),
                        OpenCms.getResourceManager().getFileTranslator().translateResource(rule.getName()));
                    staticExportWorkRule.renameTo(new File(rule.getExportPath()));
                }
            }
        } else if (report.hasError()) {
            report.println(Messages.get().container(Messages.ERR_EXPORT_NOT_SUCCESSFUL_0), I_CmsReport.FORMAT_WARNING);
        }
    }

    /**
     * Returns the accept-charset header used for internal requests.<p>
     * 
     * @return the accept-charset header
     */
    public String getAcceptCharsetHeader() {

        return m_acceptCharsetHeader;
    }

    /**
     * Returns the accept-language header used for internal requests.<p>
     * 
     * @return the accept-language header
     */
    public String getAcceptLanguageHeader() {

        return m_acceptLanguageHeader;
    }

    /**
     * Returns a cached export data for the given rfs name.<p>
     * 
     * Please note that this export data contains only the vfs name and the parameters,
     * not the resource itself. It can therefore not directly be used as a return 
     * value for the static export.<p>
     * 
     * @param rfsName the name of the resource in the real file system to get the cached vfs resource name for
     * 
     * @return a cached vfs resource name for the given rfs name, or <code>null</code> 
     */
    public CmsStaticExportData getCachedExportUri(String rfsName) {

        return (CmsStaticExportData)m_cacheExportUris.get(rfsName);
    }

    /**
     * Returns a cached link for the given vfs name.<p>
     * 
     * @param vfsName the name of the vfs resource to get the cached link for
     * 
     * @return a cached link for the given vfs name, or null 
     */
    public String getCachedOnlineLink(String vfsName) {

        return (String)m_cacheOnlineLinks.get(vfsName);
    }

    /**
     * Returns the key for the online, export and secure cache.<p>
     * 
     * @param siteRoot the site root of the resource
     * @param uri the URI of the resource
     * 
     * @return a key for the cache 
     */
    public String getCacheKey(String siteRoot, String uri) {

        return new StringBuffer(siteRoot).append(uri).toString();
    }

    /**
     * Gets the default property value as a string representation.<p>
     * 
     * @return <code>"true"</code> or <code>"false"</code>
     */
    public String getDefault() {

        return String.valueOf(m_exportPropertyDefault);
    }

    /**
     * Returns the current default charset header.<p>
     * 
     * @return the current default charset header
     */
    public String getDefaultAcceptCharsetHeader() {

        return m_defaultAcceptCharsetHeader;
    }

    /**
     * Returns the current default locale header.<p>
     * 
     * @return the current default locale header
     */
    public String getDefaultAcceptLanguageHeader() {

        return m_defaultAcceptLanguageHeader;
    }

    /**
     * Returns the default prefix for exported links in the "real" file system.<p>
     * 
     * @return the default prefix for exported links in the "real" file system
     */
    public String getDefaultRfsPrefix() {

        return m_rfsPrefix;
    }

    /**
     * Returns the number of stored backups.<p>
     * 
     * @return the number of stored backups
     */
    public Integer getExportBackups() {

        if (m_staticExportBackups != null) {
            return m_staticExportBackups;
        }
        // if backups not configured set to default value
        return EXPORT_DEFAULT_BACKUPS;
    }

    /**
     * Returns the export data for the request, if null is returned no export is required.<p>
     * 
     * @param request the request to check for export data
     * @param cms an initialized cms context (should be initialized with the "Guest" user only
     * 
     * @return the export data for the request, if null is returned no export is required
     */
    public CmsStaticExportData getExportData(HttpServletRequest request, CmsObject cms) {

        if (!isStaticExportEnabled()) {
            // export is disabled
            return null;
        }

        // build the rfs name for the export "on demand"
        String rfsName = request.getParameter(EXPORT_MARKER);
        if ((rfsName == null)) {
            rfsName = (String)request.getAttribute(EXPORT_ATTRIBUTE_ERROR_REQUEST_URI);
        }

        if (request.getHeader(CmsRequestUtil.HEADER_OPENCMS_EXPORT) != null) {
            // this is a request created by the static export and directly send to 404 handler
            // so remove the leading handler identification
            int prefix = rfsName.startsWith(getExportUrlPrefix()) ? getExportUrlPrefix().length() : 0;
            if (prefix > 0) {
                rfsName = rfsName.substring(prefix);
            } else {
                return null;
            }
        }

        if (!isValidRfsName(rfsName)) {
            // this is not an export request, no further processing is required
            return null;
        }

        return getExportData(rfsName, null, cms);
    }

    /**
     * Returns the export data for a requested resource, if null is returned no export is required.<p>
     * 
     * @param vfsName the VFS name of the resource requested
     * @param cms an initialized cms context (should be initialized with the "Guest" user only
     * 
     * @return the export data for the request, if null is returned no export is required
     */
    public CmsStaticExportData getExportData(String vfsName, CmsObject cms) {

        return getExportData(getRfsName(cms, vfsName), vfsName, cms);
    }

    /**
     * Gets the export enabled value as a string representation.<p>
     * 
     * @return <code>"true"</code> or <code>"false"</code>
     */
    public String getExportEnabled() {

        return String.valueOf(m_staticExportEnabled);
    }

    /**
     * Returns the current folder matcher.<p>
     * 
     * @return the current folder matcher
     */
    public CmsExportFolderMatcher getExportFolderMatcher() {

        return m_exportFolderMatcher;
    }

    /**
     * Returns list of resources patterns which are part of the export.<p>
     * 
     * @return the of resources patterns which are part of the export.
     */
    public List getExportFolderPatterns() {

        return Collections.unmodifiableList(m_exportFolders);
    }

    /**
     * Returns specific http headers for the static export.<p>
     * 
     * If the header <code>Cache-Control</code> is set, OpenCms will not use its default headers.<p>
     * 
     * @return the list of http export headers
     */
    public List getExportHeaders() {

        return Collections.unmodifiableList(m_exportHeaders);
    }

    /**
     * Returns the list of all resources that have the "exportname" property set.<p>
     * 
     * @return the list of all resources that have the "exportname" property set
     */
    public Map getExportnames() {

        return m_exportnameResources;
    }

    /**
     * Returns the export path for the static export, that is the folder where the 
     * static exported resources will be written to.<p>
     * 
     * The returned value will be a directory like prefix. The value is configured
     * in the <code>opencms-importexport.xml</code> configuration file. An optimization
     * of the configured value will be performed, where all relative path information is resolved
     * (for example <code>/export/../static</code> will be resolved to <code>/export</code>. 
     * Moreover, if the configured path ends with a <code>/</code>, this will be cut off 
     * (for example <code>/export/</code> becomes <code>/export</code>.<p>
     * 
     * This is resource name based, and based on the rfs-rules defined in the 
     * <code>opencms-importexport.xml</code> configuration file.<p>
     * 
     * @param vfsName the name of the resource to export
     * 
     * @return the export path for the static export, that is the folder where the
     * 
     * @see #getRfsPrefix(String)
     * @see #getVfsPrefix()
     */
    public String getExportPath(String vfsName) {

        if (vfsName != null) {
            Iterator it = m_rfsRules.iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                if (rule.getSource().matcher(vfsName).matches()) {
                    return rule.getExportPath();
                }
            }
        }
        if (m_useTempDirs && isFullStaticExport()) {
            return getExportWorkPath();
        }
        return m_staticExportPath;
    }

    /**
     * Returns the original configured export path for the static export without the complete rfs path, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getExportPath(String)}</code>
     * to obtain the export path to use when exporting.<p> 
     * 
     * @return the original configured export path for the static export without the complete rfs path
     */
    public String getExportPathForConfiguration() {

        return m_staticExportPathConfigured;
    }

    /**
     * Returns true if the default value for the resource property "export" is true.<p>
     * 
     * @return true if the default value for the resource property "export" is true
     */
    public boolean getExportPropertyDefault() {

        return m_exportPropertyDefault;
    }

    /**
     * Returns the export Rules.<p>
     *
     * @return the export Rules
     */
    public List getExportRules() {

        return Collections.unmodifiableList(m_exportRules);
    }

    /**
     * Gets the list of resource suffixes which will be exported by default.<p>
     * 
     * @return list of resource suffixes
     */
    public List getExportSuffixes() {

        return m_exportSuffixes;
    }

    /**
     * Returns the export URL used for internal requests for exporting resources that require a 
     * request / response (like JSP).<p>
     * 
     * @return the export URL used for internal requests for exporting resources like JSP
     */
    public String getExportUrl() {

        return m_exportUrl;
    }

    /**
     * Returns the export URL used for internal requests with unsubstituted context values, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getExportUrl()}</code>
     * to obtain the export path to use when exporting.<p> 
     * 
     * @return the export URL used for internal requests with unsubstituted context values
     */
    public String getExportUrlForConfiguration() {

        return m_exportUrlConfigured;
    }

    /**
     * Returns the export URL used for internal requests for exporting resources that require a 
     * request / response (like JSP) without http://servername.<p>
     * 
     * @return the export URL used for internal requests for exporting resources like JSP without http://servername
     */
    public String getExportUrlPrefix() {

        return m_exportUrlPrefix;
    }

    /**
     * Returns the export work path for the static export, that is the folder where the 
     * static exported resources will be written to during the export process.<p>
     * 
     * @return the export work path for the static export
     */
    public String getExportWorkPath() {

        return m_staticExportWorkPath;
    }

    /**
     * Returns the original configured export work path for the static export without the complete rfs path, to be used 
     * when re-writing the configuration.<p>
     * 
     * @return the original configured export work path for the static export without the complete rfs path
     */
    public String getExportWorkPathForConfiguration() {

        if (m_staticExportWorkPathConfigured != null) {
            return m_staticExportWorkPathConfigured;
        }
        // if work path not configured set to default value
        return EXPORT_DEFAULT_WORKPATH;
    }

    /**
     * Returns the configured static export handler class.<p>
     * 
     * If not set, a new <code>{@link CmsAfterPublishStaticExportHandler}</code> is created and returned.<p>
     * 
     * @return the configured static export handler class
     */
    public I_CmsStaticExportHandler getHandler() {

        if (m_handler == null) {
            setHandler(CmsOnDemandStaticExportHandler.class.getName());
        }
        return m_handler;
    }

    /**
     * Returns the configured link substitution handler class.<p>
     * 
     * If not set, a new <code>{@link CmsDefaultLinkSubstitutionHandler}</code> is created and returned.<p>
     * 
     * @return the configured link substitution handler class
     */
    public I_CmsLinkSubstitutionHandler getLinkSubstitutionHandler() {

        if (m_linkSubstitutionHandler == null) {
            setLinkSubstitutionHandler(CmsDefaultLinkSubstitutionHandler.class.getName());
        }
        return m_linkSubstitutionHandler;
    }

    /**
     * Gets the plain export optimization value as a string representation.<p>
     * 
     * @return <code>"true"</code> or <code>"false"</code>
     */
    public String getPlainExportOptimization() {

        return String.valueOf(m_quickPlainExport);
    }

    /**
     * Returns true if the quick plain export is enabled.<p>
     * 
     * @return true if the quick plain export is enabled
     */
    public boolean getQuickPlainExport() {

        return m_quickPlainExport;
    }

    /**
     * Gets the relative links value as a string representation.<p>
     * 
     * @return <code>"true"</code> or <code>"false"</code>
     */
    public String getRelativeLinks() {

        return String.valueOf(m_exportRelativeLinks);
    }

    /**
     * Returns the remote address used for internal requests.<p>
     * 
     * @return the remote address
     */
    public String getRemoteAddr() {

        return m_remoteAddr;
    }

    /**
     * Returns the remote address.<p>
     * 
     * @return the remote address
     */
    public String getRemoteAddress() {

        return m_remoteAddr;
    }

    /**
     * Returns the static export rfs name for a given vfs resource.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * 
     * @return the static export rfs name for a give vfs resource
     * 
     * @see #getVfsName(CmsObject, String)
     * @see #getRfsName(CmsObject, String, String)
     */
    public String getRfsName(CmsObject cms, String vfsName) {

        return getRfsName(cms, vfsName, null);
    }

    /**
     * Returns the static export rfs name for a given vfs resource where the link to the 
     * resource includes request parameters.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * @param parameters the parameters of the link pointing to the resource
     * 
     * @return the static export rfs name for a give vfs resource
     */
    public String getRfsName(CmsObject cms, String vfsName, String parameters) {

        String rfsName = vfsName;
        try {
            // check if the resource folder (or a parent folder) has the "exportname" property set
            CmsProperty exportNameProperty = cms.readPropertyObject(
                CmsResource.getFolderPath(rfsName),
                CmsPropertyDefinition.PROPERTY_EXPORTNAME,
                true);

            if (exportNameProperty.isNullProperty()) {
                // if "exportname" is not set we must add the site root 
                rfsName = cms.getRequestContext().addSiteRoot(rfsName);
            } else {
                // "exportname" property is set
                String exportname = exportNameProperty.getValue();
                if (exportname.charAt(0) != '/') {
                    exportname = '/' + exportname;
                }
                if (exportname.charAt(exportname.length() - 1) != '/') {
                    exportname = exportname + '/';
                }
                String value = null;
                boolean cont;
                String resourceName = rfsName;
                do {
                    // find out where the export name was set, to replace these parent folders in the RFS name
                    try {
                        CmsProperty prop = cms.readPropertyObject(
                            resourceName,
                            CmsPropertyDefinition.PROPERTY_EXPORTNAME,
                            false);
                        if (prop.isIdentical(exportNameProperty)) {
                            // look for the right position in path 
                            value = prop.getValue();
                        }
                        cont = (value == null) && (resourceName.length() > 1);
                    } catch (CmsVfsResourceNotFoundException e) {
                        // this is for publishing deleted resources 
                        cont = (resourceName.length() > 1);
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                    if (cont) {
                        resourceName = CmsResource.getParentFolder(resourceName);
                    }
                } while (cont);
                rfsName = exportname + rfsName.substring(resourceName.length());
            }

            String extension = CmsFileUtil.getExtension(rfsName);
            // check if the VFS resource is a JSP page with a ".jsp" ending 
            // in this case the rfs name suffix must be build with special care,
            // usually it must be set to ".html"             
            boolean isJsp = extension.equals(".jsp");
            if (isJsp) {
                String suffix = null;
                try {
                    CmsResource res = cms.readResource(vfsName);
                    isJsp = (res.getTypeId() == CmsResourceTypeJsp.getStaticTypeId());
                    // if the resource is a plain resource then no change in suffix is required
                    if (isJsp) {
                        suffix = cms.readPropertyObject(vfsName, CmsPropertyDefinition.PROPERTY_EXPORTSUFFIX, true).getValue(
                            ".html");
                    }
                } catch (CmsVfsResourceNotFoundException e) {
                    // resource has been deleted, so we are not able to get the right extension from the properties
                    // try to figure out the right extension from file system
                    File rfsFile = new File(CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(
                        vfsName)
                        + rfsName));
                    File parent = rfsFile.getParentFile();
                    if (parent != null) {
                        File[] paramVariants = parent.listFiles(new CmsPrefixFileFilter(rfsFile.getName()));
                        if ((paramVariants != null) && (paramVariants.length > 0)) {
                            // take the first
                            suffix = paramVariants[0].getAbsolutePath().substring(rfsFile.getAbsolutePath().length());
                        }
                    } else {
                        // if no luck, try the default extension
                        suffix = ".html";
                    }
                }
                if ((suffix != null) && !extension.equals(suffix.toLowerCase())) {
                    rfsName += suffix;
                    extension = suffix;
                }
            }
            if (parameters != null) {
                // build the RFS name for the link with parameters
                rfsName = CmsFileUtil.getRfsPath(rfsName, extension, parameters);
                // we have found a rfs name for a vfs resource with parameters, save it to the database
                try {
                    cms.writeStaticExportPublishedResource(
                        rfsName,
                        EXPORT_LINK_WITH_PARAMETER,
                        parameters,
                        System.currentTimeMillis());
                } catch (CmsException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_WRITE_FAILED_1, rfsName), e);
                }
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            // ignore exception, return vfsName as rfsName
            rfsName = vfsName;
        }

        // add export rfs prefix and return result
        if (!vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
            return getRfsPrefix(cms.getRequestContext().addSiteRoot(vfsName)).concat(rfsName);
        } else {
            // check if we are generating a link to a related resource in the same rfs rule
            String source = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
            Iterator it = m_rfsRules.iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                if (rule.getSource().matcher(source).matches() && rule.match(vfsName)) {
                    return rule.getRfsPrefix().concat(rfsName);
                }
            }
            // this is a link across rfs rules 
            return getRfsPrefix(cms.getRequestContext().getSiteRoot() + "/").concat(rfsName);
        }
    }

    /**
     * Returns the prefix for exported links in the "real" file system.<p>
     * 
     * The returned value will be a directory like prefix. The value is configured
     * in the <code>opencms-importexport.xml</code> configuration file. An optimization
     * of the configured value will be performed, where all relative path information is resolved
     * (for example <code>/export/../static</code> will be resolved to <code>/export</code>. 
     * Moreover, if the configured path ends with a <code>/</code>, this will be cut off 
     * (for example <code>/export/</code> becomes <code>/export</code>.<p>
     * 
     * This is resource name based, and based on the rfs-rules defined in the 
     * <code>opencms-importexport.xml</code> configuration file.<p>
     * 
     * @param vfsName the name of the resource to export
     * 
     * @return the prefix for exported links in the "real" file system
     * 
     * @see #getExportPath(String)
     * @see #getVfsPrefix()
     */
    public String getRfsPrefix(String vfsName) {

        if (vfsName != null) {
            Iterator it = m_rfsRules.iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                if (rule.getSource().matcher(vfsName).matches()) {
                    return rule.getRfsPrefix();
                }
            }
        }
        return m_rfsPrefix;
    }

    /**
     * Returns the original configured prefix for exported links in the "real" file, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getRfsPrefix(String)}</code>
     * to obtain the rfs prefix to use for the exported links.<p> 
     * 
     * @return the original configured prefix for exported links in the "real" file
     */
    public String getRfsPrefixForConfiguration() {

        return m_rfsPrefixConfigured;
    }

    /**
     * Returns the rfs Rules.<p>
     *
     * @return the rfs Rules
     */
    public List getRfsRules() {

        return Collections.unmodifiableList(m_rfsRules);
    }

    /**
     * Returns the vfs name of the test resource.<p>
     * 
     * @return the vfs name of the test resource.
     */
    public String getTestResource() {

        return m_testResource;
    }

    /**
     * Returns the VFS name for the given RFS name, being the exact reverse of <code>{@link #getRfsName(CmsObject, String)}</code>.<p>
     * 
     * Returns <code>null</code> if no matching VFS resource can be found for the given RFS name.<p>
     * 
     * @param cms the current users OpenCms context
     * @param rfsName the RFS name to get the VFS name for
     * 
     * @return the VFS name for the given RFS name, or <code>null</code> if the RFS name does not match to the VFS
     * 
     * @see #getRfsName(CmsObject, String)
     */
    public String getVfsName(CmsObject cms, String rfsName) {

        CmsStaticExportData data = getExportData(rfsName, null, cms);
        if (data != null) {
            String result = data.getVfsName();
            if ((result != null) && result.startsWith(cms.getRequestContext().getSiteRoot())) {
                result = result.substring(cms.getRequestContext().getSiteRoot().length());
            }
            return result;
        }
        return null;
    }

    /**
     * Returns the prefix for the internal in the VFS.<p>
     * 
     * The returned value will be a directory like prefix. The value is configured
     * in the <code>opencms-importexport.xml</code> configuration file. An optimization
     * of the configured value will be performed, where all relative path information is resolved
     * (for example <code>/opencms/../mycms</code> will be resolved to <code>/mycms</code>. 
     * Moreover, if the configured path ends with a <code>/</code>, this will be cut off 
     * (for example <code>/opencms/</code> becomes <code>/opencms</code>.<p>
     * 
     * @return the prefix for the internal in the VFS
     * 
     * @see #getExportPath(String)
     * @see #getRfsPrefix(String)
     */
    public String getVfsPrefix() {

        return m_vfsPrefix;
    }

    /**
     * Returns the original configured prefix for internal links in the VFS, to be used 
     * when re-writing the configuration.<p>
     * 
     * This is required <b>only</b> to serialize the configuration again exactly as it was configured.
     * This method should <b>not</b> be used otherwise. Use <code>{@link #getVfsPrefix()}</code>
     * to obtain the VFS prefix to use for the internal links.<p> 
     * 
     * @return the original configured prefix for internal links in the VFS
     */
    public String getVfsPrefixForConfiguration() {

        return m_vfsPrefixConfigured;
    }

    /**
     * Initializes the static export manager with the OpenCms system configuration.<p>
     * 
     * @param cms an OpenCms context object
     */
    public void initialize(CmsObject cms) {

        // initialize static export RFS path (relative to web application)
        m_staticExportPath = normalizeExportPath(m_staticExportPathConfigured);
        m_staticExportWorkPath = normalizeExportPath(getExportWorkPathForConfiguration());
        if (m_staticExportPath.equals(OpenCms.getSystemInfo().getWebApplicationRfsPath())) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_EXPORT_PATH_0));
        }
        // initialize prefix variables
        m_rfsPrefix = normalizeRfsPrefix(m_rfsPrefixConfigured);
        Iterator itRfsRules = m_rfsRules.iterator();
        while (itRfsRules.hasNext()) {
            CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)itRfsRules.next();
            try {
                rule.setExportPath(normalizeExportPath(rule.getExportPathConfigured()));
            } catch (CmsIllegalArgumentException e) {
                CmsLog.INIT.warn(e.getMessageContainer());
                rule.setExportPath(m_staticExportPath);
            }
            try {
                rule.setExportWorkPath(normalizeExportPath(rule.getExportWorkPathConfigured()));
            } catch (CmsIllegalArgumentException e) {
                CmsLog.INIT.warn(e.getMessageContainer());
                rule.setExportWorkPath(m_staticExportWorkPath);
            }
            rule.setRfsPrefix(normalizeRfsPrefix(rule.getRfsPrefixConfigured()));
        }
        m_vfsPrefix = insertContextStrings(m_vfsPrefixConfigured);
        m_vfsPrefix = CmsFileUtil.normalizePath(m_vfsPrefix, '/');
        if (CmsResource.isFolder(m_vfsPrefix)) {
            // ensure prefix does NOT end with a folder '/'
            m_vfsPrefix = m_vfsPrefix.substring(0, m_vfsPrefix.length() - 1);
        }
        if (CmsLog.INIT.isDebugEnabled()) {
            if (cms != null) {
                CmsLog.INIT.debug(Messages.get().getBundle().key(Messages.INIT_SE_MANAGER_CREATED_1, cms));
            } else {
                CmsLog.INIT.debug(Messages.get().getBundle().key(Messages.INIT_SE_MANAGER_CREATED_0));
            }
        }

        LRUMap lruMap1 = new LRUMap(2048);
        m_cacheOnlineLinks = Collections.synchronizedMap(lruMap1);
        // map must be of type "LRUMap" so that memory monitor can acecss all information
        OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheOnlineLinks", lruMap1);

        LRUMap lruMap2 = new LRUMap(2048);
        m_cacheExportUris = Collections.synchronizedMap(lruMap2);
        // map must be of type "LRUMap" so that memory monitor can acecss all information
        OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheExportUris", lruMap2);

        LRUMap lruMap3 = new LRUMap(2048);
        m_cacheSecureLinks = Collections.synchronizedMap(lruMap3);
        // map must be of type "LRUMap" so that memory monitor can acecss all information
        OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheSecureLinks", lruMap3);

        LRUMap lruMap4 = new LRUMap(2048);
        m_cacheExportLinks = Collections.synchronizedMap(lruMap4);
        // map must be of type "LRUMap" so that memory monitor can acecss all information
        OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_cacheExportLinks", lruMap4);

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_PUBLISH_PROJECT,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_UPDATE_EXPORTS});

        m_exportFolderMatcher = new CmsExportFolderMatcher(m_exportFolders, m_testResource);

        // initialize "exportname" folders
        setExportnames();

        // get the default accept-language header value
        m_defaultAcceptLanguageHeader = CmsAcceptLanguageHeaderParser.createLanguageHeader();

        // get the default accept-charset header value
        m_defaultAcceptCharsetHeader = OpenCms.getSystemInfo().getDefaultEncoding();

        // get the export url prefix
        int pos = m_exportUrl.indexOf("://");
        if (pos > 0) {
            // absolute link, remove http://servername
            int pos2 = m_exportUrl.indexOf('/', pos + 3);
            if (pos2 > 0) {
                m_exportUrlPrefix = m_exportUrl.substring(pos2);
            } else {
                // should never happen
                m_exportUrlPrefix = "";
            }
        } else {
            m_exportUrlPrefix = m_exportUrl;
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            if (isStaticExportEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STATIC_EXPORT_ENABLED_0));
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EXPORT_DEFAULT_1,
                    Boolean.valueOf(getExportPropertyDefault())));
                itRfsRules = m_rfsRules.iterator();
                while (itRfsRules.hasNext()) {
                    CmsStaticExportRfsRule rfsRule = (CmsStaticExportRfsRule)itRfsRules.next();
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_EXPORT_RFS_RULE_EXPORT_PATH_2,
                        rfsRule.getSource(),
                        rfsRule.getExportPath()));
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_EXPORT_RFS_RULE_RFS_PREFIX_2,
                        rfsRule.getSource(),
                        rfsRule.getRfsPrefix()));
                    if (rfsRule.getUseRelativeLinks() != null) {
                        if (rfsRule.getUseRelativeLinks().booleanValue()) {
                            CmsLog.INIT.info(Messages.get().getBundle().key(
                                Messages.INIT_EXPORT_RFS_RULE_RELATIVE_LINKS_1,
                                rfsRule.getSource()));
                        } else {
                            CmsLog.INIT.info(Messages.get().getBundle().key(
                                Messages.INIT_EXPORT_RFS_RULE_ABSOLUTE_LINKS_1,
                                rfsRule.getSource()));
                        }
                    }
                }
                // default rule
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EXPORT_RFS_RULE_EXPORT_PATH_2,
                    "/",
                    m_staticExportPath));
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EXPORT_RFS_RULE_RFS_PREFIX_2,
                    "/",
                    m_rfsPrefix));
                if (m_exportRelativeLinks) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_EXPORT_RFS_RULE_RELATIVE_LINKS_1, "/"));
                } else {
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_EXPORT_RFS_RULE_ABSOLUTE_LINKS_1, "/"));
                }
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_EXPORT_VFS_PREFIX_1, getVfsPrefix()));
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EXPORT_EXPORT_HANDLER_1,
                    getHandler().getClass().getName()));
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_EXPORT_URL_1, getExportUrl()));
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EXPORT_OPTIMIZATION_1,
                    getPlainExportOptimization()));
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_EXPORT_TESTRESOURCE_1, getTestResource()));
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_LINKSUBSTITUTION_HANDLER_1,
                    getLinkSubstitutionHandler().getClass().getName()));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STATIC_EXPORT_DISABLED_0));
            }
        }
    }

    /**
     * Checks if the static export is required for the given VFS resource.<p>
     * 
     * Please note that the given OpenCms user context is NOT used to read the resource.
     * The check for export is always done with the permissions of the "Export" user.
     * The provided user context is just used to get the current site root.<p>
     * 
     * Since the "Export" user always operates in the "Online" project, the resource
     * is also read from the "Online" project, not from the current project of the given 
     * OpenCms context.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * 
     * @return <code>true</code> if static export is required for the given VFS resource
     */
    public boolean isExportLink(CmsObject cms, String vfsName) {

        boolean result = false;
        if (isStaticExportEnabled()) {
            Boolean exportResource = (Boolean)m_cacheExportLinks.get(getCacheKey(
                cms.getRequestContext().getSiteRoot(),
                vfsName));
            if (exportResource == null) {
                try {
                    // static export must always be checked with the export users permissions,
                    // not the current users permissions
                    CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
                    exportCms.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
                    // let's look up export property in VFS
                    String exportValue = exportCms.readPropertyObject(
                        vfsName,
                        CmsPropertyDefinition.PROPERTY_EXPORT,
                        true).getValue();
                    if (exportValue == null) {
                        // no setting found for "export" property
                        if (getExportPropertyDefault()) {
                            // if the default is "true" we always export
                            result = true;
                        } else {
                            // check if the resource is exportable by suffix
                            result = isSuffixExportable(vfsName);
                        }
                    } else {
                        // "export" value found, if it was "true" we export
                        result = Boolean.valueOf(exportValue).booleanValue();
                    }
                } catch (Exception e) {
                    // no export required (probably security issues, e.g. no access for export user)
                }
                m_cacheExportLinks.put(
                    getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName),
                    Boolean.valueOf(result));
            } else {
                result = exportResource.booleanValue();
            }
        }
        return result;
    }

    /**
     * Returns true if the export process is a full static export.<p>
     *
     * @return true if the export process is a full static export
     */
    public boolean isFullStaticExport() {

        return m_fullStaticExport;
    }

    /**
     * Returns <code>true</code> if the given VFS resource should be transported through a secure channel.<p>
     * 
     * The secure mode is only checked in the "Online" project. 
     * If the given OpenCms context is currently not in the "Online" project,
     * <code>false</code> is returned.<p>
     * 
     * The given resource is read from the site root of the provided OpenCms context.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * 
     * @return <code>true</code> if the given VFS resource should be transported through a secure channel
     * 
     * @see #isSecureLink(CmsObject, String, String)
     */
    public boolean isSecureLink(CmsObject cms, String vfsName) {

        if (!cms.getRequestContext().currentProject().isOnlineProject()) {
            return false;
        }
        Boolean secureResource = (Boolean)m_cacheSecureLinks.get(getCacheKey(
            cms.getRequestContext().getSiteRoot(),
            vfsName));
        if (secureResource == null) {
            try {
                String secureProp = cms.readPropertyObject(vfsName, CmsPropertyDefinition.PROPERTY_SECURE, true).getValue();
                secureResource = Boolean.valueOf(secureProp);
                // only cache result if read was successfull
                m_cacheSecureLinks.put(getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName), secureResource);
            } catch (CmsVfsResourceNotFoundException e) {
                secureResource = Boolean.FALSE;
                // resource does not exist, no secure link will be required for any user
                m_cacheSecureLinks.put(getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName), secureResource);
            } catch (Exception e) {
                // no secure link required (probably security issues, e.g. no access for current user)
                // however other users may be allowed to read the resource, so the result can't be cached
                secureResource = Boolean.FALSE;
            }
        }
        return secureResource.booleanValue();
    }

    /**
     * Returns <code>true</code> if the given VFS resource that is located under the 
     * given site root should be transported through a secure channel.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * @param siteRoot the site root where the the VFS resource should be read
     * 
     * @return <code>true</code> if the given VFS resource should be transported through a secure channel
     * 
     * @see #isSecureLink(CmsObject, String)
     */
    public boolean isSecureLink(CmsObject cms, String vfsName, String siteRoot) {

        if (siteRoot == null) {
            return isSecureLink(cms, vfsName);
        }

        // the site root of the cms object has to be changed so that the property can be read   
        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot(siteRoot);
            return isSecureLink(cms, vfsName);
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
    }

    /**
     * Returns true if the static export is enabled.<p>
     * 
     * @return true if the static export is enabled
     */
    public boolean isStaticExportEnabled() {

        return m_staticExportEnabled;
    }

    /**
     * Returns true if the given resource name is exportable because of it's suffix.<p>
     * 
     * @param resourceName the name to check 
     * @return true if the given resource name is exportable because of it's suffix
     */
    public boolean isSuffixExportable(String resourceName) {

        if (resourceName == null) {
            return false;
        }
        int pos = resourceName.lastIndexOf('.');
        if (pos >= 0) {
            String suffix = resourceName.substring(pos).toLowerCase();
            return m_exportSuffixes.contains(suffix);
        }
        return false;
    }

    /**
     * Checks if we have to use temporary directories during export.<p>
     * 
     * @return <code>true</code> if using temporary directories
     */
    public boolean isUseTempDir() {

        return m_useTempDirs;
    }

    /**
     * Returns true if the links in the static export should be relative.<p>
     * 
     * @param vfsName the name of the resource to export
     * 
     * @return true if the links in the static export should be relative
     */
    public boolean relativeLinksInExport(String vfsName) {

        if (vfsName != null) {
            Iterator it = m_rfsRules.iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                if (rule.getSource().matcher(vfsName).matches()) {
                    return rule.getUseRelativeLinks() != null
                    ? rule.getUseRelativeLinks().booleanValue()
                    : m_exportRelativeLinks;
                }
            }
        }
        return m_exportRelativeLinks;
    }

    /**
     * Sets the accept-charset header value.<p>
     * 
     * @param value accept-language header value
     */
    public void setAcceptCharsetHeader(String value) {

        m_acceptCharsetHeader = value;
    }

    /**
     * Sets the accept-language header value.<p>
     * 
     * @param value accept-language header value
     */
    public void setAcceptLanguageHeader(String value) {

        m_acceptLanguageHeader = value;
    }

    /**
     * Sets the default property value.<p>
     * 
     * @param value must be <code>true</code> or <code>false</code>
     */
    public void setDefault(String value) {

        m_exportPropertyDefault = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Sets the number of backups for the static export.<p>
     * 
     * @param backup number of backups
     */
    public void setExportBackups(String backup) {

        m_staticExportBackups = new Integer(backup);
    }

    /**
     * Sets the export enabled value.<p>
     * 
     * @param value must be <code>true</code> or <code>false</code>
     */
    public void setExportEnabled(String value) {

        m_staticExportEnabled = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Adds a resource pattern to the list of resources which are part of the export.<p>
     * 
     * @param folder the folder pattern to add to the list.
     */
    public void setExportFolderPattern(String folder) {

        m_exportFolders.add(folder);
    }

    /**
     * Sets specific http header for the static export.<p>
     * 
     * The format of the headers must be "header:value".<p> 
     *  
     * @param exportHeader a specific http header
     */
    public void setExportHeader(String exportHeader) {

        if (CmsStringUtil.splitAsArray(exportHeader, ':').length == 2) {
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_EXPORT_HEADERS_1, exportHeader));
            }
            m_exportHeaders.add(exportHeader);
        } else {
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_INVALID_HEADER_1, exportHeader));
            }
        }
    }

    /**
     * Sets the path where the static export is written.<p>
     * 
     * @param path the path where the static export is written
     */
    public void setExportPath(String path) {

        m_staticExportPathConfigured = path;
    }

    /**
     * Adds a suffix to the list of resource suffixes which will be exported by default.<p>
     * 
     * @param suffix the suffix to add to the list.
     */
    public void setExportSuffix(String suffix) {

        m_exportSuffixes.add(suffix.toLowerCase());
    }

    /**
     * Sets the export url.<p>
     * 
     * @param url the export url
     */
    public void setExportUrl(String url) {

        m_exportUrl = insertContextStrings(url);
        m_exportUrlConfigured = url;
    }

    /**
     * Sets the path where the static export is temporarily written.<p>
     * 
     * @param path the path where the static export is temporarily written
     */
    public void setExportWorkPath(String path) {

        m_staticExportWorkPathConfigured = path;
    }

    /**
     * Sets the link substitution handler class.<p>
     * 
     * @param handlerClassName the link substitution handler class name
     */
    public void setHandler(String handlerClassName) {

        try {
            m_handler = (I_CmsStaticExportHandler)Class.forName(handlerClassName).newInstance();
        } catch (Exception e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Sets the static export handler class.<p>
     * 
     * @param handlerClassName the static export handler class name
     */
    public void setLinkSubstitutionHandler(String handlerClassName) {

        try {
            m_linkSubstitutionHandler = (I_CmsLinkSubstitutionHandler)Class.forName(handlerClassName).newInstance();
        } catch (Exception e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Sets the plain export optimization value.<p>
     * 
     * @param value must be <code>true</code> or <code>false</code>
     */
    public void setPlainExportOptimization(String value) {

        m_quickPlainExport = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Sets the relative links value.<p>
     * 
     * @param value must be <code>true</code> or <code>false</code>
     */
    public void setRelativeLinks(String value) {

        m_exportRelativeLinks = Boolean.valueOf(value).booleanValue();
    }

    /**
     * Sets the remote address which will be used for internal requests during the static export.<p>
     * 
     * @param addr the remote address to be used
     */
    public void setRemoteAddr(String addr) {

        m_remoteAddr = addr;
    }

    /**
     * Sets the prefix for exported links in the "real" file system.<p>
     * 
     * @param rfsPrefix the prefix for exported links in the "real" file system
     */
    public void setRfsPrefix(String rfsPrefix) {

        m_rfsPrefixConfigured = rfsPrefix;
    }

    /**
     * Sets the test resource.<p>
     *  
     * @param testResource the vfs name of the test resource
     */
    public void setTestResource(String testResource) {

        m_testResource = testResource;
    }

    /**
     * Sets the prefix for internal links in the vfs.<p>
     * 
     * @param vfsPrefix the prefix for internal links in the vfs
     */
    public void setVfsPrefix(String vfsPrefix) {

        m_vfsPrefixConfigured = vfsPrefix;
    }

    /**
     * Shuts down all this static export manager.<p>
     * 
     * This is required since there may still be a thread running when the system is being shut down.<p>
     */
    public synchronized void shutDown() {

        int count = 0;
        // if the handler is still running, we must wait up to 30 seconds until it is finished
        while ((count < HANDLER_FINISH_TIME) && m_handler.isBusy()) {
            count++;
            try {
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_STATIC_EXPORT_SHUTDOWN_3,
                        m_handler.getClass().getName(),
                        String.valueOf(count),
                        String.valueOf(HANDLER_FINISH_TIME)));
                }
                wait(1000);
            } catch (InterruptedException e) {
                // if interrupted we ignore the handler, this will produce some log messages but should be ok 
                count = HANDLER_FINISH_TIME;
            }
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_1, this.getClass().getName()));
        }

    }

    /**
     * Clears the caches in the export manager.<p>
     * 
     * @param event the event that requested to clear the caches
     */
    protected void clearCaches(CmsEvent event) {

        // synchronization of this method is not required as the individual maps are all synchronized maps anyway,
        // and setExportnames() is doing it's own synchronization 

        // flush all caches   
        m_cacheOnlineLinks.clear();
        m_cacheExportUris.clear();
        m_cacheSecureLinks.clear();
        m_cacheExportLinks.clear();
        setExportnames();
        if (LOG.isDebugEnabled()) {
            String eventType = "EVENT_CLEAR_CACHES";
            if (event.getType() != I_CmsEventListener.EVENT_CLEAR_CACHES) {
                eventType = "EVENT_PUBLISH_PROJECT";
            }
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLUSHED_CACHES_1, eventType));
        }
    }

    /**
     * Creates the backup folders for the given export folder and deletes the oldest if the maximum number is reached.<p>
     * 
     * @param staticExport folder for which a new backup folder has to be created
     * @param exportPath export path to create backup path out of it
     * @param exportBackups number of maximum 
     * @param ruleBackupExtension extension for rule based backups
     */
    protected void createExportBackupFolders(
        File staticExport,
        String exportPath,
        int exportBackups,
        String ruleBackupExtension) {

        if (staticExport.exists()) {
            String backupFolderName = exportPath.substring(0, exportPath.lastIndexOf(File.separator) + 1);
            if (ruleBackupExtension != null) {
                backupFolderName = backupFolderName + EXPORT_BACKUP_FOLDER_NAME + ruleBackupExtension;
            } else {
                backupFolderName = backupFolderName + EXPORT_BACKUP_FOLDER_NAME;
            }
            for (int i = exportBackups; i > 0; i--) {
                File staticExportBackupOld = new File(backupFolderName + new Integer(i).toString());
                if (staticExportBackupOld.exists()) {
                    if ((i + 1) > exportBackups) {
                        // delete folder if it is the last backup folder
                        CmsFileUtil.purgeDirectory(staticExportBackupOld);
                    } else {
                        // set backup folder to the next backup folder name
                        staticExportBackupOld.renameTo(new File(backupFolderName + new Integer(i + 1).toString()));
                    }
                }
                // old export folder rename to first backup folder
                if (i == 1) {
                    staticExport.renameTo(staticExportBackupOld);
                }
            }

            // if no backups will be stored the old export folder has to be deleted
            if (exportBackups == 0) {
                CmsFileUtil.purgeDirectory(staticExport);
            }
        }
    }

    /**
     * Creates the parent folder for a exported resource in the RFS.<p>
     * 
     * @param exportPath the path to export the file
     * @param rfsName the rfs name of the resource
     * 
     * @throws CmsException if the folder could not be created
     */
    protected void createExportFolder(String exportPath, String rfsName) throws CmsException {

        String exportFolderName = CmsFileUtil.normalizePath(exportPath + CmsResource.getFolderPath(rfsName));
        File exportFolder = new File(exportFolderName);
        if (!exportFolder.exists()) {
            if (!exportFolder.mkdirs()) {
                throw new CmsStaticExportException(Messages.get().container(Messages.ERR_CREATE_FOLDER_1, rfsName));

            }
        }
    }

    /**
     * Returns the export data for a requested resource, if null is returned no export is required.<p>
     * 
     * @param rfsName the RFS name of the resource requested
     * @param vfsName the VFS name of the resource requested
     * @param cms an initialized cms context (should be initialized with the "Guest" user only
     * 
     * @return the export data for the request, if null is returned no export is required
     */
    protected CmsStaticExportData getExportData(String rfsName, String vfsName, CmsObject cms) {

        CmsResource resource = null;
        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");

            // cut export prefix from name
            rfsName = rfsName.substring(getRfsPrefixForRfsName(rfsName).length());

            String parameters = null;
            if (vfsName == null) {
                // check if we have the result already in the cache
                CmsStaticExportData data = getCachedExportUri(rfsName);
                if (data != null) {
                    vfsName = data.getVfsName();
                    parameters = data.getParameters();
                }
            }

            if (vfsName != null) {
                // this export uri is already cached            
                if (CACHEVALUE_404 != vfsName) {
                    // this uri can be exported
                    try {
                        resource = cms.readResource(vfsName);
                    } catch (CmsException e) {
                        // static export fails, because the guest user has no permission: 
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(
                                Messages.get().getBundle().key(
                                    Messages.ERR_EXPORT_FILE_FAILED_1,
                                    new String[] {vfsName}),
                                e);
                        }
                        return null;
                    }
                    // valid cache entry, return export data object
                    return new CmsStaticExportData(vfsName, rfsName, resource, parameters);
                } else {
                    // this uri can not be exported
                    return null;
                }
            } else {
                // export uri not in cache, must look up the file in the VFS
                vfsName = getVfsNameInternal(cms, rfsName);
                if (vfsName != null) {
                    try {
                        resource = cms.readResource(vfsName);
                    } catch (Exception e) {
                        // ignore, still not found
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(
                                Messages.get().getBundle().key(
                                    Messages.ERR_EXPORT_FILE_FAILED_1,
                                    new String[] {vfsName}),
                                e);
                        }
                    }
                }
                if (resource == null) {
                    // it could be a translated resourcename with parameters, so make a lookup
                    // in the published resources table
                    try {
                        parameters = cms.readStaticExportPublishedResourceParameters(rfsName);
                        // there was a match in the db table, so get the StaticExportData 
                        if (CmsStringUtil.isNotEmpty(parameters)) {

                            // get the rfs base string without the parameter hashcode
                            String rfsBaseName = rfsName.substring(0, rfsName.lastIndexOf('_'));

                            // get the vfs base name, which is later used to read the resource in the vfs
                            String vfsBaseName = getVfsNameInternal(cms, rfsBaseName);

                            // everything is there, so read the resource in the vfs and build the static export data object
                            if (vfsBaseName != null) {
                                resource = cms.readResource(vfsBaseName);
                                CmsStaticExportData exportData = new CmsStaticExportData(
                                    vfsBaseName,
                                    rfsName,
                                    resource,
                                    parameters);
                                cacheExportUri(rfsName, exportData.getVfsName(), parameters);
                                return exportData;
                            }
                        }
                    } catch (CmsException e) {
                        // ignore, resource does not exist
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(
                                Messages.get().getBundle().key(
                                    Messages.ERR_EXPORT_FILE_FAILED_1,
                                    new String[] {vfsName}),
                                e);
                        }
                    }
                    // no match found, nothing to export
                    cacheExportUri(rfsName, CACHEVALUE_404, null);
                    return null;
                } else {
                    // found a resource to export
                    cacheExportUri(rfsName, vfsName, null);
                    return new CmsStaticExportData(vfsName, rfsName, resource);
                }
            }
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
    }

    /**
     * Returns the longest rfs prefix matching a given already translated rfs name.<p>
     * 
     * @param rfsName the rfs name
     * 
     * @return its rfs prefix
     * 
     * @see #getRfsPrefix(String)
     */
    protected String getRfsPrefixForRfsName(String rfsName) {

        String retVal = "";
        // default case
        if (rfsName.startsWith(m_rfsPrefix + "/")) {
            retVal = m_rfsPrefix;
        }
        // additional rules
        Iterator it = m_rfsRules.iterator();
        while (it.hasNext()) {
            CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
            String rfsPrefix = rule.getRfsPrefix();
            if (rfsName.startsWith(rfsPrefix + "/") && (retVal.length() < rfsPrefix.length())) {
                retVal = rfsPrefix;
            }
        }
        return retVal;
    }

    /**
     * Returns the VFS name from a given RFS name.<p>
     * 
     * The RFS name must not contain the RFS prefix.<p>
     * 
     * @param cms an initialized OpenCms user context
     * @param rfsName the name of the RFS resource
     * 
     * @return the name of the VFS resource
     */
    protected String getVfsNameInternal(CmsObject cms, String rfsName) {

        String vfsName = null;
        CmsResource resource;

        boolean match = false;

        try {
            resource = cms.readResource(cms.getRequestContext().removeSiteRoot(rfsName));
            if (resource.isFolder() && !CmsResource.isFolder(rfsName)) {
                rfsName += '/';
            }
            vfsName = rfsName;
            match = true;
        } catch (Throwable t) {
            // resource not found          
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_EXPORT_FILE_FAILED_1, new String[] {rfsName}), t);
            }
        }

        if (!match) {
            // name of export resource could not be resolved by reading the resource directly,
            // now try to find a match with the "exportname" folders            
            Map exportnameFolders = getExportnames();
            Iterator i = exportnameFolders.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry)i.next();
                String exportName = (String)entry.getKey();
                if (rfsName.startsWith(exportName)) {
                    // prefix match
                    match = true;
                    vfsName = "" + entry.getValue() + rfsName.substring(exportName.length());
                    try {
                        resource = cms.readResource(vfsName);
                        if (resource.isFolder()) {
                            if (!CmsResource.isFolder(rfsName)) {
                                rfsName += '/';
                            }
                            if (!CmsResource.isFolder(vfsName)) {
                                vfsName += '/';
                            }
                        }
                        break;
                    } catch (CmsVfsResourceNotFoundException e) {
                        // continue with trying out the other exportname to find a match (may be a multiple prefix)
                        match = false;
                        continue;
                    } catch (CmsException e) {
                        break;
                    }
                }
            }
        }

        // finally check if its a modified jsp resource        
        if (!match) {
            // first cut of the last extension
            int extPos = rfsName.lastIndexOf('.');
            if (extPos >= 0) {
                String cutName = rfsName.substring(0, extPos);
                int pos = cutName.lastIndexOf('.');
                if (pos >= 0) {
                    // now check if remaining String ends with ".jsp"
                    String extension = cutName.substring(pos).toLowerCase();
                    if (".jsp".equals(extension)) {
                        return getVfsNameInternal(cms, cutName);
                    }
                }
            }
        }
        if (match) {
            return vfsName;
        } else {
            return null;
        }
    }

    /**
     * Substitutes the ${CONTEXT_NAME} and ${SERVLET_NAME} in a path with the real values.<p>
     * 
     * @param path the path to substitute
     * @return path with real context values
     */
    protected String insertContextStrings(String path) {

        // create a new macro resolver
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();

        // add special mappings for macros 
        resolver.addMacro("CONTEXT_NAME", OpenCms.getSystemInfo().getContextPath());
        resolver.addMacro("SERVLET_NAME", OpenCms.getSystemInfo().getServletPath());

        // resolve the macros
        return resolver.resolveMacros(path);
    }

    /**
     * Returns true if the rfs Name match against any of the defined export urls.<p>
     * 
     * @param rfsName the rfs Name to validate
     * 
     * @return true if the rfs Name match against any of the defined export urls
     */
    protected boolean isValidRfsName(String rfsName) {

        if (rfsName != null) {
            // default case
            if (rfsName.startsWith(m_rfsPrefix + "/")) {
                return true;
            }
            // additional rules
            Iterator it = m_rfsRules.iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                String rfsPrefix = rule.getRfsPrefix() + "/";
                if (rfsName.startsWith(rfsPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
      * Checks if a String is a valid URL.<p>
      * 
      * @param inputString The String to check can be <code>null</code>
      * 
      * @return <code>true</code> if the String is not <code>null</code> and a valid URL
      */
    protected boolean isValidURL(String inputString) {

        boolean isValid = false;
        try {
            if (inputString != null) {
                URL tempURL = new URL(inputString);
                isValid = (tempURL.getProtocol() != null);
            }
        } catch (MalformedURLException mue) {
            // ignore because it is not harmful
        }
        return isValid;
    }

    /**
     * Returns a normalized export path.<p>
     * 
     * Replacing macros, normalizing the path and taking care of relative paths.<p>
     * 
     * @param exportPath the export path to normalize
     * 
     * @return the normalized export path
     */
    protected String normalizeExportPath(String exportPath) {

        String result = insertContextStrings(exportPath);
        result = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(result);
        if (result.endsWith(File.separator)) {
            // ensure export path does NOT end with a File.separator
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Returns a normalized rfs prefix.<p>
     * 
     * Replacing macros and normalizing the path.<p>
     * 
     * @param rfsPrefix the prefix to normalize
     * 
     * @return the normalized rfs prefix
     */
    protected String normalizeRfsPrefix(String rfsPrefix) {

        String result = insertContextStrings(rfsPrefix);
        if (!isValidURL(result)) {
            result = CmsFileUtil.normalizePath(result, '/');
        }
        result = CmsFileUtil.normalizePath(result, '/');
        if (CmsResource.isFolder(result)) {
            // ensure prefix does NOT end with a folder '/'
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Scrubs all the "export" folders.<p>
     * 
     * @param report an I_CmsReport instance to print output message, or null to write messages to the log file   
     */
    protected void scrubExportFolders(I_CmsReport report) {

        if (report != null) {
            report.println(
                Messages.get().container(Messages.RPT_DELETING_EXPORT_FOLDERS_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        synchronized (m_lockScrubExportFolders) {
            int count = 0;
            Integer size = new Integer(m_rfsRules.size() + 1);
            // default case
            String exportFolderName = CmsFileUtil.normalizePath(m_staticExportPath + '/');
            try {
                File exportFolder = new File(exportFolderName);
                // check if export file exists, if so delete it
                if (exportFolder.exists() && exportFolder.canWrite()) {
                    CmsFileUtil.purgeDirectory(exportFolder);
                }
                count++;
                if (report != null) {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_DELETE_EXPORT_FOLDER_3,
                            new Integer(count),
                            size,
                            exportFolderName),
                        I_CmsReport.FORMAT_NOTE);
                } else {
                    // write log message
                    if (LOG.isInfoEnabled()) {
                        LOG.info(Messages.get().getBundle().key(Messages.LOG_DEL_MAIN_SE_FOLDER_1, exportFolderName));
                    }
                }
            } catch (Throwable t) {
                // ignore, nothing to do about the
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_FOLDER_DELETION_FAILED_1, exportFolderName), t);
                }
            }
            // iterate over the rules
            Iterator it = m_rfsRules.iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = (CmsStaticExportRfsRule)it.next();
                exportFolderName = CmsFileUtil.normalizePath(rule.getExportPath() + '/');
                try {
                    File exportFolder = new File(exportFolderName);
                    // check if export file exists, if so delete it
                    if (exportFolder.exists() && exportFolder.canWrite()) {
                        CmsFileUtil.purgeDirectory(exportFolder);
                    }
                    count++;
                    if (report != null) {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_DELETE_EXPORT_FOLDER_3,
                                new Integer(count),
                                size,
                                exportFolderName),
                            I_CmsReport.FORMAT_NOTE);
                    } else {
                        // write log message
                        if (LOG.isInfoEnabled()) {
                            LOG.info(Messages.get().getBundle().key(Messages.LOG_DEL_MAIN_SE_FOLDER_1, exportFolderName));
                        }
                    }
                } catch (Throwable t) {
                    // ignore, nothing to do about the
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(Messages.LOG_FOLDER_DELETION_FAILED_1, exportFolderName),
                            t);
                    }
                }
            }
        }
        if (report != null) {
            report.println(
                Messages.get().container(Messages.RPT_DELETING_EXPORT_FOLDERS_END_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
    }

    /**
     * Set the list of all resources that have the "exportname" property set.<p>
     */
    protected void setExportnames() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_UPDATE_EXPORTNAME_PROP_START_0));
        }

        List resources;
        CmsObject cms = null;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            resources = cms.readResourcesWithProperty(CmsPropertyDefinition.PROPERTY_EXPORTNAME);

            synchronized (m_lockSetExportnames) {
                Map tempExportnames = new HashMap(resources.size());
                for (int i = 0, n = resources.size(); i < n; i++) {
                    CmsResource res = (CmsResource)resources.get(i);
                    try {
                        String foldername = cms.getSitePath(res);
                        String exportname = cms.readPropertyObject(
                            foldername,
                            CmsPropertyDefinition.PROPERTY_EXPORTNAME,
                            false).getValue();
                        if (exportname != null) {
                            if (exportname.charAt(exportname.length() - 1) != '/') {
                                exportname = exportname + "/";
                            }
                            if (exportname.charAt(0) != '/') {
                                exportname = "/" + exportname;
                            }
                            tempExportnames.put(exportname, foldername);
                        }
                    } catch (CmsException e) {
                        // ignore, folder will not be added
                    }
                }
                m_exportnameResources = Collections.unmodifiableMap(tempExportnames);
            }
        } catch (CmsException e) {
            // ignore, no resources will be added at all
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_UPDATE_EXPORTNAME_PROP_FINISHED_0));
        }
    }

    /**
     * Writes a resource to the given export path with the given rfs name and the given content.<p>
     * 
     * @param req the current request
     * @param exportPath the path to export the resource
     * @param rfsName the rfs name
     * @param resource the resource
     * @param content the content
     * 
     * @throws CmsException if something goes wrong
     */
    protected void writeResource(
        HttpServletRequest req,
        String exportPath,
        String rfsName,
        CmsResource resource,
        byte[] content) throws CmsException {

        String exportFileName = CmsFileUtil.normalizePath(exportPath + rfsName);

        // make sure all required parent folder exist
        createExportFolder(exportPath, rfsName);
        // generate export file instance and output stream
        File exportFile = new File(exportFileName);
        // write new exported file content
        try {
            FileOutputStream exportStream = new FileOutputStream(exportFile);
            exportStream.write(content);
            exportStream.close();

            // log export success 
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_STATIC_EXPORTED_2,
                    resource.getRootPath(),
                    exportFileName));
            }

        } catch (Throwable t) {
            throw new CmsStaticExportException(
                Messages.get().container(Messages.ERR_OUTPUT_STREAM_1, exportFileName),
                t);
        }
        // update the file with the modification date from the server
        if (req != null) {
            Long dateLastModified = (Long)req.getAttribute(CmsRequestUtil.HEADER_OPENCMS_EXPORT);
            if ((dateLastModified != null) && (dateLastModified.longValue() != -1)) {
                exportFile.setLastModified((dateLastModified.longValue() / 1000) * 1000);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_SET_LAST_MODIFIED_2,
                        exportFile.getName(),
                        new Long((dateLastModified.longValue() / 1000) * 1000)));
                }
            }
        } else {
            // otherwise take the last modification date form the OpenCms resource
            exportFile.setLastModified((resource.getDateLastModified() / 1000) * 1000);
        }
    }
}