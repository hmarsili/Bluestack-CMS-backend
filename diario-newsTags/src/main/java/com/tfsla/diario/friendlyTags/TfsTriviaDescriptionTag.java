package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTriviaDescriptionTag extends A_TfsTriviaValueTag {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	 public int doStartTag() throws JspException {

	     I_TfsTrivia trivia = getCurrentTrivia();
	     
		 String content = getElementValue(trivia,TfsXmlContentNameProvider.getInstance().getTagName("trivia.description"));

	     printContent(content);

		 return SKIP_BODY;
	 }
}
