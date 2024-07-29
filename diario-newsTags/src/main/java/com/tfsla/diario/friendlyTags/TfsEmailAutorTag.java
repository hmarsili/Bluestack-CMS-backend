package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsEmailAutorTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 6632678283962807130L;

	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.authors.email"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.authors.email")); //email
	        
	     printContent(content);

		 return SKIP_BODY;
	 }

	
}
