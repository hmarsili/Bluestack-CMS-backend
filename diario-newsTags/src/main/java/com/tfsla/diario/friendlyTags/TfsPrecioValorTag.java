package com.tfsla.diario.friendlyTags;
import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;


public class TfsPrecioValorTag extends A_TfsNoticiaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.price.value"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); 

	     printContent(content);

		 return SKIP_BODY;
	 }
}
