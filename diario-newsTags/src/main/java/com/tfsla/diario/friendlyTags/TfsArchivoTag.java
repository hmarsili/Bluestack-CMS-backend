package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsArchivoTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -6306131189117954470L;

	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.files.filepath"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());
		 pageContext.getRequest().setAttribute("filepath", content);

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.files.filepath")); //archivo
	        
	     printContent(content);

		 return SKIP_BODY;
	 }

}
