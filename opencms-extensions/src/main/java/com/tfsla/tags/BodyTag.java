package com.tfsla.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.Tag;

import com.tfsla.utils.Writer;

/**
 * Clase base para extender Tags que tienen body
 * 
 * @author lgassman
 *
 */
public abstract class BodyTag extends AbstractTag implements javax.servlet.jsp.tagext.BodyTag {
	
	private BodyContent content;

	
	public void setBodyContent(BodyContent arg0) {
		this.content = arg0;
		changeBodyContext();
	}

	protected void changeBodyContext() {
		this.setWriter(new Writer(this));
	}

	@Override
	protected void changePageContext() {
		//lo sobreescribe para que no le pise el writer
	}
	
	public BodyContent getBodyContent() {
		return this.content;
	}
	
	@SuppressWarnings("unused")
	public final void doInitBody() throws JspException {
		this.initBody();
	}

	/**
	 * Inicia el Body
	 * No tira ChequedException 
	 */
	protected void initBody() {
	
	}

	@SuppressWarnings("unused")
	public int doAfterBody() throws JspException {
		this.afterBody();
		return Tag.EVAL_PAGE;
	}
	
	protected void afterBody() {
		
	}

	protected String getValue() {
		return this.getBodyContent().getString();
	}

}
