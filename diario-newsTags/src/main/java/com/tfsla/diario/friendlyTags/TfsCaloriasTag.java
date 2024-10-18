package com.tfsla.diario.friendlyTags;


import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class  TfsCaloriasTag extends A_TfsNoticiaValue {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.calorias")); 
        
        printContent(content);

        return SKIP_BODY;
    }
}

