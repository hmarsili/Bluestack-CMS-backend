package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.tfsla.diario.utils.TfsIncludeContentUtil;

public class TfsComentariosInitTag extends TagSupport {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5384058235736282696L;

	@Override
    public int doStartTag() throws JspException {
    	
		TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);

		includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/comments/addComment.jsp");
		includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/comments/reportAbuse.jsp");
		
		return SKIP_BODY;

    }
}
