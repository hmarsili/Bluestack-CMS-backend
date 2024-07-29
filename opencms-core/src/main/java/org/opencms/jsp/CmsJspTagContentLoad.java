/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentLoad.java,v $
 * Date   : $Date: 2011/03/23 14:51:34 $
 * Version: $Revision: 1.44 $
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

package org.opencms.jsp;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspContentLoadBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.CmsDirectEditParams;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * Implementation of the <code>&lt;cms:contentload/&gt;</code> tag, 
 * used to access and display XML content item information from the VFS.<p>
 * 
 * Since version 7.0.2 it is also possible to store the results of the content load in the JSP context
 * using a {@link CmsJspContentLoadBean}. Using this bean the loaded XML content objects can be accessed
 * directly using the JSP EL and the JSTL. To use this feature, you need to add the <code>var</code> (and optionally
 * the <code>scope</code>) parameter to the content load tag. For example, if a parameter like 
 * <code>var="myVarName"</code> is provided, then the result of the content load is stored in the JSP 
 * context variable <code>myVarName</code> with an instance of a {@link CmsJspContentLoadBean}.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.44 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagContentLoad extends CmsJspScopedVarBodyTagSuport implements I_CmsXmlContentContainer {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 981176995635225294L;

    /** The CmsObject for the current user. */
    private transient CmsObject m_cms;

    /** The name of the collector to use for list building. */
    private String m_collector;

    /** The name of the content collector used. */
    private String m_collectorName;

    /** The parameters of the content collector uses. */
    private String m_collectorParam;

    /** The list of collected content items. */
    private List m_collectorResult;

    /** Reference to the last loaded content element. */
    private transient I_CmsXmlDocument m_content;

    /** The bean to store information required to make the result list browsable. */
    private CmsContentInfoBean m_contentInfoBean;

    /**
     * The locale to use for displaying the current content.<p>
     * 
     * Initially, this is equal to the locale set using <code>{@link #setLocale(String)}</code>. 
     * However, the content locale may change in case a loaded XML content does not have the selected locale available.
     * In this case the next default locale that is available in the content will be used as content locale.<p> 
     */
    private Locale m_contentLocale;

    /** The FlexController for the current request. */
    private CmsFlexController m_controller;

    /** The "direct edit" button selection to use for the 2nd to the last element. */
    private CmsDirectEditButtonSelection m_directEditFollowButtons;

    /** The link for creation of a new element, specified by the selected collector. */
    private String m_directEditLinkForNew;

    /** The editable mode. */
    private CmsDirectEditMode m_directEditMode;

    /** Indicates if the last element was direct editable. */
    private boolean m_directEditOpen;

    /** Indicates if this is the first content iteration loop. */
    private boolean m_isFirstLoop;

    /** Reference to the currently selected locale. */
    private Locale m_locale;

    /** The index of the current page that gets displayed. */
    private String m_pageIndex;

    /** The number of page links in the Google-like page navigation. */
    private String m_pageNavLength;

    /** The size of a page to be displayed. */
    private String m_pageSize;

    /** Parameter used for the collector. */
    private String m_param;

    /** Indicates if the collector results should be preloaded. */
    private boolean m_preload;

    /** The (optional) property to extend the parameter with. */
    private String m_property;

    /** The file name to load the current content value from. */
    private String m_resourceName;

    /**
     * Empty constructor, required for JSP tags.<p> 
     */
    public CmsJspTagContentLoad() {

        super();
    }

    /**
     * Constructor used when using <code>contentload</code> from scriptlet code.<p> 
     * 
     * @param container the parent content container (could be a preloader)
     * @param context the JSP page context
     * @param collectorName the collector name to use
     * @param collectorParam the collector param to use
     * @param locale the locale to use 
     * @param editable indicates if "direct edit" support is wanted
     * 
     * @throws JspException in case something goes wrong
     */
    public CmsJspTagContentLoad(
        I_CmsXmlContentContainer container,
        PageContext context,
        String collectorName,
        String collectorParam,
        Locale locale,
        boolean editable)
    throws JspException {

        this(container, context, collectorName, collectorParam, null, null, locale, editable);
    }

    /**
     * Constructor used when using <code>contentload</code> from scriptlet code.<p> 
     * 
     * @param container the parent content container (could be a preloader)
     * @param context the JSP page context
     * @param collectorName the collector name to use
     * @param collectorParam the collector param to use
     * @param pageIndex the display page index (may contain macros)
     * @param pageSize the display page size (may contain macros)
     * @param locale the locale to use 
     * @param editable indicates if "direct edit" support is wanted
     * 
     * @throws JspException in case something goes wrong
     */
    public CmsJspTagContentLoad(
        I_CmsXmlContentContainer container,
        PageContext context,
        String collectorName,
        String collectorParam,
        String pageIndex,
        String pageSize,
        Locale locale,
        boolean editable)
    throws JspException {

        this(
            container,
            context,
            collectorName,
            collectorParam,
            pageIndex,
            pageSize,
            locale,
            CmsDirectEditMode.valueOf(editable));
    }

    /**
     * Constructor used when using <code>contentload</code> from scriptlet code.<p> 
     * 
     * @param container the parent content container (could be a preloader)
     * @param context the JSP page context
     * @param collectorName the collector name to use
     * @param collectorParam the collector param to use
     * @param pageIndex the display page index (may contain macros)
     * @param pageSize the display page size (may contain macros)
     * @param locale the locale to use 
     * @param editMode indicates which "direct edit" mode is wanted
     * 
     * @throws JspException in case something goes wrong
     */
    public CmsJspTagContentLoad(
        I_CmsXmlContentContainer container,
        PageContext context,
        String collectorName,
        String collectorParam,
        String pageIndex,
        String pageSize,
        Locale locale,
        CmsDirectEditMode editMode)
    throws JspException {

        setCollector(collectorName);
        setParam(collectorParam);
        setPageIndex(pageIndex);
        setPageSize(pageSize);
        m_locale = locale;
        m_contentLocale = locale;
        m_directEditMode = editMode;
        m_preload = false;

        setPageContext(context);
        init(container);
    }

    /**
     * Returns the resource name currently processed.<p> 
     * 
     * @param cms the current OpenCms user context
     * @param contentContainer the current content container
     * 
     * @return the resource name currently processed
     */
    protected static String getResourceName(CmsObject cms, I_CmsXmlContentContainer contentContainer) {

        if ((contentContainer != null) && (contentContainer.getResourceName() != null)) {
            return contentContainer.getResourceName();
        } else if (cms != null) {
            return cms.getRequestContext().getUri();
        } else {
            return null;
        }
    }

    /**
     * Limits the collector's result list to the size of a page to be displayed in a JSP.<p>
     * 
     * @param contentInfoBean the info bean of the collector
     * @param collectorResult the result list of the collector
     * 
     * @return a limited collector's result list
     */
    private static List limitCollectorResult(CmsContentInfoBean contentInfoBean, List collectorResult) {

        List result = null;
        int pageCount = -1;

        if (contentInfoBean.getPageSize() > 0) {

            pageCount = collectorResult.size() / contentInfoBean.getPageSize();
            if ((collectorResult.size() % contentInfoBean.getPageSize()) != 0) {
                pageCount++;
            }

            contentInfoBean.setPageCount(pageCount);

            int startIndex = (contentInfoBean.getPageIndex() - 1) * contentInfoBean.getPageSize();
            int endIndex = contentInfoBean.getPageIndex() * contentInfoBean.getPageSize();
            if (endIndex > collectorResult.size()) {
                endIndex = collectorResult.size();
            }

            result = collectorResult.subList(startIndex, endIndex);
        } else {

            result = collectorResult;
            if (collectorResult.size() > 0) {
                contentInfoBean.setPageCount(1);
            }
        }

        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
     */
    @Override
    public int doAfterBody() throws JspException {

        // close open direct edit first
        if (hasMoreContent()) {
            // another loop is required
            return EVAL_BODY_AGAIN;
        }
        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        // no more files are available, so skip the body and finish the loop
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException, CmsIllegalArgumentException {

        // get a reference to the parent "content container" class (if available)
        Tag ancestor = findAncestorWithClass(this, I_CmsXmlContentContainer.class);
        I_CmsXmlContentContainer container = null;
        if (ancestor != null) {
            // parent content container available, use preloaded values from this container
            container = (I_CmsXmlContentContainer)ancestor;
            // check if container really is a preloader
            if (!container.isPreloader()) {
                // don't use ancestor if not a preloader
                container = null;
            }
        }

        // initialize the content load tag
        init(container);

        hasMoreContent();

        return isScopeVarSet() ? SKIP_BODY : EVAL_BODY_INCLUDE;
    }

    /**
     * Returns the collector.<p>
     *
     * @return the collector
     */
    public String getCollector() {

        return m_collector;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getCollectorName()
     */
    public String getCollectorName() {

        return m_collectorName;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getCollectorParam()
     */
    public String getCollectorParam() {

        return m_collectorParam;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getCollectorResult()
     */
    public List getCollectorResult() {

        return m_collectorResult;
    }

    /**
     * Returns the editable flag.<p>
     * 
     * @return the editable flag
     */
    public String getEditable() {

        return m_directEditMode != null ? m_directEditMode.toString() : "";
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return (m_locale != null) ? m_locale.toString() : "";
    }

    /**
     * Returns the index of the page to be displayed.<p>
     * 
     * @return the index of the page to be displayed
     */
    public String getPageIndex() {

        return m_pageIndex;
    }

    /**
     * Returns the number of page links in the Google-like page navigation.<p>
     * 
     * @return the number of page links in the Google-like page navigation
     */
    public String getPageNavLength() {

        return m_pageNavLength;
    }

    /**
     * Returns the size of a single page to be displayed.<p>
     * 
     * @return the size of a single page to be displayed
     */
    public String getPageSize() {

        return m_pageSize;
    }

    /**
     * Returns the collector parameter.<p>
     *
     * @return the collector parameter
     */
    public String getParam() {

        return m_param;
    }

    /**
     * Returns <code>"true"</code> if this content load tag should only preload the values from the collector.<p>
     * 
     * @return <code>"true"</code> if this content load tag should only preload the values from the collector
     */
    public String getPreload() {

        return String.valueOf(isPreloader());
    }

    /**
     * Returns the property.<p>
     *
     * @return the property
     */
    public String getProperty() {

        return m_property;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getResourceName()
     */
    public String getResourceName() {

        return m_resourceName;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getXmlDocument()
     */
    public I_CmsXmlDocument getXmlDocument() {

        return m_content;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getXmlDocumentElement()
     */
    public String getXmlDocumentElement() {

        // value must be set in "loop" or "show" class
        return null;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#getXmlDocumentLocale()
     */
    public Locale getXmlDocumentLocale() {

        return m_contentLocale;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#hasMoreContent()
     */
    public boolean hasMoreContent() throws JspException {

        if (m_isFirstLoop) {
            m_isFirstLoop = false;
        } else {
            if (m_directEditOpen) {
                // last element was direct editable, close it
                CmsJspTagEditable.endDirectEdit(pageContext);
                m_directEditOpen = false;
            }
        }

        if (isPreloader()) {
            // if in preload mode, no result is required            
            return false;
        }

        // check if there are more files to iterate
        boolean hasMoreContent = m_collectorResult.size() > 0;
        if (hasMoreContent) {
            // there are more results available...
            try {
                doLoadNextFile();
            } catch (CmsException e) {
                m_controller.setThrowable(e, m_resourceName);
                throw new JspException(e);
            }

            // check "direct edit" support
            if (m_directEditMode.isEnabled() && (m_resourceName != null)) {

                // check options for first element
                CmsDirectEditButtonSelection directEditButtons;
                if (m_directEditFollowButtons == null) {
                    // this is the first call, calculate the options
                    if (m_directEditLinkForNew == null) {
                        // if create link is null, show only "edit" button for first element
                        directEditButtons = CmsDirectEditButtonSelection.EDIT;
                        // also show only the "edit" button for 2nd to last element
                        m_directEditFollowButtons = directEditButtons;
                    } else {
                        // if create link is not null, show "edit", "delete" and "new" button for first element
                        directEditButtons = CmsDirectEditButtonSelection.EDIT_DELETE_NEW;
                        // show "edit" and "delete" button for 2nd to last element
                        m_directEditFollowButtons = CmsDirectEditButtonSelection.EDIT_DELETE;
                    }
                } else {
                    // re-use pre calculated options
                    directEditButtons = m_directEditFollowButtons;
                }

                m_directEditOpen = CmsJspTagEditable.startDirectEdit(pageContext, new CmsDirectEditParams(
                    m_resourceName,
                    directEditButtons,
                    m_directEditMode,
                    m_directEditLinkForNew));
            }

        } else {
            // no more results in the collector, reset locale (just to make sure...)
            m_locale = null;
        }

        return hasMoreContent;
    }

    /**
     * @see org.opencms.jsp.I_CmsXmlContentContainer#isPreloader()
     */
    public boolean isPreloader() {

        return isScopeVarSet() ? true : m_preload;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_cms = null;
        m_collector = null;
        m_collectorName = null;
        m_collectorParam = null;
        m_collectorResult = null;
        m_content = null;
        m_contentInfoBean = null;
        m_contentLocale = null;
        m_controller = null;
        m_directEditLinkForNew = null;
        m_directEditFollowButtons = null;
        m_directEditOpen = false;
        m_directEditMode = null;
        m_isFirstLoop = false;
        m_locale = null;
        m_pageIndex = null;
        m_pageNavLength = null;
        m_pageSize = null;
        m_param = null;
        m_preload = false;
        m_property = null;
        m_resourceName = null;
        super.release();
    }

    /**
     * Sets the collector.<p>
     *
     * @param collector the collector to set
     */
    public void setCollector(String collector) {

        m_collector = collector;
    }

    /**
     * Sets the editable mode.<p>
     * 
     * @param mode the mode to set
     */
    public void setEditable(String mode) {

        m_directEditMode = CmsDirectEditMode.valueOf(mode);
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        if (CmsStringUtil.isEmpty(locale)) {
            m_locale = null;
            m_contentLocale = null;
        } else {
            m_locale = CmsLocaleManager.getLocale(locale);
            m_contentLocale = m_locale;
        }
    }

    /**
     * Sets the index of the page to be displayed.<p>
     * 
     * @param pageIndex the index of the page to be displayed
     */
    public void setPageIndex(String pageIndex) {

        m_pageIndex = pageIndex;
    }

    /**
     * Sets the number of page links in the Google-like page navigation.<p>
     * 
     * @param pageNavLength the number of page links in the Google-like page navigation
     */
    public void setPageNavLength(String pageNavLength) {

        m_pageNavLength = pageNavLength;
    }

    /**
     * Sets the size of a single page to be displayed.<p>
     * 
     * @param pageSize the size of a single page to be displayed
     */
    public void setPageSize(String pageSize) {

        m_pageSize = pageSize;
    }

    /**
     * Sets the collector parameter.<p>
     *
     * @param param the collector parameter to set
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Sets the preload flag for this content load tag.<p> 
     * 
     * If this is set to <code>true</code>, then the collector result will only 
     * be preloaded, but not iterated.<p> 
     * 
     * @param preload the preload flag to set
     */
    public void setPreload(String preload) {

        m_preload = Boolean.valueOf(preload).booleanValue();
    }

    /**
     * Sets the property.<p>
     *
     * @param property the property to set
     */
    public void setProperty(String property) {

        m_property = property;
    }

    /**
     * Load the next file name from the initialized list of file names.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void doLoadNextFile() throws CmsException {

        // get the next resource from the collector
        CmsResource resource = getNextResource();
        if (resource == null) {
            m_resourceName = null;
            m_content = null;
            return;
        }

        // set the resource name
        m_resourceName = m_cms.getSitePath(resource);

        // upgrade the resource to a file
        CmsFile file = m_cms.readFile(resource);

        // unmarshal the XML content from the resource, don't use unmarshal(CmsObject, CmsResource) 
        // as no support for getting the historic version that has been cached by a CmsHistoryResourceHandler 
        // will come from there!
        m_content = CmsXmlContentFactory.unmarshal(m_cms, file, pageContext.getRequest());

        // check if locale is available
        m_contentLocale = m_locale;
        if (!m_content.hasLocale(m_contentLocale)) {
            Iterator it = OpenCms.getLocaleManager().getDefaultLocales().iterator();
            while (it.hasNext()) {
                Locale locale = (Locale)it.next();
                if (m_content.hasLocale(locale)) {
                    // found a matching locale
                    m_contentLocale = locale;
                    break;
                }
            }
        }
    }

    /**
     * Returns the content info bean.<p>
     * 
     * @return the content info bean
     */
    protected CmsContentInfoBean getContentInfoBean() {

        return m_contentInfoBean;
    }

    /**
     * Initializes this content load tag.<p> 
     * 
     * @param container the parent container (could be a preloader)
     * 
     * @throws JspException in case something goes wrong
     */
    protected void init(I_CmsXmlContentContainer container) throws JspException {

        // check if the tag contains a pageSize, pageIndex and pageNavLength attribute, or none of them
        int pageAttribCount = 0;
        pageAttribCount += CmsStringUtil.isNotEmpty(m_pageSize) ? 1 : 0;
        pageAttribCount += CmsStringUtil.isNotEmpty(m_pageIndex) ? 1 : 0;

        if ((pageAttribCount > 0) && (pageAttribCount < 2)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_TAG_CONTENTLOAD_INDEX_SIZE_0));
        }

        I_CmsXmlContentContainer usedContainer;
        if (container == null) {
            // no preloading ancestor has been found
            usedContainer = this;
            if (CmsStringUtil.isEmpty(m_collector)) {
                // check if the tag contains a collector attribute
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_TAG_CONTENTLOAD_MISSING_COLLECTOR_0));
            }
            if (CmsStringUtil.isEmpty(m_param)) {
                // check if the tag contains a param attribute
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_TAG_CONTENTLOAD_MISSING_PARAM_0));
            }
        } else {
            // use provided container (preloading ancestor)
            usedContainer = container;
        }

        if (isPreloader()) {
            // always disable direct edit for preload
            m_directEditMode = CmsDirectEditMode.FALSE;
        } else if (m_directEditMode == null) {
            // direct edit mode must not be null
            m_directEditMode = CmsDirectEditMode.FALSE;
        }

        // initialize OpenCms access objects
        m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();

        // get the resource name from the selected container
        String resourcename = getResourceName(m_cms, usedContainer);

        // initialize a string mapper to resolve EL like strings in tag attributes
        CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(m_cms).setJspPageContext(pageContext).setResourceName(
            resourcename).setKeepEmptyMacros(true);

        // resolve the collector name
        if (container == null) {
            // no preload parent container, initialize new values
            m_collectorName = resolver.resolveMacros(getCollector());
            // resolve the parameter
            m_collectorParam = resolver.resolveMacros(getParam());
            m_collectorResult = null;
        } else {
            // preload parent content container available, use values from this container
            m_collectorName = usedContainer.getCollectorName();
            m_collectorParam = usedContainer.getCollectorParam();
            m_collectorResult = usedContainer.getCollectorResult();
            if (m_locale == null) {
                // use locale from ancestor if available
                m_locale = usedContainer.getXmlDocumentLocale();
            }
        }

        if (m_locale == null) {
            // no locale set, use locale from users request context
            m_locale = m_cms.getRequestContext().getLocale();
        }

        try {
            // now collect the resources
            I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(m_collectorName);
            if (collector == null) {
                throw new CmsException(Messages.get().container(Messages.ERR_COLLECTOR_NOT_FOUND_1, m_collectorName));
            }
            // execute the collector if not already done in parent tag
            if (m_collectorResult == null) {
                m_collectorResult = collector.getResults(m_cms, m_collectorName, m_collectorParam);
            }

            m_contentInfoBean = new CmsContentInfoBean();
            m_contentInfoBean.setPageSizeAsString(resolver.resolveMacros(m_pageSize));
            m_contentInfoBean.setPageIndexAsString(resolver.resolveMacros(m_pageIndex));
            m_contentInfoBean.setPageNavLengthAsString(resolver.resolveMacros(m_pageNavLength));
            m_contentInfoBean.setResultSize(m_collectorResult.size());
            m_contentInfoBean.setLocale(m_locale.toString());
            m_contentInfoBean.initResultIndex();

            if (!isPreloader()) {
                // not required when only preloading 
                m_collectorResult = CmsJspTagContentLoad.limitCollectorResult(m_contentInfoBean, m_collectorResult);
                m_contentInfoBean.initPageNavIndexes();

                String createParam = collector.getCreateParam(m_cms, m_collectorName, m_collectorParam);
                if (createParam != null) {
                    // use "create link" only if collector supports it
                    m_directEditLinkForNew = CmsEncoder.encode(m_collectorName + "|" + createParam);
                }
            } else if (isScopeVarSet()) {
                // scope variable is set, store content load bean in JSP context
                CmsJspContentLoadBean bean = new CmsJspContentLoadBean(m_cms, m_locale, m_collectorResult);
                storeAttribute(bean);
            }

        } catch (CmsException e) {
            m_controller.setThrowable(e, m_cms.getRequestContext().getUri());
            throw new JspException(e);
        }

        // reset the direct edit options (required because of re-used tags)
        m_directEditOpen = false;
        m_directEditFollowButtons = null;

        // the next loop is the first loop
        m_isFirstLoop = true;
    }

    /**
     * Returns the next resource from the collector.<p>
     * 
     * @return the next resource from the collector
     */
    private CmsResource getNextResource() {

        if ((m_collectorResult != null) && (m_collectorResult.size() > 0)) {

            m_contentInfoBean.incResultIndex();
            return (CmsResource)m_collectorResult.remove(0);
        }

        return null;
    }
}