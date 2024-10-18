package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsHiddenKeywordsTag extends A_TfsNoticiaValue {

    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.hiddenkeywords")); //claves
        if (content==null)
        	content="";
        
        printContent(content);

        return SKIP_BODY;
    }

}
