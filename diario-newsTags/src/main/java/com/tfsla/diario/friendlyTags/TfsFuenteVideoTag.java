package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsFuenteVideoTag extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 7181292420827682909L;

	@Override
	 public int doStartTag() throws JspException {

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.video.source")); //fuente

		 I_TfsCollectionListTag collection = getCurrentCollection();
	     setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"));

	     String content = collection.getCollectionValue(getCollectionPathName()); //keywords

	     printContent(content);

		 return SKIP_BODY;
	 }
	
}
