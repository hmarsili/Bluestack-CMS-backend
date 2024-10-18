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

public abstract class A_TfsNoticiaValue extends TagSupport implements I_TfsCollectionListTag {

	protected String keyName = "";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(A_TfsNoticiaValue.class);

    protected CmsObject cms = null;
    
	protected String getPropertyValue(I_TfsNoticia noticia, String propertyName, boolean search) {
	    // get the current users OpenCms context
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    CmsProperty property=null;
		try {
			property = cms.readPropertyObject(noticia.getXmlDocument().getFile(), propertyName, search);
		} catch (CmsException e) {
			LOG.error("Error reading property " + propertyName,e);
		}
	    
	    if (property!=null)
	    	return property.getValue("");
	    
	    return "";
	}

	protected String getElementValue(I_TfsNoticia noticia, String elementName) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, noticia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName,e);
		}
	
		return "";
	}
	
	
	public String getIndexElementValues(I_TfsNoticia noticia, String elementName,int index) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
	    try {
			return xmlContent.getStringValue(cms, elementName, noticia.getXmlDocumentLocale(),index);
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName,e);
		}
	
		return "";
	}

	public int getIndexElementValuesSize(I_TfsNoticia noticia, String elementName) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
		return xmlContent.getValues(elementName, noticia.getXmlDocumentLocale()).size();
	}

	

	protected I_TfsNoticia getCurrentNews() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsNoticia.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag News not accesible");
	    }
	
	    I_TfsNoticia noticia = (I_TfsNoticia) ancestor;
		return noticia;
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
			return getElementValue(this.getCurrentNews(),name);
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		try {
			return getIndexElementValuesSize(this.getCurrentNews(),name);
		} catch (JspTagException e) {
			// TODO Auto-generated catch block
			return 0;
		}
	}

	public String getCollectionIndexValue(String name, int index) throws JspTagException {
		return getIndexElementValues(this.getCurrentNews(),name,index);
	}

	public String getCollectionPathName() throws JspTagException {
		// get a reference to the parent "content container" class
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
