package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import java.io.*;
import java.util.Date;
public class TfsCommentsTag extends BodyTagSupport {

	private int size=10;
	protected long commentIdx = 1;
	
	public int doStartTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        String path = cms.getSitePath(noticia.getXmlDocument().getFile());

        commentIdx = new Date().getTime();
        
		try {
		pageContext.getOut().print("<div type=\"comments-list\" page=\"1\" size=\"" + size + "\" path=\"" + path + "\" id=\"" + commentIdx + "\"></div>");
		return EVAL_BODY_BUFFERED;
		} catch(IOException ioe) {
		throw new JspException(ioe.getMessage());
		}
		}


		public int doEndTag() throws JspException {

		try {  
		// Write from bodyContent writer to original writer.
		
		//pageContext.getOut().print(bodyContent.getString());
		
		String fragmentCode = bodyContent.getString();
		
		pageContext.getOut().print("<input type=\"hidden\" id=\"" + commentIdx + "\" value=\"" + fragmentCode + "\" name=\"commentCode\"/>");
		
		
		//// For large buffers, the following code may be more
		//// efficient than the previous line.
		//// Get the original (enclosing) writer:
		// JspWriter jOut = bodyContent.getEnclosingWriter();
		//// Append body output with previous writer:
		//bodyContent.writeOut(jOut);

		// Now we're back to the original writer.

		return EVAL_PAGE;
		} catch(IOException ioe) {
		throw new JspException(ioe.getMessage());
		} 
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

		
		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

	}

