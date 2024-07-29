package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsNombreFuentesTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -8229008857217853306L;

	@Override
	 public int doStartTag() throws JspException {

		// I_TfsCollectionListTag collection = getCurrentCollection();
		 //String content = collection.getCollectionValue(TfsXmlContentNameProvider.getInstance().getTagName("news.sources.name")); //titulo
		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.sources.name"));
		 	 
		 String content = collection.getCollectionValue(getCollectionPathName());
		 pageContext.getRequest().setAttribute("sourcename", content);
	     printContent(content);

		 return SKIP_BODY;
	 }

}