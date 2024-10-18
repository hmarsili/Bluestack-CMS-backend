package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;


public class TfsPeopleListTag extends A_TfsNoticiaSplitElement   {    
	/**
	 * 
	 */
	private static final long serialVersionUID = 638459188800733966L;

	public int doStartTag() throws JspException {
		
		items = null;
	    idx=-1;
	    
		I_TfsNoticia noticia = getCurrentNews();
	        
	    String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.people")); //personas
	    
	    if (content.trim().equals(""))
	    	return SKIP_BODY;
	    
	    items = content.split(separator);
	    
	    return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	    
	}
	
	@Override
	public int doAfterBody() throws JspException {

		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}
	

}
