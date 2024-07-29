package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTituloPersonTag extends A_TfsNoticiaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 15456337789867231L;
	
	@Override
	 public int doStartTag() throws JspException {
		 
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.people.name"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());
	     
	     printContent(content);

		 return SKIP_BODY;
	 }

}
