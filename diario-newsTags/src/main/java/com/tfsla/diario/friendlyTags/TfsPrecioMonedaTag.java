package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPrecioMonedaTag extends A_TfsNoticiaCollectionValue {
	
	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.price.currency"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); 

	     printContent(content);

		 return SKIP_BODY;
	 }

}
