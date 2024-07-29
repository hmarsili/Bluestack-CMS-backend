package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.model.TfsVideoFormat;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideoFormatPath extends A_TfsNoticiaCollectionValue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4252740179164023075L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());  

	     printContent(content);
	     
		return SKIP_BODY;
	 }

}
