package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsAudioVfsPath extends A_TfsNoticiaCollectionValue  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2353710821112394166L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.vfspath"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //audio

	     printContent(content);

		 return SKIP_BODY;
	 }

}
