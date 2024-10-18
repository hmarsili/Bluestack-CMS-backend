package org.opencms.jsp;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.tfsla.utils.StringUtils;

/**
 * Este tag convierte todos los caracteres a Unicode y elimina todos los tags span.
 * 
 * @author mpotelfeola
 */
public class ParseForWAPTag extends BodyTagSupport {
    
    private static final long serialVersionUID = -1542665804252946729L;
    
    private static String REGEX_DELETE_SPANS = "</?[sS][pP][aA][nN]((.|\n)*?)>";
        
    @Override
    public int doAfterBody() throws JspException {
        try {
            this.getBodyContent().getEnclosingWriter().print(
                    this.parseForWAP(this.getBodyContent().getString()));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.doAfterBody();
    }

    private String parseForWAP(String string) {
        String temp = StringUtils.toUnicode(string);
        temp = StringUtils.escapeJSQuotes(temp);
        temp = this.deleteAllSpans(temp);
        temp = temp.replaceAll("</br>", "<br>");
        temp = temp.replaceAll("</p>", "<br>");
        temp = temp.replaceAll("<p>", "<br>");
        return temp;
    }

    /**
     * Borra todos los spans que tiene el html, ya que algunos crean conflictos en WAP.
     */
    public String deleteAllSpans(String string) {
        return string.replaceAll(REGEX_DELETE_SPANS, "");
    }
   
}
