package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsAudioAuthor extends A_TfsNoticiaCollectionValue{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 619860070347763353L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.author"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //video

	     printContent(content);

		 return SKIP_BODY;
	 }

}
