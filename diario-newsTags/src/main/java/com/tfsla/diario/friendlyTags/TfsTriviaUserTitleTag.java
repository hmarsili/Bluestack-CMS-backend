package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.trivias.model.TfsTrivia;


public class TfsTriviaUserTitleTag extends A_TfsTriviaUserValueTag{

	private static final long serialVersionUID = -6679836842427584583L;
	
	@Override
    public int doStartTag() throws JspException {
		
		 TfsTrivia trivia = getCurrentTrivia();
		 
		 String content = trivia.getTitle();
		 
         printContent(content);

		 return SKIP_BODY;
    }


}
