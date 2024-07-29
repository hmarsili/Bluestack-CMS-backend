 
package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsIngredientTag extends  A_TfsNoticiaCollectionWithBlanks {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
    public int doStartTag() throws JspException {

		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.ingrediente.cantidad"); //"cantidad";

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.ingrediente")); //ingediente

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
