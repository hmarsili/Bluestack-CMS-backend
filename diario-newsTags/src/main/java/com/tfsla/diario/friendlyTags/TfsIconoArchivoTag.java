package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsIconoArchivoTag extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {


		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.files.icon"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());
		 
		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.files.title")); //titulo
		 pageContext.getRequest().setAttribute("fileicon", content);  
	     printContent(content);

		 return SKIP_BODY;
	 }

}
