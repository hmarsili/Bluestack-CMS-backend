package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsIngredientQuantityTag extends A_TfsNoticiaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String separator = "  ";
	 

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.ingrediente.cantidad"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());
		 
		  
		 if (content != null) 
			content = content.replace("-", separator);
		 else
			 content = "";
	     printContent(content);

		 return SKIP_BODY;
	 }


	public String getSeparator() {
		return separator;
	}


	public void setSeparator(String separator) {
		this.separator = separator;
	}
}
