package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

public abstract class A_TfsNoticiaSplitElement extends BodyTagSupport implements I_TfsCollectionListTag {

    String[] items = null;
    protected int idx = -1;
    String separator = " ";

	public I_TfsNoticia getCurrentNews() throws JspTagException {
	    Tag ancestor = findAncestorWithClass(this, I_TfsNoticia.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag News not accesible");
	    }
	
	    I_TfsNoticia noticia = (I_TfsNoticia) ancestor;
		return noticia;
	}
	
	public boolean hasMoreContent() {
		idx++;
		
		if (items == null) {
			return false;
		}
		
		if (idx < items.length) {
			return true;
		}
		
		return false;
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		return getElementValue(this.getCurrentNews(), name);
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		try {
			return getIndexElementValuesSize(this.getCurrentNews(), name);
		} catch (JspTagException e) {
			return 0;
		}
	}

	public String getCollectionIndexValue(String name, int index) throws JspTagException {
		return getIndexElementValues(this.getCurrentNews(),name,index);
	}

	public String getIndexElementValues(I_TfsNoticia noticia, String elementName,int index) {
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, noticia.getXmlDocumentLocale(), index);
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName, e);
		}
	
		return "";
	}

	public int getIndexElementValuesSize(I_TfsNoticia noticia, String elementName) {
	    //CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
		return xmlContent.getValues(elementName, noticia.getXmlDocumentLocale()).size();
	}
	public String getElementValue(I_TfsNoticia noticia, String elementName) {
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, noticia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName,e);
		}
	
		return "";
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public int getIndex() {
		return idx;
	}

	public boolean isLast() {
		return (idx == items.length-1);
	}

	public String getCurrentItem() {
		return items[idx].trim();
	}

	public String getCollectionPathName() throws JspTagException {
		return "";
	}
	
	private static final long serialVersionUID = 4080252888554066620L;
    protected static final Log LOG = CmsLog.getLog(A_TfsNoticiaSplitElement.class);
}