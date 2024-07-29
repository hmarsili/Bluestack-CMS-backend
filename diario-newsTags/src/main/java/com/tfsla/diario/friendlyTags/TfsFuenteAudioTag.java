package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsFuenteAudioTag extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 3878282200251875437L;

	@Override
	 public int doStartTag() throws JspException {

	     setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.publisher"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());  
	     
	     printContent(content);

		 return SKIP_BODY;
	 }


}
