package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

public class TfsNombreCategoriaTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -3208687889814837790L;

	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName("");
		 
		 String content = collection.getCollectionValue(getCollectionPathName());

	     printContent(content);

		 return SKIP_BODY;
	 }

}
