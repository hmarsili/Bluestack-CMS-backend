package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsDireccionTag extends A_TfsNoticiaValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7267854860034405221L;
	
	
	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.event.address")); //precio
        
        printContent(content);

        return SKIP_BODY;
    }
}
