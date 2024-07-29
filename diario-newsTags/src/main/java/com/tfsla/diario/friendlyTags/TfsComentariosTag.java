package com.tfsla.diario.friendlyTags;

import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.comentarios.data.CommentPersistor;
import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.diario.model.TfsComentario;

public class TfsComentariosTag extends BodyTagSupport implements I_TfsComentario,I_TfsCollectionListTag {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2352120960757099333L;

	/** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(A_TfsNoticiaValue.class);

    protected TfsComentario previousComentario = null;
    
	protected int index=0;
	protected int lastElement=0;

	protected List<Comment> comments=null;
	
	protected String page;
	protected String url;
	protected String parentid;
	protected String withMoreAnswers;
	protected String minAnswers;

	protected String fromcommentid;
	protected String tocommentid;	
	protected String paginate;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	protected boolean hasMoreContent() {
		index++;

		if (index<lastElement)
			exposeComentario(getComment());
		else
			restoreComentario();

		return (index<lastElement);
	}

	public boolean isLast() {
		return (index==lastElement-1);
	}

	@Override
	public int doStartTag() throws JspException {
		
		findComments();
		
		if (index<lastElement) {
			exposeComentario(getComment());
		
			return EVAL_BODY_INCLUDE;
		}
		return SKIP_BODY;
		
		//return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );		

	}

	@Override
	public int doAfterBody() throws JspException {

		if (index==lastElement)
			restoreComentario();

		index++;
		
		if (index<lastElement) {
			exposeComentario(getComment());
			return EVAL_BODY_AGAIN;
		}
		
//		if (hasMoreContent()) {
//			return EVAL_BODY_AGAIN;
//		}

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}


	protected void findComments()
	{
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    saveComentario();
	    
	    index=0;
	    
	    if (page==null || page.equals(""))
	    	page="1";
	    
	    
	    String urlResource = url;
	    if (urlResource==null || urlResource.equals(""))
	    	urlResource = getContextUrl(cms);
	    
	    if (urlResource==null || urlResource.equals(""))
	    	urlResource = getRelativeUrl(cms);
	    
        String m_news = CommentsModule.getInstance(cms).getMasterNews(cms,urlResource);
		
		if(m_news!=null && m_news != "" ) urlResource = m_news;
		

		if (parentid==null || parentid.equals(""))
			parentid = "0";

		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getTitle();
		siteName = siteName.replaceFirst("/sites/", "");
        siteName = siteName.replaceFirst("/site/", "");        
        siteName = siteName.substring(0,siteName.length() -1);

		CommentPersistor cPer = new CommentPersistor(CommentsModule.getInstance(cms));
		if (fromcommentid!=null && !fromcommentid.trim().equals("")) {
			comments = cPer.getNewComments(cms,urlResource,Integer.parseInt(fromcommentid), siteName);
		}
		else if (tocommentid!=null && !tocommentid.trim().equals("")) {
			comments = cPer.getPreviousComments(cms, urlResource, Integer.parseInt(tocommentid), siteName, Integer.parseInt(parentid));
		}
		else if(withMoreAnswers!=null && withMoreAnswers.toLowerCase().trim().equals("true")){
				int min_Answers = -1;
				
				if(minAnswers!=null && minAnswers!=""){
					min_Answers = Integer.parseInt(minAnswers);
				}else{
					min_Answers = CommentsModule.getInstance(cms).getMinAnswers();
				}
				
				comments = cPer.getCommentsWhitMoreAnswers(cms, urlResource,min_Answers,page, siteName);
		}else{
			
			
			int size=CommentsModule.getInstance(cms).getQueryPageSize();
			int offset = 0;
			String pageNumber=page;
			
			if (page.equals("1"))
				size = CommentsModule.getInstance(cms).getFirstPageSize();
			else {
				offset = CommentsModule.getInstance(cms).getFirstPageSize();
				pageNumber = "" + (Integer.parseInt(pageNumber)-1);
			}

			
			if (paginate!=null && paginate.toLowerCase().trim().equals("false")){
				size = 0;
				pageNumber = "1";
			}
		   comments = cPer.getCommentsByLevels(cms, urlResource, Integer.parseInt(parentid), pageNumber, size, offset);
		}
		
		if (comments!=null)
			lastElement = comments.size();
	}
	
	public Comment getComment()
	{
		return comments.get(index);
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
	
    protected void exposeComentario(Comment comentario)
    {
    	TfsComentario comment = new TfsComentario(comentario);
    	comment.setCommentCount(comments.size());
		pageContext.getRequest().setAttribute("comment", comment);

    }
    
    protected void restoreComentario()
    {
    	pageContext.getRequest().setAttribute("comment", previousComentario );
    }

	protected void saveComentario()
    {
		previousComentario  = (TfsComentario) pageContext.getRequest().getAttribute("comment");
    	pageContext.getRequest().setAttribute("comment",null);
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

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		if (page==null || page.trim().length()==0)
			this.page = null;
		else
			this.page = page;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if (url==null || url.trim().length()==0)
			this.url = null;
		else
			this.url = url;
	}
	
	public String getMinAnswers() {
		return minAnswers;
	}

	public void setMinAnswers(String minAnswers) {
		if (minAnswers==null || minAnswers.trim().length()==0)
			this.minAnswers = null;
		else
			this.minAnswers = minAnswers;
	}
	
	public void setPaginate(String paginate) {
		if (paginate==null || paginate.trim().length()==0)
			this.paginate = null;
		else
			this.paginate = paginate;
	}
	
	public String getPaginate() {
		return paginate;
	}
	
	public void setFromcommentid(String fromcommentid) {
		if (fromcommentid==null || fromcommentid.trim().length()==0)
			this.fromcommentid = null;
		else
			this.fromcommentid = fromcommentid;
	}
	
	public String getFromcommentid() {
		return fromcommentid;
	}
	
	public void setTocommentid(String tocommentid) {
		if (tocommentid==null || tocommentid.trim().length()==0)
			this.tocommentid = null;
		else
			this.tocommentid = tocommentid;
	}
	
	public String getTocommentid() {
		return tocommentid;
	}
	
	public String getWithMoreAnswers() {
		return withMoreAnswers;
	}

	public void setWithMoreAnswers(String withMoreAnswers) {
		if (withMoreAnswers==null || withMoreAnswers.trim().length()==0)
			this.withMoreAnswers = null;
		else
			this.withMoreAnswers = withMoreAnswers;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		if (parentid==null || parentid.trim().length()==0)
			this.parentid = null;
		else
			this.parentid = parentid;
	}

	public String getCollectionValue(String name) throws JspTagException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
		return null;
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCollectionPathName() throws JspTagException {
		// TODO Auto-generated method stub
		return "";
	}

}
