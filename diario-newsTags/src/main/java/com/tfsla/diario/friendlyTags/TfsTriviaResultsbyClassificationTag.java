package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTriviaResultsbyClassificationTag  extends A_TfsTriviaCollection  {
	
	private static final long serialVersionUID = -5379220139840466427L;

	protected static final Log LOG = CmsLog.getLog(TfsTriviaResultsbyClassificationTag.class);
	
	@Override
	public int doStartTag() throws JspException {

		init(TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultsbyclassification")); 
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
