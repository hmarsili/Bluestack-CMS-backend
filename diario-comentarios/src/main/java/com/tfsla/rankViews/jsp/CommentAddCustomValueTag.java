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
import com.tfsla.rankViews.service.RankService;

public class CommentAddCustomValueTag  extends AbstractOpenCmsTag {

	public String getCommentid() {
		return commentid;
	}

	public void setCommentid(String commentid) {
		this.commentid = commentid;
	}

	private String commentid;
	private String name;
	private String counter;
	private String value;

	private CmsObject cms;
	
	private PageContext pageContext;

	public CommentAddCustomValueTag()
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
			int custom = Integer.parseInt(getCounter());

			res = cms.readResource(getName());
			rService.addHitCustom(res, getCommentid(), cms, session, custom, valor);
				
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

	public String getCounter() {
		return counter;
	}

	public void setCounter(String counter) {
		this.counter = counter;
	}


}
