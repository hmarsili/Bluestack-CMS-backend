package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class A_TfsMessagesValueTag extends TagSupport{
	
	private static final long serialVersionUID = 844176355897667749L;

	protected static final Log LOG = CmsLog.getLog(A_TfsMessagesValueTag.class);
	

	public A_TfsMessagesValueTag() {
		super();
	}
	
	protected I_TfsMessages getMessages() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsMessages.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Messages not accesible");
	    }
	    
	    I_TfsMessages messages = (I_TfsMessages) ancestor;
		return messages;
	}
	
	protected void printContent(String content) throws JspException {
		try {
	            pageContext.getOut().print(content);
	    } catch (IOException e) {
	        if (LOG.isErrorEnabled()) {
	            LOG.error("Error trying to retrieve Message", e);
	        }
	        throw new JspException(e);
	    }
	}
}
