package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsKeyWordsAudioTag extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 5383795907705255171L;

	@Override
	 public int doStartTag() throws JspException {

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.video.keywords")); //keywords
		 
		 I_TfsCollectionListTag collection = getCurrentCollection();
	     setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.keywords"));

	     String content = collection.getCollectionValue(getCollectionPathName()); //keywords
  
	     printContent(content);

		 return SKIP_BODY;
	 }

	
}
