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

public abstract class A_TfsTriviaSplitElement extends BodyTagSupport implements I_TfsCollectionListTag {

    String[] items = null;
    protected int idx = -1;
    String separator = " ";

	public I_TfsTrivia getCurrentTrivia() throws JspTagException {
	    Tag ancestor = findAncestorWithClass(this, I_TfsTrivia.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Trivia not accesible");
	    }
	
	    I_TfsTrivia trivia = (I_TfsTrivia) ancestor;
		return trivia;
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
		return getElementValue(this.getCurrentTrivia(), name);
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		try {
			return getIndexElementValuesSize(this.getCurrentTrivia(), name);
		} catch (JspTagException e) {
			return 0;
		}
	}

	public String getCollectionIndexValue(String name, int index) throws JspTagException {
		return getIndexElementValues(this.getCurrentTrivia(),name,index);
	}

	public String getIndexElementValues(I_TfsTrivia trivia, String elementName,int index) {
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    I_CmsXmlDocument xmlContent = trivia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, trivia.getXmlDocumentLocale(), index);
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName, e);
		}
	
		return "";
	}

	public int getIndexElementValuesSize(I_TfsTrivia trivia, String elementName) {
	    //CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    I_CmsXmlDocument xmlContent = trivia.getXmlDocument();
	    
		return xmlContent.getValues(elementName, trivia.getXmlDocumentLocale()).size();
	}
	public String getElementValue(I_TfsTrivia trivia, String elementName) {
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    I_CmsXmlDocument xmlContent = trivia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, trivia.getXmlDocumentLocale());
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
    protected static final Log LOG = CmsLog.getLog(A_TfsTriviaSplitElement.class);
}