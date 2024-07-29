package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsGeoLocationTag  extends A_TfsNoticiaValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5332923931709992000L;
	

	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.event.geoLocation")); 
        
        printContent(content);

        return SKIP_BODY;
    }
}
