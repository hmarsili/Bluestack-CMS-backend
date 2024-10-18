package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsCodigoVideoTag  extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")); //codigo
	     
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());


	     printContent(content);

		 return SKIP_BODY;
	 }

	
}
