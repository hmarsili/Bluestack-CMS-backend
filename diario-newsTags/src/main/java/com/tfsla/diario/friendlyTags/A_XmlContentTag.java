package com.tfsla.diario.friendlyTags;

import java.util.Iterator;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

public class A_XmlContentTag extends BaseTag {

    protected transient I_CmsXmlDocument m_content;

    protected Locale m_contentLocale;

    protected Locale m_locale;

	protected void init(CmsResource resource)
	{
		super.init();
        try {
			CmsFile file = m_cms.readFile(resource);
			
		       m_content = CmsXmlContentFactory.unmarshal(m_cms, file, pageContext.getRequest());

		        if (m_locale == null) {
		            // no locale set, use locale from users request context
		            m_locale = m_cms.getRequestContext().getLocale();
		        }

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

		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void init(String resourcePath)
	{
		super.init();
        try {
			CmsFile file = m_cms.readFile(resourcePath, CmsResourceFilter.ALL);
			
		       m_content = CmsXmlContentFactory.unmarshal(m_cms, file, pageContext.getRequest());

		        if (m_locale == null) {
		            // no locale set, use locale from users request context
		            m_locale = m_cms.getRequestContext().getLocale();
		        }

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

		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	public void setLocale(String locale) {
		if (CmsStringUtil.isEmpty(locale)) {
            m_locale = null;
            m_contentLocale = null;
        } else {
            m_locale = CmsLocaleManager.getLocale(locale);
            m_contentLocale = m_locale;
        }
    }

    public I_CmsXmlDocument getXmlDocument() {

        return m_content;
    }

	public Locale getXmlDocumentLocale() {
		return m_contentLocale;
	}

}
