package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.friendlyTags.I_TfsUser;
import com.tfsla.trivias.model.TfsTrivia;

public class A_TfsTriviaUserValueTag extends TagSupport {

	private static final long serialVersionUID = 5556958960967041371L;
	
	protected static final Log LOG = CmsLog.getLog(A_TfsTriviaUserValueTag.class);
	
	protected TfsTrivia getCurrentTrivia() throws JspTagException {
		
	    Tag ancestor = findAncestorWithClass(this, I_TfsTriviaUser.class);
		
	    if (ancestor == null) {
	        throw new JspTagException("Tag Trivia not accesible");
	    }
	
	    I_TfsTriviaUser triviaAncestor = (I_TfsTriviaUser) ancestor;
	    
	    TfsTrivia  trivia = triviaAncestor.getTrivia();
	    
		return trivia;
	}
	
	protected I_TfsUser getCurrentUser() throws JspTagException {
		
	    Tag ancestor = findAncestorWithClass(this, I_TfsUser.class);
	    
	    if (ancestor == null) {
	        throw new JspTagException("Tag User not accesible");
	    }
	    
	    I_TfsUser usuario = (I_TfsUser) ancestor;
		return usuario;
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
