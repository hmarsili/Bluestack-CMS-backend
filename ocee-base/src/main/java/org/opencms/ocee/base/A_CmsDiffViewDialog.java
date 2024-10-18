package org.opencms.ocee.base;

import com.alkacon.diff.Diff;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

public abstract class A_CmsDiffViewDialog extends CmsDialog {
   private CmsDiffViewMode o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   protected A_CmsDiffViewDialog(CmsJspActionElement jsp) {
      super(jsp);
      this.setParamStyle("new");
   }

   public void displayDialog() throws Exception {
      if (this.getAction() == 4) {
         this.actionCloseDialog();
      }

      JspWriter out = this.getJsp().getJspContext().getOut();
      out.println(this.htmlStart());
      out.print("<link rel='stylesheet' type='text/css' href='");
      out.print(getStyleUri(this.getJsp()));
      out.println("diff.css'>");
      out.println(this.bodyStart((String)null));
      out.println(this.dialogStart());
      out.println(this.dialogContentStart(this.getParamTitle()));
      out.print("<form name='diff-form' method='post' action='");
      out.print(this.getDialogUri());
      out.println("'>");
      out.println(this.allParamsAsHidden());
      out.println("</form>");
      out.println("<p style='text-align: right;'>");
      String iconPath = null;
      String onClic = "javascript:document.forms['diff-form'].mode.value = '";
      if (this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super == CmsDiffViewMode.ALL) {
         iconPath = "list/details_hide.png";
         onClic = onClic + CmsDiffViewMode.DIFF_ONLY;
      } else {
         iconPath = "list/details_show.png";
         onClic = onClic + CmsDiffViewMode.ALL;
      }

      onClic = onClic + "'; document.forms['diff-form'].submit();";
      out.println(A_CmsHtmlIconButton.defaultButtonHtml(CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT, "id", this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.getName().key(this.getLocale()), (String)null, true, iconPath, (String)null, onClic));
      out.println("</p>");
      out.println(this.dialogBlockStart((String)null));
      out.println("<table cellspacing='0' cellpadding='0' class='xmlTable'>\n<tr><td><pre>");

      try {
         CmsHtmlDiffConfiguration conf = new CmsHtmlDiffConfiguration(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super == CmsDiffViewMode.ALL ? -1 : this.getLinesBeforeSkip(), this.getLocale());
         out.println(Diff.diffAsHtml(this.getOriginalSource(), this.getCopySource(), conf));
      } catch (Exception var5) {
         out.print(var5);
      }

      out.println("</pre></td></tr>\n</table>");
      out.println(this.dialogBlockEnd());
      out.println(this.dialogContentEnd());
      out.println(this.dialogEnd());
      out.println(this.bodyEnd());
      out.println(this.htmlEnd());
   }

   public String getParamMode() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super == null ? null : this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.getMode();
   }

   public void setParamMode(String mode) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = CmsDiffViewMode.valueOf(mode);
   }

   protected abstract String getCopySource();

   protected abstract int getLinesBeforeSkip();

   protected abstract String getOriginalSource();

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      super.initWorkplaceRequestValues(settings, request);
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamMode())) {
         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = CmsDiffViewMode.DIFF_ONLY;
      }

      try {
         this.validateParamaters();
      } catch (Exception var6) {
         this.setAction(4);

         try {
            this.actionCloseDialog();
         } catch (JspException var5) {
         }

      }
   }

   protected abstract void validateParamaters() throws Exception;
}
