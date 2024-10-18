package org.opencms.jsp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.tfsla.tags.AbstractTag;
import com.tfsla.utils.Writer;

/**
 * Agrega el botones en una JSP que scrollean con la pantalla.
 * @author mpotelfeola
 */
public abstract class AbstractButtonsTag extends AbstractTag {

    @Override
    public int doStartTag() {
        if (!isOnline()) {
        	this.getWriter().println("<div id=\"divStayTopLeft\" style=\"position:absolute;z-index:1\" >");
            this.getWriter().println(this.getButton());
            this.getWriter().println("</div>");
            this.appendScript(this.getWriter());
        }
        return SKIP_BODY;
    }

    protected abstract String getButton();
    
    private boolean isOnline() {
        CmsJspActionElement cms = new CmsJspActionElement();
        cms.init(this.getPageContext(), (HttpServletRequest) this.getPageContext().getRequest(),
                (HttpServletResponse) this.getPageContext().getResponse());

        return cms.getCmsObject().getRequestContext().currentProject().isOnlineProject();
    }

    private void appendScript(Writer writer) {
    	writer.println("<script type=\"text/javascript\">");
    	writer.println("function JSFX_FloatTopLeft()");
        writer.println("{");
        writer.println("var startX = 10, startY = 10;");
        writer.println("var ns = (navigator.appName.indexOf(\"Netscape\") != -1);");
        writer.println("var d = document;");
        writer.println("var px = document.layers ? \"\" : \"px\";");
        writer.println("function ml(id)");
        writer.println("{");
        writer.println("var el=d.getElementById?d.getElementById(id):d.all?d.all[id]:d.layers[id];");
        writer.println("if(d.layers)el.style=el;");
        writer.println("el.sP=function(x,y){this.style.left=x+px;this.style.top=y+px;};");
        writer.println("el.x = startX; el.y = startY;");
        writer.println("return el;");
        writer.println("}");
        writer.println("window.stayTopLeft=function()");
        writer.println("{");
        writer.println("var pY = ns ? pageYOffset : document.documentElement && document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop;");
        writer.println("ftlObj.y += (pY + startY - ftlObj.y)/8;");
        writer.println("ftlObj.sP(ftlObj.x, ftlObj.y);");
        writer.println("setTimeout(\"stayTopLeft()\", 40);");
        writer.println("}");
        writer.println("ftlObj = ml(\"divStayTopLeft\");");
        writer.println("stayTopLeft();");
        writer.println("}");
        writer.println("JSFX_FloatTopLeft();");
        writer.println("</script> ");
    }

}