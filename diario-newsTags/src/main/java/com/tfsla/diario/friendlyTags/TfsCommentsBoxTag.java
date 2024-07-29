package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.utils.TfsIncludeContentUtil;

public class TfsCommentsBoxTag  extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4579664342419035939L;
	
	String style = "default";
	String path = null;
	String withMoreAnswers;
	String minAnswers;
    
	@Override
    public int doStartTag() throws JspException {
    	//try {
    		


			TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);

		    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		    String urlResource = path;
		    if (urlResource==null || urlResource.equals(""))
		    	urlResource = getContextUrl(cms);
		    
		    if (urlResource==null || urlResource.equals(""))
		    	urlResource = getRelativeUrl(cms);


			String boxDivId = "cmtBox_" + new Date().getTime();

			try {
				pageContext.getOut().print("<div  id=\"" + boxDivId + "\">");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			includeContent.setParameterToRequest("path",urlResource);
			includeContent.setParameterToRequest("id",boxDivId);
			includeContent.setParameterToRequest("style",style);
			includeContent.setParameterToRequest("page","1");
			includeContent.setParameterToRequest("withMoreAnswers",withMoreAnswers);
			includeContent.setParameterToRequest("minAnswers",minAnswers);
			
				
			includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/comments/" + style + "/commentList.jsp");


			try {
				pageContext.getOut().print("</div>");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		//} catch (IOException e) {
		//	throw new JspException(e);
		//}
		
		return SKIP_BODY;
    }

	public String getContextUrl(CmsObject cms)
	{
		I_TfsNoticia noticia = getCurrentNews();
		if (noticia!=null)
			return cms.getSitePath(noticia.getXmlDocument().getFile());

		return null;
	}
	
	public String getRelativeUrl(CmsObject cms)
	{
		return cms.getRequestContext().getUri();
	}

	protected I_TfsNoticia getCurrentNews() {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsNoticia.class);
	    if (ancestor == null) {
	        return null;
	    }
	
	    I_TfsNoticia noticia = (I_TfsNoticia) ancestor;
		return noticia;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getMinAnswers() {
		return minAnswers;
	}

	public void setMinAnswers(String minAnswers) {
		this.minAnswers = minAnswers;
	}
	
	public String getWithMoreAnswers() {
		return withMoreAnswers;
	}

	public void setWithMoreAnswers(String withMoreAnswers) {
		this.withMoreAnswers = withMoreAnswers;
	}

}
