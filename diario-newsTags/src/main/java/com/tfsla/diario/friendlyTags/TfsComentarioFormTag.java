package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagLink;

import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.diario.utils.TfsHashGenerator;
import com.tfsla.diario.utils.TfsIncludeContentUtil;

public class TfsComentarioFormTag extends BodyTagSupport implements I_CaptchaContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 349870344964960350L;

	private String style = "default";
	private String url = null;
	private String parentid = "0";
	private String lazyload = "false";

	protected CmsObject cms = null;
	
	String formDivId = "";
	String formName = "";
	

	@Override
	public int doStartTag() throws JspException {
		
    	cms = CmsFlexController.getController(pageContext.getRequest()).getCmsObject();

		String currentUri =  cms.getRequestContext().getUri();
		String commentUrl = url;
		
		if (commentUrl==null || commentUrl.trim().equals(""))
			commentUrl = getContextUrl(cms);
		
		if (commentUrl==null || commentUrl.trim().equals(""))		
			commentUrl = currentUri;
		
		
		currentUri = CmsJspTagLink.linkTagAction(currentUri,pageContext.getRequest());

		formDivId = TfsHashGenerator.getHash(commentUrl + parentid);

		formName = "frm_" + formDivId;
		formDivId = "cmt_form_" + formDivId;
		
		String hidden = "";
		if (parentid!="0")
			hidden = "style=\"display:none\"";

		//Determino si tengo que redireccionar a la pagina de login.
		CmsUser user = cms.getRequestContext().currentUser();
		boolean showRedictLogin = user.isGuestUser() && !CommentsModule.getInstance(cms).getGuestComments().booleanValue();

		//Obtengo si tengo que mostrar el texto del comentario nuevamente en el textarea
		String txt = (String)pageContext.getRequest().getAttribute("textoComentario");
		Boolean submitted = (Boolean) pageContext.getRequest().getAttribute("commentSubmitted");		
		if (submitted==null)
			submitted = false;
		String submittedPId = pageContext.getRequest().getParameter("parentid");
		if (submitted || txt==null || (!submittedPId.equals(parentid)))
			txt = "";

		//Obtengo si tengo que enviar el texto del comentario enviado.
		String commentSubmittedText = "";
		if (parentid.equals("0")) {
			if (submitted)
				commentSubmittedText = "Su comentario ha sido enviado";
		}
		
		//Obtengo si hubo algun error en el registro del comentario
		String commentErrorText = "";
		if (parentid.equals("0")) {
			Boolean processError = (Boolean) pageContext.getRequest().getAttribute("processError");
			if (processError!=null && processError==true)
				commentErrorText = (String)pageContext.getRequest().getAttribute("resultMsg");
		}
		
		try {
			
			pageContext.getOut().print("<div class=\"formComment\" id=\""+ formDivId + "\" " + hidden + ">");
		
			TfsIncludeContentUtil includeForm = new TfsIncludeContentUtil(pageContext);

			includeForm.setParameterToRequest("style", style);
			includeForm.setParameterToRequest("lazyload", lazyload);

			includeForm.setParameterToRequest("commentUrl", commentUrl);
			includeForm.setParameterToRequest("currentUri", currentUri);
			includeForm.setParameterToRequest("formName",formName);
			includeForm.setParameterToRequest("commentText", txt);
			includeForm.setParameterToRequest("commentSubmittedText", commentSubmittedText);
			includeForm.setParameterToRequest("commentErrorText", commentErrorText);
			includeForm.setParameterToRequest("parentid", parentid);
			includeForm.setParameterToRequest("redirectLogin", "" + showRedictLogin);
			includeForm.setParameterToRequest("showCaptcha", "" + showCaptcha());
			includeForm.setParameterToRequest("formDivId", formDivId);
			includeForm.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/comments/" + style + "/commentForm.jsp");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {

		try {					
			pageContext.getOut().print("</div>");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return super.doEndTag();
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getContextUrl(CmsObject cms)
	{
		I_TfsNoticia noticia = getCurrentNews();
		if (noticia!=null)
			return cms.getSitePath(noticia.getXmlDocument().getFile());

		return null;
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

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public boolean showCaptcha() {
		return CommentsModule.getInstance(cms).getUseCapcha();
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	
    public String getLazyload() {
		return lazyload;
	}

	public void setLazyload(String lazyload) {
		this.lazyload = lazyload;
	}

}
