package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsLugarTag extends A_TfsNoticiaValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4703196902143339336L;

	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.event.place")); //precio
        
        printContent(content);

        return SKIP_BODY;
    }
}
