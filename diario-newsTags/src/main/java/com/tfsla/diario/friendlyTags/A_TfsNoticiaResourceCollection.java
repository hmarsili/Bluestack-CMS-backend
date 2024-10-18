package com.tfsla.diario.friendlyTags;

import java.util.Iterator;
import java.util.Locale;

import jakarta.servlet.jsp.JspTagException;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.model.TfsListaNoticias;
import com.tfsla.diario.model.TfsNoticia;

public class A_TfsNoticiaResourceCollection extends A_TfsNoticiaCollectionWithBlanks  implements I_TfsNoticia {

    protected transient I_CmsXmlDocument m_content;

    protected Locale m_contentLocale;

    protected Locale m_locale;

	protected void initResource(String newsPath)
	{
	        try {
	        	
	    	    // get the current users OpenCms context
	    	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

				CmsFile file = cms.readFile(newsPath);
				
			       m_content = CmsXmlContentFactory.unmarshal(cms, file, pageContext.getRequest());
	
			        if (m_locale == null) {
			            // no locale set, use locale from users request context
			            m_locale = cms.getRequestContext().getLocale();
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
	
			        TfsNoticia noticia = new TfsNoticia(cms,m_content,m_contentLocale,pageContext);
					
					TfsNoticia anterior = (TfsNoticia) pageContext.getRequest().getAttribute("news");
					TfsListaNoticias lista = new TfsListaNoticias(getsize(),this.index,getsize(),1);
					lista.setCurrentsection(noticia.getSection());
					try {
						
						lista.setCurrentPriorityHome(Integer.parseInt(noticia.getPriorityhome()));
					} 
					catch (NumberFormatException e)
					{
						lista.setCurrentPriorityHome(30);				
					}
					try {
							lista.setCurrentprioritysection(Integer.parseInt(noticia.getPrioritysection()));
						} 
						catch (NumberFormatException e)
						{
							lista.setCurrentprioritysection(30);				
						}
					
					if (anterior!=null)
					{
						try {
							lista.setPriorityhomechanged(!anterior.getPriorityhome().equals(noticia.getPriorityhome()));
						} 
						catch (NumberFormatException e)
						{
							lista.setPriorityhomechanged(true);				
						}
						try {
							lista.setPrioritysectionchanged(!anterior.getPrioritysection().equals(noticia.getPrioritysection()));
						} 
						catch (NumberFormatException e)
						{
							lista.setPrioritysectionchanged(true);				
						}
						lista.setSectionchanged(!anterior.getSection().equals(noticia.getSection()));
					}

					pageContext.getRequest().setAttribute("newslist", lista);
					pageContext.getRequest().setAttribute("news", noticia);

					
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

	}

	protected int getsize()
	{
		int size=0;
		
		I_TfsNoticia noticia;
		try {
			noticia = getCurrentNews();
		} catch (JspTagException e) {
			return 0;
		}
		
		for (int j=1;j<=lastElement;j++)
		{
			String controlValue = getIndexElementValue(noticia,keyControlName,j);
			if (!controlValue.trim().equals(""))
				size++;
		}
		
		return size;

	}
	@Override
	protected boolean hasMoreContent() {
		index++;

		boolean withElement=false;
		
		while (index<=lastElement && !withElement) {
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return false;
			}
			String controlValue = getIndexElementValue(noticia,keyControlName);
			if (!controlValue.trim().equals(""))
			{
				withElement=true;
				initResource(controlValue);
			}
			else
				index++;
		}
		
		return (index<=lastElement);
	}

	public String getKeyControlName() {
		return keyControlName;
	}

	public void setKeyControlName(String keyControlName) {
		this.keyControlName = keyControlName;
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
