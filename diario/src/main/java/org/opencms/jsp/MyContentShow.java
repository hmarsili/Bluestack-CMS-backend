package org.opencms.jsp;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Hace lo mismo que el contentShow
 * Esta clase está como ejemplo de cómo usar la clase abstracta AbstractOpenCmsTag
 * @author lgassman
 */
public class MyContentShow extends AbstractOpenCmsTag {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -6776067180965738432L;

    @Override
    public int doStartTag() throws JspException {

        String content = getContent();
        try {
           	this.getPageContext().getOut().print(content);
        }
        catch (IOException e) {
            if (getLog().isErrorEnabled()) {
                getLog().error(Messages.get().getBundle().key(Messages.LOG_ERR_JSP_BEAN_0), e);
            }
            throw new JspException(e);
        }
        return Tag.SKIP_BODY;
    }
}