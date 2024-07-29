package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsFotoAutorTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 4626162409482454327L;

	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.authors.photo"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.authors.photo")); 
	        
	     printContent(content);

		 return SKIP_BODY;
	 }

	
}
