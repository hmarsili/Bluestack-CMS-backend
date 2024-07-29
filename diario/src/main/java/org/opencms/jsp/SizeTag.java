package org.opencms.jsp;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.exceptions.ApplicationException;

public class SizeTag extends CmsJspTagContentLoop {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6764351542498961922L;
	
	public SizeTag() {
		super();
	}

	
	public SizeTag(I_CmsXmlContentContainer container, String element) {
		super(container, element);
	}
	
	@Override
	protected void init(I_CmsXmlContentContainer container) {
		super.init(container);
	}

	
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		Integer size = null;
		try {
			size = this.getXmlDocument().getValues(getElement(), getXmlDocumentLocale()).size();
			this.pageContext.getOut().print(size);
		} catch (IOException e) {
			throw new ApplicationException("No se pudo imprimir el size " + size, e);
		}
		return SKIP_BODY;
	}

}
