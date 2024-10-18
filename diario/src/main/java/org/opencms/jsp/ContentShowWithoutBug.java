/*
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;

import java.io.IOException;
import java.util.Locale;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Es una copia igualita a CmsJspTagContentShow pero resuleve el bug de cuando están vacíos los elementos
 *  
 * @since 6.0.0 
 */
public class ContentShowWithoutBug extends TagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(ContentShowWithoutBug.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -6776067180965738432L;

    /** Name of the content node element to show. */
    private String m_element;

    /** Locale of the content node elemen to show. */
    private Locale m_locale;

    
    // ********************************************
    // ** Acá se hackea para resolver el bug
    // ********************************************
    /**
     * 
     * @param element
     * @param locale
     * @param xmlContent
     * @return
     */
    private static boolean hasValue(String element, Locale locale, I_CmsXmlDocument xmlContent) {
    	try {
    		return xmlContent.hasValue(element, locale);
    	}
    	catch(NullPointerException e) {
    		return false;
    	}
    }

    // ********************************************
    // ** Hasta acá se hackea para resolver el bug
    // ********************************************
    
    
    
    /**
     * Internal action method to show an element from a XML content document.<p>
     * 
     * @param container the content container to read the XML content from
     * @param context the current JSP page context
     * @param element the node name of the element to show
     * @param locale the locale of the element to show
     * 
     * @return the value of the selected content element
     */
    public static String contentShowTagAction(
        I_CmsXmlContentContainer container,
        PageContext context,
        String element,
        Locale locale) {

        // get the current users OpenCms context
        CmsObject cms = CmsFlexController.getCmsObject(context.getRequest());

        // get loaded content from content container
        I_CmsXmlDocument xmlContent = container.getXmlDocument();

        if (CmsStringUtil.isEmpty(element)) {
            element = container.getXmlDocumentElement();
        } else {
            element = CmsXmlUtils.concatXpath(container.getXmlDocumentElement(), element);
        }

        String content;
        if (CmsMacroResolver.isMacro(element)) {
            // this is a macro, initialize a macro resolver
            String resourcename = CmsJspTagContentLoad.getResourceName(cms, container);
            CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(cms).setJspPageContext(context).setResourceName(
                resourcename).setKeepEmptyMacros(true);
            // resolve the macro
            content = resolver.resolveMacros(element);
        } else if (xmlContent == null) {
            // no XML content- no output
            content = null;
        } else {

            // determine the locale to display
            if (locale == null) {
                // no locale was set, use default from parent tag (usually "contentload")
                locale = container.getXmlDocumentLocale();
            }
            // now get the content element value to display

            if (hasValue(element, locale, xmlContent)) {
                try {
                    // read the element from the content
                    content = xmlContent.getStringValue(cms, element, locale);
                } catch (Exception e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_CONTENT_SHOW_1, element), e);
                    content = null;
                }
            } else {
                content = null;
            }

            // make sure that no null String is returned
            if (content == null) {
                content = CmsMessages.formatUnknownKey(element);
            }
        }

        return content;
    }


    /**
     * @see jakarta.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {

        // need to release manually, JSP container may not call release as required (happens with Tomcat)
        release();
        return EVAL_PAGE;
    }

    /**
     * @see jakarta.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsXmlContentContainer.class);
        if (ancestor == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(Messages.ERR_PARENTLESS_TAG_1, "contentshow");
            String msg = Messages.getLocalizedMessage(errMsgContainer, this.pageContext);
            throw new JspTagException(msg);
        }
        I_CmsXmlContentContainer contentContainer = (I_CmsXmlContentContainer)ancestor;

        // now get the content element value to display
        String content = contentShowTagAction(contentContainer, this.pageContext, getElement(), this.m_locale);

        try {
            if (content != null) {
                this.pageContext.getOut().print(content);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_JSP_BEAN_0), e);
            }
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    /**
     * Returns the name of the content node element to show.<p>
     * 
     * @return the name of the content node element to show
     */
    public String getElement() {

        return (this.m_element != null) ? this.m_element : "";
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return (this.m_locale != null) ? this.m_locale.toString() : "";
    }

    /**
     * @see jakarta.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        this.m_element = null;
        this.m_locale = null;
        super.release();
    }

    /**
     * Sets the name of the content node element to show.<p>
     * 
     * @param element the name of the content node element to show
     */
    public void setElement(String element) {

        this.m_element = element;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        if (CmsStringUtil.isEmpty(locale)) {
            this.m_locale = null;
        } else {
            this.m_locale = CmsLocaleManager.getLocale(locale);
        }
    }
}