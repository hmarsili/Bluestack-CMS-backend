package com.tfsla.rankViews.jsp;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.AbstractOpenCmsTag;
import org.opencms.main.CmsException;

import com.tfsla.rankViews.service.CommentRankService;

public class CommentAddValoracionTag  extends AbstractOpenCmsTag {

	public String getCommentid() {
		return commentid;
	}

	public void setCommentid(String commentid) {
		this.commentid = commentid;
	}

	private String commentid;
	private String name;
	private String value;

	private CmsObject cms;
	
	private PageContext pageContext;

	public CommentAddValoracionTag()
	{
		super();
	}
	
	@Override
	public int doStartTag() throws JspException {
		
    	cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	CommentRankService rService = new CommentRankService();
		HttpSession session = pageContext.getSession();
		
		CmsResource res;
		try {

			int valor = Integer.parseInt(getValue());

				res = cms.readResource(getName());
				rService.addValoracion(res, getCommentid(), cms, session, valor);
				
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return SKIP_BODY;
	}
	
	private String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPageContext(PageContext arg0) {
		super.setPageContext(arg0);
		this.pageContext = arg0;

	}

	private String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
