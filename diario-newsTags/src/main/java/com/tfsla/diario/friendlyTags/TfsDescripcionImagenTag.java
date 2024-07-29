package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsDescripcionImagenTag extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), "descripcion");
	     setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.description"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //descripcion

	     printContent(content);

		 return SKIP_BODY;
	 }
}
