package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import org.opencms.main.OpenCms;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsFuentesTag extends A_TfsNoticiaCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1300588453488508580L;

	@Override
	public int doStartTag() throws JspException {

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.sources")); //Fuentes

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