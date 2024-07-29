package com.tfsla.diario.friendlyTags;


import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsDificultadTag extends A_TfsNoticiaValue {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.dificultad")); 
        
        printContent(content);

        return SKIP_BODY;
    }
}

