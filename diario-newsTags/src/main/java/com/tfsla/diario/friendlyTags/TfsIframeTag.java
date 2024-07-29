package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsIframeTag extends A_TfsNoticiaValue {

    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.iframe")); //iframe
        
        if (content!=null)
        	printContent(content);

        return SKIP_BODY;
    }

	//
}
