
package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.JspException;

import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsIncludeContentUtil;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsNoticiasTriviaTag extends A_TfsNoticiaValue{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8687704179001473387L;
	
	String style = "default";
	
	@Override
    public int doStartTag() throws JspException {

		I_TfsNoticia noticia = getCurrentNews();
        
        String triviaURL = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.trivia"));
        
        if(triviaURL!=null)
        	showTrivia(triviaURL);

        return SKIP_BODY;
    }
	
	
	protected void includeBoxTrivia(String urlResource) throws JspException {
		
		TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);
	
		includeContent.setParameterToRequest("path",urlResource);
		includeContent.setParameterToRequest("style",style);
		
		includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/trivias/trivia.jsp");
	}
	
	protected void showTrivia(String urlResource) throws JspException {
		
		TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);
	
		String boxDivId = "triviaBox_" + new Date().getTime();
	
		try {
			pageContext.getOut().print("<div  id=\"" + boxDivId + "\" path=\"" + urlResource + "\">");
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		includeContent.setParameterToRequest("path",urlResource);
		includeContent.setParameterToRequest("id",boxDivId);
		includeContent.setParameterToRequest("style",style);
		
		try {
			includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/trivias/" + style + "/triviaView.jsp");
			 
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	
		try {
			pageContext.getOut().print("</div>");
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	

}


