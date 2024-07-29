package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsKeyWordsListTag extends A_TfsNoticiaSplitElement   {

	public int doStartTag() throws JspException {
		items = null;
	    idx = -1;
	    
		I_TfsNoticia noticia = getCurrentNews();
	    String content = getElementValue(noticia, TfsXmlContentNameProvider.getInstance().getTagName("news.keywords"));
	    
	    if (content.trim().equals("")) {
	    	return SKIP_BODY;
	    }
	    
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
	
	private static final long serialVersionUID = -8510042380535549718L;
}