package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.model.TfsVideoFormat;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideoFormatName extends A_TfsNoticiaCollectionValue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4374181875566541737L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.video.format"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //video

	     printContent(content);

		 return SKIP_BODY;
	 }

}
