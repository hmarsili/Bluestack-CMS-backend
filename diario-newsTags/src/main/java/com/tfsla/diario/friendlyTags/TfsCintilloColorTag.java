package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsCintilloColorTag extends A_TfsNoticiaValue {

    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.text.label.color")); //color-cintillo
        
        printContent(content);

        return SKIP_BODY;
    }

}