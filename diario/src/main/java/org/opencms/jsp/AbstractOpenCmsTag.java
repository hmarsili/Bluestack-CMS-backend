package org.opencms.jsp;

import java.util.Locale;

import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
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

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.tags.AbstractTag;

/**
 * Es una clase base para extender tags de openCMS
 * Debe estar en este package, porque usa cosas con visibilidad al package
 * que están en el código de OpenCMS
 * 
 * Por limitaciones de OpenCMS, al redefinir los métodos release y endTag, deben llamar al super. 
 * 
 * @author lgassman
 */
public abstract class AbstractOpenCmsTag extends AbstractTag {

    /** The log object for this class. */
	private final Log log = CmsLog.getLog(this.getClass());
	/** Locale of the content node elemen to show. */
    private Locale m_locale;
	/** Name of the content node element to show. */
	private String m_element;
    
	// *******************************************
	// **************Helpers**********************
	// *******************************************

	protected I_CmsXmlContentContainer getAncestor() {
		Tag ancestor = TagSupport.findAncestorWithClass(this, I_CmsXmlContentContainer.class);
		if (ancestor == null) {
			CmsMessageContainer errMsgContainer = Messages.get().container(
					Messages.ERR_PARENTLESS_TAG_1, "contentshow");
			String msg = Messages.getLocalizedMessage(errMsgContainer, this
					.getPageContext());
			throw new ApplicationException(msg);
		}
		return (I_CmsXmlContentContainer) ancestor;
	}
	
	protected Locale toLocale(String locale) {
        return (!CmsStringUtil.isEmpty(locale)) ? CmsLocaleManager.getLocale(locale) : null;
	}
	
    /**
     * Devuelve el content o un String vacio ("") si no existe
     * @return
     */
	protected String getContent() {
		String content = this.getContentOrNull();
    	return (content != null) ? content : "";
	}

	/**
     * Devuelve el content o null ("") si no existe
     * @return
     */
	protected String getContentOrNull() {
		return contentShowTagAction(getAncestor(), this.getPageContext(), getElement(), this.m_locale);
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
	 * Returns the name of the content node element to show.<p>
	 * 
	 * @return the name of the content node element to show
	 */
	public String getElement() {
	
	    return (this.m_element != null) ? this.m_element : "";
	}

	public void setElement(String element) {
	
		this.m_element = element;
	}

	public void setLocale(String locale) {
	
	    if (CmsStringUtil.isEmpty(locale)) {
	    	this.m_locale = null;
	    } else {
	    	this.m_locale = CmsLocaleManager.getLocale(locale);
	    }
	}

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
	public String contentShowTagAction(I_CmsXmlContentContainer container, PageContext context, String element, Locale locale) {
	
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
	
	        if (xmlContent.hasValue(element, locale)) {
	            try {
	                // read the element from the content
	                content = xmlContent.getStringValue(cms, element, locale);
	            } catch (Exception e) {
	                getLog().error(Messages.get().getBundle().key(Messages.LOG_ERR_CONTENT_SHOW_1, element), e);
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

	protected static String getResourceName(CmsObject cms, I_CmsXmlContentContainer contentContainer) {
  	
        if (contentContainer != null && contentContainer.getResourceName() != null) {
            return contentContainer.getResourceName();
        } else if (cms != null) {
            return cms.getRequestContext().getUri();
        } else {
            return null;
        }
    }

	protected Log getLog() {
		return this.log;
	}
	
    @Override
    public int doEndTag() {
        // need to release manually, JSP container may not call release as required (happens with Tomcat)
        release();
        return EVAL_PAGE;
    }

}
