package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTriviaStatisticUserQuantityTag extends A_TfsTriviaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 725407952091036013L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("trivia.cantUsers"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); 

		 if (content != null)
			 printContent(content);
		 else {
			 printContent("");
		 }  
		 return SKIP_BODY;
	 }
}


