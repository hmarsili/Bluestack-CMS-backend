package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsOrdenTag  extends A_TfsNoticiaValue {

	
	
    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.vod.order")); // order
       
        printContent(content);

        return SKIP_BODY;
    }
    
	

}