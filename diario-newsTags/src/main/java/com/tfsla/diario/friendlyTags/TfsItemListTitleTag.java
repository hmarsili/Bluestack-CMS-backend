package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsItemListTitleTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -6306131189117954470L;

	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.title"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());
	        
	     printContent(content);

		 return SKIP_BODY;
	 }

}
