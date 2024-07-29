package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsNoticasVodTag extends A_TfsNoticiaCollectionWithBlanks  {

	@Override
	public int doStartTag() throws JspException {

		keyControlName = "";

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.vod")); //videoOnDemand

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

