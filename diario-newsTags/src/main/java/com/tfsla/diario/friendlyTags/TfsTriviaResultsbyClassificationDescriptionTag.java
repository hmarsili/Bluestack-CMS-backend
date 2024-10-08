package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTriviaResultsbyClassificationDescriptionTag  extends A_TfsTriviaCollectionValue {

	private static final long serialVersionUID = -6306131189117954470L;

	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionTrivia();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultsbyclassification.description"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());
	        
	     printContent(content);

		 return SKIP_BODY;
	 }

}
