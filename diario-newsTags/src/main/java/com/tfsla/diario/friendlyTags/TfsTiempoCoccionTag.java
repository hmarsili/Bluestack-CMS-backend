package com.tfsla.diario.friendlyTags;


import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsTagsUtil;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class  TfsTiempoCoccionTag extends A_TfsNoticiaValue {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String separator = " "; 
	private String format = "";
	
	@Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.tiempoCoccion")); 
        
        if (content!= null) {
        	String[] splitContent = content.split("-");
        	Integer quantity =0;
        	String unity = "";
        	if (splitContent.length == 2) {
        		try {
        			quantity= Integer.valueOf(splitContent[0]);
        		} catch (NumberFormatException ex) {
        			// toma el valor en 0 si no puede parsear los tiempos
        		}
        		unity = splitContent[1];
        	}
        	if (format.equals("long")) {
        		content = String.valueOf(TfsTagsUtil.getTransformationTime(unity, quantity));
        	} else if (format.equals(""))
        		content = content.replace("-", separator);
        } else {
        	content = "";
        }
        
        printContent(content);
        return SKIP_BODY;

    }
	
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}

