package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsLocalidadTag extends A_TfsNoticiaValue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.event.locality")); 
        
        printContent(content);

        return SKIP_BODY;
    }

}
