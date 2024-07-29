package org.opencms.jsp;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

public class ContentPropertyTag extends AbstractOpenCmsTag {
	private String name;

	@Override
	public int doStartTag() throws JspException {
		CmsObject object = CmsFlexController.getController(this.getPageContext().getRequest()).getCmsObject();
		String resourceName = this.getAncestor().getResourceName();
		try {
			this.getPageContext().getOut().print(object.readPropertyObject(resourceName, this.getName(), false).getValue());
		}
		catch (Exception e) {
			throw new JspException("leyendo la propiedad " + getName() + " de " + resourceName , e);
		}
		return SKIP_BODY;
	}

	private String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
