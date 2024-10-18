package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsCategoriasVideoTag extends A_TfsNoticiaCollection  {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5702211669875183678L;

	@Override
	public int doStartTag() throws JspException {

	
		init(TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories")); //Categorias

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
}
