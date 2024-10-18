package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsExternalNewsTituloTag extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -2669818885180957800L;

	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.relatedexternalnews.title"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.relatedexternalnews.title"));
	        
	     printContent(content);

		 return SKIP_BODY;
	 }

}
