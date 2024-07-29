package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsAudioRated extends A_TfsNoticiaCollectionValue{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5526046404378242730L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.rated"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //audio

	     printContent(content);

		 return SKIP_BODY;
	 }
}
