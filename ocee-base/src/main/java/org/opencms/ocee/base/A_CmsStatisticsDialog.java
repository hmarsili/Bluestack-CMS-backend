package org.opencms.ocee.base;

import java.util.Iterator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;

public abstract class A_CmsStatisticsDialog extends CmsDialog {
   public static final String KEY_PREFIX = "counter.";
   public static final String KEY_GROUP_PREFIX = "group.";
   public static final String KEY_HELP_POSTFIX = ".help";

   public A_CmsStatisticsDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   protected abstract CmsStatisticalCounterCollection getCounters();

   public A_CmsStatisticsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void displayDialog() throws Exception {
      StringBuffer html = new StringBuffer(1024);
      html.append(this.defaultActionHtmlStart());
      html.append(this.dialogContentStart((String)null));
      CmsStatisticalCounterCollection counters = this.getCounters();
      Iterator itGroups = counters.getGroups().iterator();

      while(itGroups.hasNext()) {
         String group = (String)itGroups.next();
         html.append(this.dialogBlockStart(this.key("counter.group." + group)));
         html.append("\n\t<table cellspacing='0' cellpadding='0' class='xmlTable'>\n");
         Iterator itCounters = counters.getCountersForGroup(group).iterator();

         I_CmsStatisticalCounter counter;
         while(itCounters.hasNext()) {
            counter = (I_CmsStatisticalCounter)itCounters.next();
            html.append("\t\t<tr>\n");
            html.append("\t\t\t<td class='xmlLabel'>");
            html.append(this.key("counter." + counter.getName()));
            html.append(":&nbsp;</td>\n");
            html.append("\t\t\t<td><img id='img");
            html.append("counter.");
            html.append(counter.getName());
            html.append("' src='");
            html.append(getSkinUri());
            html.append("commons/help.png");
            html.append("' alt='' border='0' onmouseover=\"sMH('");
            html.append("counter.");
            html.append(counter.getName());
            html.append(".help");
            html.append("');\" onmouseout=\"hMH('");
            html.append("counter.");
            html.append(counter.getName());
            html.append(".help");
            html.append("');\"></td>\n");
            html.append("\t\t\t<td class='xmlTd'><span class='xmlInput textInput' style='border: 0px solid black;'>&nbsp;");
            html.append(counter.toString(this));
            html.append("</span></td>\n");
            html.append("\t\t\t<td><span style='display:block; height: 1px; width: 5px;'></span></td>\n");
            html.append("\t\t\t<td></td>\n");
            html.append("\t\t</tr>\n");
         }

         html.append("\n\t</table>\n");
         itCounters = counters.getCountersForGroup(group).iterator();

         while(itCounters.hasNext()) {
            counter = (I_CmsStatisticalCounter)itCounters.next();
            html.append("\t<div class='help' id='help");
            html.append("counter.");
            html.append(counter.getName());
            html.append(".help");
            html.append("' onmouseover=\"sMH('");
            html.append("counter.");
            html.append(counter.getName());
            html.append(".help");
            html.append("');\" onmouseout=\"hMH('");
            html.append("counter.");
            html.append(counter.getName());
            html.append(".help");
            html.append("');\">");
            html.append(this.key("counter." + counter.getName() + ".help"));
            html.append("</div>");
            html.append("\n");
         }

         html.append(this.dialogBlockEnd());
      }

      html.append(this.dialogContentEnd());
      html.append(this.defaultActionHtmlEnd());
      JspWriter out = this.getJsp().getJspContext().getOut();
      out.print(html.toString());
   }

   protected String defaultActionHtmlEnd() {
      StringBuffer result = new StringBuffer(2048);
      result.append(this.dialogEnd());
      result.append(this.bodyEnd());
      result.append(this.htmlEnd());
      return result.toString();
   }

   protected String defaultActionHtmlStart() {
      StringBuffer result = new StringBuffer(2048);
      result.append(this.htmlStart((String)null));
      result.append(this.bodyStart("dialog", (String)null));
      result.append(this.dialogStart());
      return result.toString();
   }
}
