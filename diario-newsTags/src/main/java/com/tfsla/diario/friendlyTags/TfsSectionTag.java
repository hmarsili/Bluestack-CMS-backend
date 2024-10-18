package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;
//import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsSectionTag  extends A_TfsNoticiaCollectionValue {
//public class TfsSectionTag extends A_TfsNoticiaValue {

    @Override
    public int doStartTag() throws JspException {

        //I_TfsNoticia noticia = getCurrentNews();
        
        //String content = getPropertyValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.section"),false); //seccion
        
		 I_TfsCollectionListTag collection = getCurrentCollection();

		 String content = collection.getCollectionValue(TfsXmlContentNameProvider.getInstance().getTagName("news.section")); //seccion

        printContent(content);

        return SKIP_BODY;
    }

}
