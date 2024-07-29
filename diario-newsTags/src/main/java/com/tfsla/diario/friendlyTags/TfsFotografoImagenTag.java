package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsFotografoImagenTag extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {

		 
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.author"));
		 String content = collection.getCollectionValue(getCollectionPathName()); //fotografo
		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), "fotografo");
	     printContent(content);

		 return SKIP_BODY;
	 }
	
}
