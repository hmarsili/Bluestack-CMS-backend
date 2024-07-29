package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class TfsTriviaKeywordsItemTag extends TagSupport{

	private static final long serialVersionUID = -8133006426757076995L;
	protected static final Log LOG = CmsLog.getLog(TfsTriviaKeywordsItemTag.class);

	@Override
    public int doStartTag() throws JspException {
		 Tag ancestor = findAncestorWithClass(this, TfsTriviaKeyWordsListTag.class);
		    if (ancestor == null) {
		        throw new JspTagException("Tag not accesible");
		    }
		
		    TfsTriviaKeyWordsListTag tagList = (TfsTriviaKeyWordsListTag) ancestor;
		    
		    printContent(tagList.getCurrentItem());
		    
		    return SKIP_BODY;
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

	
}
