package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTituloVideoTag extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -7215389837549208420L;

	@Override
	 public int doStartTag() throws JspException {

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.video.title")); //titulo

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //descripcion
	     
	     printContent(content);

		 return SKIP_BODY;
	 }

	
}
