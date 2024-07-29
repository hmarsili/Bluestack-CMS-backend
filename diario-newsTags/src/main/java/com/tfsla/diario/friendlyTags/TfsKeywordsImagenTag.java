package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsKeywordsImagenTag extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), "keywords");
	     
		 I_TfsCollectionListTag collection = getCurrentCollection();
	     setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.keywords"));

	     String content = collection.getCollectionValue(getCollectionPathName()); //keywords
		 
	     printContent(content);

		 return SKIP_BODY;
	 }
}
