package com.tfsla.rankViews.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;

import org.opencms.flex.CmsFlexController;
import org.opencms.file.CmsObject;

import com.tfsla.rankViews.service.RankService;

import javax.servlet.jsp.tagext.Tag;

public class GenericHitTag implements Tag {

	private String name;

	private CmsObject cms;
	
	private PageContext pageContext;

	public GenericHitTag()
	{
		super();
	}
	
    public int doEndTag() {
        // need to release manually, JSP container may not call release as required (happens with Tomcat)
        return Tag.EVAL_PAGE;
    }


	public int doStartTag() throws JspException {
		
    	cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		RankService rService = new RankService();
		HttpSession session = pageContext.getSession();
		
		rService.countHitView(getName(), cms, session);
		return Tag.SKIP_BODY;
	}
	
	private String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPageContext(PageContext arg0) {
		this.pageContext = arg0;

	}

	public Tag getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public void release() {
	}

	public void setParent(Tag arg0) {
	}

}
