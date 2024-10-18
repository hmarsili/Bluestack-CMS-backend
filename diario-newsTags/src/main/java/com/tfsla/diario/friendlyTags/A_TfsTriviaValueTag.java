package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

public abstract class A_TfsTriviaValueTag extends TagSupport implements I_TfsCollectionListTag{

	private static final long serialVersionUID = 1257302667107698543L;

	protected String keyName = "";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(A_TfsTriviaValueTag.class);

    protected CmsObject cms = null;
    
	protected String getPropertyValue(I_TfsTrivia trivia, String propertyName, boolean search) {
	   
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    CmsProperty property=null;
		try {
			property = cms.readPropertyObject(trivia.getXmlDocument().getFile(), propertyName, search);
		} catch (CmsException e) {
			LOG.error("Error reading property " + propertyName,e);
		}
	    
	    if (property!=null)
	    	return property.getValue("");
	    
	    return "";
	}

	protected String getElementValue(I_TfsTrivia trivia, String elementName) {
	    
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    I_CmsXmlDocument xmlContent = trivia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, trivia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName,e);
		}
	
		return "";
	}
	
	
	public String getIndexElementValues(I_TfsTrivia trivia, String elementName,int index) {
	   
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    I_CmsXmlDocument xmlContent = trivia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, trivia.getXmlDocumentLocale(),index);
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName,e);
		}
	
		return "";
	}

	public int getIndexElementValuesSize(I_TfsTrivia trivia, String elementName) {
	 
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    I_CmsXmlDocument xmlContent = trivia.getXmlDocument();
	    
		return xmlContent.getValues(elementName, trivia.getXmlDocumentLocale()).size();
	}

	protected I_TfsTrivia getCurrentTrivia() throws JspTagException {
	    Tag ancestor = findAncestorWithClass(this, I_TfsTrivia.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Trivia not accesible");
	    }
	
	    I_TfsTrivia trivia = (I_TfsTrivia) ancestor;
		return trivia;
	}

	protected void printContent(String content) throws JspException {
		try {
	            pageContext.getOut().print(content);
	    } catch (IOException e) {
	        if (LOG.isErrorEnabled()) {
	            LOG.error("Error trying to retrieve Title", e);
	        }
	        throw new JspException(e);
	    }
	}

	public int getIndex() {
		return 0;
	}

	public boolean isLast() {
		return true;
	}

	public String getCollectionValue(String name) throws JspTagException {
			return getElementValue(this.getCurrentTrivia(),name);
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		try {
			return getIndexElementValuesSize(this.getCurrentTrivia(),name);
		} catch (JspTagException e) {
			// TODO Auto-generated catch block
			return 0;
		}
	}

	public String getCollectionIndexValue(String name, int index) throws JspTagException {
		return getIndexElementValues(this.getCurrentTrivia(),name,index);
	}

	public String getCollectionPathName() throws JspTagException {
		
	    Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag " + this.getClass().getName() + " in wrong context.");
	    }

	    I_TfsCollectionListTag parentCollection = (I_TfsCollectionListTag) ancestor;
	    String pathName = parentCollection.getCollectionPathName();
	    
	    if (!pathName.equals("") && !keyName.equals(""))
	    	pathName += "/";
	    
	    if (!keyName.equals(""))
	    	pathName += keyName + "[1]";
	    		
	    return pathName;
	}
	
	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}


}
