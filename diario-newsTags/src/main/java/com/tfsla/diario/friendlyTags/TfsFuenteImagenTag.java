package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsFuenteImagenTag extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollection();
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.source"));

		 String content = collection.getCollectionValue(getCollectionPathName()); //fuente

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), "fuente");
	        
	     printContent(content);

		 return SKIP_BODY;
	 }
}
