package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

public class TfsGenericNewsValueTag extends A_TfsNoticiaValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2624879418341413834L;

	private String element = "";

	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,element);
        
        if (content!=null)
        	printContent(content);

        return SKIP_BODY;
    }
	
	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}
	
}
