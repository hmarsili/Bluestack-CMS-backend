package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTriviaUrlTag extends A_TfsTriviaValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3553971086811376306L;

	@Override
    public int doStartTag() throws JspException {
		
		 CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
		
		 I_TfsTrivia trivia = getCurrentTrivia();
		 
		 String content = cms.getSitePath(trivia.getXmlDocument().getFile());
		
         printContent(content);

		 return SKIP_BODY;
    }
}