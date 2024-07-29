package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.comentarios.services.CommentsModule;

public class TfsComentariosNavegadorTag  extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2361629509712092700L;

	String id = null;
	String style = "default";
	String path = null;
	String withMoreAnswers = null;
	String minAnswers = null;
	
	int page = 1;
	
	@Override
    public int doStartTag() throws JspException {

		CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    String urlResource = path;
	    if (urlResource==null || urlResource.equals(""))
	    	urlResource = getContextUrl(cms);
	    
	    if (urlResource==null || urlResource.equals(""))
	    	urlResource = getRelativeUrl(cms);
	    
	    int commentsCount = CommentsModule.getInstance(cms).getCommentsCountByParent(cms, urlResource,0);

	    if(withMoreAnswers!=null){
			if(withMoreAnswers.equals("true")){
				int min_Answers = -1;
				
				if(minAnswers!=null && minAnswers!="")
					min_Answers = Integer.parseInt(minAnswers);
				
				commentsCount = CommentsModule.getInstance(cms).getCommentsWhitMoreAnswersCount(cms, urlResource, min_Answers);
			}
		}
		
		int pagesCount = CommentsModule.getInstance(cms).getPagesCount(commentsCount);
		int commentPerPage = CommentsModule.getInstance(cms).getQueryPageSize();
		
		if( page > 1 ){
			int prevCount = (page==2 ? CommentsModule.getInstance(cms).getFirstPageSize() : commentPerPage);
			String text =
					"<a title=\"Pasados " + prevCount + "\" href=\"javascript:showComments('" + id + "','" + path + "'," + (page-1) + ",'" + style + "', '"+ withMoreAnswers+ "','"+ minAnswers + "');\" class=\"btn-Gris clearfix\">" +
						"<span class=\"cl\"></span>" +
						"<span class=\"cm\">Pasados " + prevCount + " <span></span></span>" +
						"<span class=\"cr\"></span>" +
					"</a>";
			
			
			try {
				pageContext.getOut().print(text);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if( page < pagesCount ){
			
			String text =
					"<a title=\"Próximos " + commentPerPage + "\" href=\"javascript:showComments('" + id + "','" + path + "'," + (page+1) + ",'" + style + "', '"+ withMoreAnswers+ "','"+ minAnswers + "');\" class=\"btn-Gris clearfix\">" +
						"<span class=\"cl\"></span>" +
						"<span class=\"cm\">Próximos " + commentPerPage + " <span></span></span>" +
						"<span class=\"cr\"></span>" +
					"</a>";
			
			
			try {
				pageContext.getOut().print(text);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		
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


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
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
