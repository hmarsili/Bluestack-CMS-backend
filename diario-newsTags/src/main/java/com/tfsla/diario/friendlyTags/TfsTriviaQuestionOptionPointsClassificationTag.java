package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTriviaQuestionOptionPointsClassificationTag extends A_TfsTriviaCollectionValue{

	private static final long serialVersionUID = -8133006426757076995L;
	
	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionTrivia();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.option.points.classification"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());
	        
	     printContent(content);

		 return SKIP_BODY;
	 }
	
}
