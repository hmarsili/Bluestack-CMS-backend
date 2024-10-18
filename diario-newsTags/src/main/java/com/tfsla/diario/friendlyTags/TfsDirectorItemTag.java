
package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class TfsDirectorItemTag extends TagSupport {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3612858321413275905L;
	protected static final Log LOG = CmsLog.getLog(TfsDirectorItemTag.class);

	@Override
    public int doStartTag() throws JspException {
		 	Tag ancestor = findAncestorWithClass(this, TfsDirectoresListTag.class);
		    if (ancestor == null) {
		        throw new JspTagException("Tag diretorsList not accesible");
		    }
		
		    TfsDirectoresListTag tagList = (TfsDirectoresListTag) ancestor;
		    pageContext.getRequest().setAttribute("directoritem", tagList.getCurrentItem()!=null?tagList.getCurrentItem():""); 
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
