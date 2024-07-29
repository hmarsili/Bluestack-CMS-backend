package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideoRated extends A_TfsNoticiaCollectionValue{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5526046404378242730L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.video.rated"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //video

	     printContent(content);

		 return SKIP_BODY;
	 }
}
