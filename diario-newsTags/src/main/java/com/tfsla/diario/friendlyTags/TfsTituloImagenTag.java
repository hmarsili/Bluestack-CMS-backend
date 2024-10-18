package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTituloImagenTag extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {

	     setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.title"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //titulo

	     printContent(content);

		 return SKIP_BODY;
	 }
}
