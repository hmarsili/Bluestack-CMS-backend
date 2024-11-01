package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;

public class CmsNewsletterEditorWrapper extends CmsDialog {
   private String m_paramBacklink;

   public CmsNewsletterEditorWrapper(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsNewsletterEditorWrapper(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void displayDialog() throws Exception {
      this.initAdminTool();
      JspWriter out = this.getJsp().getJspContext().getOut();
      out.print(this.htmlStart());
      out.print(this.bodyStart((String)null));
      out.print("<form name='editor' method='post' target='_top' action='");
      out.print(this.getJsp().link("/system/workplace/editors/editor.jsp"));
      out.print("'>\n");
      out.print(this.paramsAsHidden());
      out.print("</form>\n");
      out.print("<script type='text/javascript'>\n");
      out.print("document.forms['editor'].submit();\n");
      out.print("</script>\n");
      out.print(this.bodyEnd());
      out.print(this.htmlEnd());
   }

   public String getParamBacklink() {
      return this.m_paramBacklink;
   }

   public void setParamBacklink(String paramBacklink) {
      this.m_paramBacklink = paramBacklink;
   }
}
