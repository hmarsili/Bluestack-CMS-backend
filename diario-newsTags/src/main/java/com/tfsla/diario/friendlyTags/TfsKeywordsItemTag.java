package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class TfsKeywordsItemTag extends TagSupport{

    protected static final Log LOG = CmsLog.getLog(TfsKeywordsItemTag.class);

	@Override
    public int doStartTag() throws JspException {
		 Tag ancestor = findAncestorWithClass(this, TfsKeyWordsListTag.class);
		    if (ancestor == null) {
		        throw new JspTagException("Tag News not accesible");
		    }
		
		    TfsKeyWordsListTag tagList = (TfsKeyWordsListTag) ancestor;
		    
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
