package com.tfsla.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import com.tfsla.utils.Writer;

/**
 * Clase base para extender tags
 * 
 * @author lgassman
 */
public abstract class AbstractTag implements Tag {

	private PageContext pageContext;
	private Tag parent;
	private Writer writer;
	
	public void setPageContext(PageContext arg0) {
		this.pageContext = arg0;
		this.changePageContext();
	}
	
	/**
	 * Template method para hacer algo cuando cambia el contexto
	 */
	protected void changePageContext() {
		this.writer = new Writer(this);
	}

	public PageContext getPageContext() {
		return this.pageContext;
	}

	public void setParent(Tag arg0) {
		this.parent = arg0;
	}

	public Tag getParent() {
		return this.parent;
	}

	@SuppressWarnings("unused")
	public int doStartTag() throws JspException {
		return Tag.EVAL_BODY_INCLUDE;
	}


	@SuppressWarnings("unused")
	public int doEndTag() throws JspException {
		return Tag.EVAL_PAGE;
	}

	/**
	 * Cuando se finaliza
	 */
	public void release() {
		
	}

	protected Writer getWriter() {
		return this.writer;
	}
	
	protected void setWriter(Writer writer) {
		this.writer = writer;
	}

}
