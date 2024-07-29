package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideoBitrate extends A_TfsNoticiaCollectionValue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8230532355472477891L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.video.bitrate"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //video

	     printContent(content);

		 return SKIP_BODY;
	 }

}
