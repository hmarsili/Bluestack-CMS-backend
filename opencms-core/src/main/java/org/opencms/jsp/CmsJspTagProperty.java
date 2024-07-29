/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagProperty.java,v $
 * Date   : $Date: 2011/03/23 14:51:34 $
 * Version: $Revision: 1.28 $
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

import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the properties of a resource in the OpenCms VFS .<p>
 * 
 * Of particular importance is the setting of the <code>file</code> attribute,
 * which can take the following values.<p>
 * 
 * This attribute allows you to specify where to search for the property.<BR>            
 * The following values are supported: 
 * </P>
 * <DL>
 *   <DT><b>uri</b> (default) 
 *   <DD>  Look up  the property on the file with the 
 *   uri requested by the user. 
 *   <DT><b>search.uri</b> or <b>search</b> 
 *   <DD>Look up the property by also checking all parent folders for the property, 
 *   starting with the file with uri requested by the user and 
 *   going "upward" if the property was not found there. 
 *   <DT><b>element.uri</b> 
 *   <DD>Look up the property on the currently 
 *   processed sub - element. This is useful in templates or other pages that 
 *   consist of many elements.   
 *   <DT><b>search.element.uri</b> 
 *   <DD>Look up the property by also checking all parent folders for the 
 *   property, starting with the file with the currently processed sub - 
 *   element and going "upward" if the property was not found there.
 *   <DT><B>{some-file-uri}</B> 
 *   <DD>Look up the property on that exact file 
 *   uri in the OpenCms VFS,<EM> fallback if no other valid option is 
 *   selected for the file attribute.</EM>           
 *   </DD>
 * </DL>
 *   
 * <P>There are also some deprecated options for the "file" value that are 
 * still supported but should not longer be used:</P>
 * <DL>
 *   <DT>parent 
 *   <DD>same as <STRONG>uri</STRONG> 
 *   <DT>search-parent 
 *   <DD>same as <STRONG>search.uri</STRONG> 
 *   <DT>this
 *   <DD>same as <STRONG>element.uri</STRONG> 
 *   <DT>search-this 
 *   <DD>same as <STRONG>search.element.uri</STRONG></DD>
 * </DL>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.28 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagProperty extends TagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -4040833541258687977L;

    /** Accessor constant: Use element uri. */
    public static final String USE_ELEMENT_URI = "element.uri";

    /** Accessor constant: Use parent (same as USE_URI). */
    public static final String USE_PARENT = "parent";

    /** Accessor constant: Use search (same as USE_SEARCH_URI). */
    public static final String USE_SEARCH = "search";

    /** Accessor constant: Use search element uri. */
    public static final String USE_SEARCH_ELEMENT_URI = "search.element.uri";

    /** Accessor constant: Search parent (same as USE_SEARCH_URI). */
    public static final String USE_SEARCH_PARENT = "search-parent";

    /** Accessor constant: Use seach this (same as USE_SEARCH_ELEMENT_URI). */
    public static final String USE_SEARCH_THIS = "search-this";

    /** Accessor constant: Search uri. */
    public static final String USE_SEARCH_URI = "search.uri";

    /** Accessor constant: Use this (same as USE_ELEMENT_URI). */
    public static final String USE_THIS = "this";

    /** Accessor constant: Use uri. */
    public static final String USE_URI = "uri";

    /** Static array of the possible "file" properties. */
    // automatic member sorting will cause compilation error (static order) due to the naming convention.
    static final String[] ACTION_VALUES = {
        USE_URI,
        USE_PARENT,
        USE_SEARCH,
        USE_SEARCH_URI,
        USE_SEARCH_PARENT,
        USE_ELEMENT_URI,
        USE_THIS,
        USE_SEARCH_ELEMENT_URI,
        USE_SEARCH_THIS};

    /** Array list for fast lookup. */
    // automatic member sorting will cause compilation error. 
    public static final List ACTION_VALUES_LIST = Arrays.asList(ACTION_VALUES);

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagProperty.class);

    /** The default value. */
    private String m_defaultValue;

    /** Indicates if HTML should be escaped. */
    private boolean m_escapeHtml;

    /** The file to read the property from. */
    private String m_propertyFile;

    /** The name of the property to read. */
    private String m_propertyName;

    /**
     * Internal action method.<p>
     * 
     * @param property the property to look up
     * @param action the search action
     * @param defaultValue the default value
     * @param escape if the result html should be escaped or not
     * @param req the current request
     * @return String the value of the property or <code>null</code> if not found (and no
     *      defaultValue provided)
     * @throws CmsException if something goes wrong
     */
    public static String propertyTagAction(
        String property,
        String action,
        String defaultValue,
        boolean escape,
        ServletRequest req) throws CmsException {

        CmsFlexController controller = CmsFlexController.getController(req);

        // if action is not set use default
        if (action == null) {
            action = ACTION_VALUES[0];
        }

        String value;
        String vfsUri;
        boolean search;
        switch (ACTION_VALUES_LIST.indexOf(action)) {
            case 0: // USE_URI
            case 1: // USE_PARENT
                // read properties of parent (i.e. top requested) file
                vfsUri = controller.getCmsObject().getRequestContext().getUri();
                search = false;
                break;
            case 2: // USE_SEARCH
            case 3: // USE_SEARCH_URI
            case 4: // USE_SEARCH_PARENT 
                // try to find property on parent file and all parent folders
                vfsUri = controller.getCmsObject().getRequestContext().getUri();
                search = true;
                break;
            case 5: // USE_ELEMENT_URI
            case 6: // USE_THIS
                // read properties of this file            
                vfsUri = controller.getCurrentRequest().getElementUri();
                search = false;
                break;
            case 7: // USE_SEARCH_ELEMENT_URI
            case 8: // USE_SEARCH_THIS
                // try to find property on this file and all parent folders
                vfsUri = controller.getCurrentRequest().getElementUri();
                search = true;
                break;
            default:
                // read properties of the file named in the attribute  
                vfsUri = CmsLinkManager.getAbsoluteUri(action, controller.getCurrentRequest().getElementUri());
                search = false;
        }
        // now read the property from the VFS
        value = controller.getCmsObject().readPropertyObject(vfsUri, property, search).getValue(defaultValue);
        if (escape) {
            // HTML escape the value 
            value = CmsEncoder.escapeHtml(value);
        }
        return value;
    }

    /**
     * @return SKIP_BODY
     * @throws JspException in case somethins goes wrong
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                String prop = propertyTagAction(getName(), getFile(), m_defaultValue, m_escapeHtml, req);
                // Make sure that no null String is returned
                if (prop == null) {
                    prop = "";
                }
                pageContext.getOut().print(prop);

            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "property"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the default value.<p>
     * 
     * @return the default value
     */
    public String getDefault() {

        return m_defaultValue != null ? m_defaultValue : "";
    }

    /**
     * The value of the escape html flag.<p>
     * 
     * @return the value of the escape html flag
     */
    public String getEscapeHtml() {

        return "" + m_escapeHtml;
    }

    /**
     * Returns the file name.<p>
     * 
     * @return the file name
     */
    public String getFile() {

        return m_propertyFile != null ? m_propertyFile : "parent";
    }

    /**
     * Returns the property name.<p>
     * 
     * @return String the property name
     */
    public String getName() {

        return m_propertyName != null ? m_propertyName : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_propertyFile = null;
        m_propertyName = null;
        m_defaultValue = null;
        m_escapeHtml = false;
    }

    /**
     * Sets the default value.<p>
     * 
     * This is used if a selected property is not found.<p>
     * 
     * @param def the default value
     */
    public void setDefault(String def) {

        if (def != null) {
            m_defaultValue = def;
        }
    }

    /**
     * Set the escape html flag.<p>
     * 
     * @param value should be <code>"true"</code> or <code>"false"</code> (all values other then <code>"true"</code> are
     * considered to be false)
     */
    public void setEscapeHtml(String value) {

        if (value != null) {
            m_escapeHtml = Boolean.valueOf(value.trim()).booleanValue();
        }
    }

    /**
     * Sets the file name.<p>
     * 
     * @param file the file name
     */
    public void setFile(String file) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(file)) {
            m_propertyFile = file;
        }
    }

    /**
     * Sets the property name.<p>
     * 
     * @param name the property name to set
     */
    public void setName(String name) {

        if (name != null) {
            m_propertyName = name;
        }
    }

}
