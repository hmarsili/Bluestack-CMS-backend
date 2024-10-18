package org.opencms.jsp;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.tfsla.utils.StringUtils;

public class EscapeToUnicodeTag extends BodyTagSupport {

    private static final long serialVersionUID = -1542665804252946729L;

    @Override
    public int doAfterBody() throws JspException {
        try {
            this.getBodyContent().getEnclosingWriter().print(
                    StringUtils.toUnicode(this.getBodyContent().getString()));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.doAfterBody();
    }

}
