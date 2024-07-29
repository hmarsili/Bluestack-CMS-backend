package org.opencms.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsSecurityManager;
import org.opencms.main.CmsLog;
/**
 * Es una clase base para extenders tags con cuerpo que usan OpenCMS
 *  
 * @author lgassman
 */
public abstract class AbstractOpenCmsBodyTag extends AbstractOpenCmsTag implements
		BodyTag {

	private BodyContent bodyContent;
	
	public void setBodyContent(BodyContent b) {
		this.bodyContent = b;
	}
	
	protected BodyContent getBodyContent() {
		return this.bodyContent;
	}

	@SuppressWarnings("unused")
	public void doInitBody() throws JspException {
		//nada
	}

	@SuppressWarnings({ "unused", "static-access" })
	public int doAfterBody() throws JspException {
		//nada
		return BodyTag.EVAL_PAGE;
	}

    public static Log getLogger() {
        //hack uso este logger que se que ya esta configurado
        return CmsLog.getLog(CmsSecurityManager.class);
    }
}
