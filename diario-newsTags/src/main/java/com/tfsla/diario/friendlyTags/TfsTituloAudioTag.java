package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTituloAudioTag extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -4021308108018061467L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.title"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());  
	     
	     printContent(content);

		 return SKIP_BODY;
	 }

}
