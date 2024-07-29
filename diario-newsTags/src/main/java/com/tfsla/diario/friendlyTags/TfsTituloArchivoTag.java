package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTituloArchivoTag extends A_TfsNoticiaCollectionValue {

	 @Override
	 public int doStartTag() throws JspException {


		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.files.title"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());
		 
		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.files.title")); //titulo
		 pageContext.getRequest().setAttribute("filetitle", content);   
	     printContent(content);

		 return SKIP_BODY;
	 }

}
