package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class A_TfsTriviaCollectionValue extends BodyTagSupport {
	
	private static final long serialVersionUID = 6513835503429246475L;
	protected String keyName = "";
    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(A_TfsTriviaCollectionValue.class);

    protected I_TfsTrivia getCurrentTrivia() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsTrivia.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Trivia not accesible");
	    }
	
	    I_TfsTrivia trivia = (I_TfsTrivia) ancestor;
		return trivia;
	}
    
	protected I_TfsCollectionListTag getCurrentCollectionTrivia() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Trivia not accesible");
	    }
	
	    LOG.debug("A_TfsTriviaCollectionValue - getCurrentCollectionTrivia -->" + ancestor.getClass().getName());
	    A_TfsTriviaCollection trivia = (A_TfsTriviaCollection) ancestor;
		return trivia;
	}
	
	protected I_TfsCollectionListTag getCurrentCollection() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag with Collection not accesible");
	    }
	
	    I_TfsCollectionListTag collection = (I_TfsCollectionListTag) ancestor;
		return collection;
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

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

}
