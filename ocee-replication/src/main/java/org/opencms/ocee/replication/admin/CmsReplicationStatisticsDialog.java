package org.opencms.ocee.replication.admin;

import java.util.Date;
import java.util.Iterator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.ocee.replication.CmsReplicationStatistics;
import org.opencms.ocee.replication.I_CmsReplicationHandler;
import org.opencms.util.CmsDateUtil;
import org.opencms.workplace.CmsDialog;

public class CmsReplicationStatisticsDialog extends CmsDialog {
   public CmsReplicationStatisticsDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsReplicationStatisticsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void displayDialog() throws Exception {
      StringBuffer html = new StringBuffer(1024);
      html.append(this.defaultActionHtmlStart());
      html.append(this.dialogContentStart((String)null));
      boolean isFirst = true;

      for(Iterator itHandlers = CmsReplicationManager.getInstance().getConfiguration().getReplicationHandlers().iterator(); itHandlers.hasNext(); html.append(this.dialogBlockEnd())) {
         I_CmsReplicationHandler handler = (I_CmsReplicationHandler)itHandlers.next();
         html.append(this.dialogBlockStart(handler.getName().key(this.getLocale())));
         html.append("\n\t<table cellspacing='0' cellpadding='0' class='xmlTable'>\n");
         CmsReplicationStatistics stats = handler.getStatistics();
         if (stats != null) {
            html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(this.key("GUI_REPLICATION_HANDLERS_STATS_DATE_0"), CmsDateUtil.getDateTime(new Date(stats.getDate()), 0, this.getLocale()), isFirst ? "date" : null));
            html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(this.key("GUI_REPLICATION_HANDLERS_STATS_DURATION_0"), Long.toString(stats.getDuration()), isFirst ? "duration" : null));
            html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(this.key("GUI_REPLICATION_HANDLERS_STATS_USERNAME_0"), stats.getUserName(), isFirst ? "user" : null));
            html.append("\n\t<tr><td colspan='5'>\n");
            html.append(this.dialogBlockStart(this.key("GUI_REPLICATION_HANDLERS_STATS_DETAILS_0")));
            html.append("\n\t<table cellspacing='0' cellpadding='0' class='xmlTable'>\n");
            Iterator itDetails;
            String key;
            if (!stats.getDetailKeys().isEmpty()) {
               itDetails = stats.getDetailKeys().iterator();

               while(itDetails.hasNext()) {
                  key = (String)itDetails.next();
                  String value = stats.getDetails(key);
                  html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(handler.getStatDetailName(key).key(this.getLocale()), value, handler.getClass().getName() + key));
               }
            } else {
               html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super((String)null, this.key("GUI_REPLICATION_HANDLERS_STATS_DETAILS_NOTAVAILABLE_0"), (String)null));
            }

            html.append("\n\t</table>\n");
            if (!stats.getDetailKeys().isEmpty()) {
               itDetails = stats.getDetailKeys().iterator();

               while(itDetails.hasNext()) {
                  key = (String)itDetails.next();
                  html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(handler.getClass().getName() + key, handler.getStatDetailHelpText(key).key(this.getLocale())));
               }
            }

            html.append("\n\t</td></tr>\n");
         } else {
            html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super((String)null, this.key("GUI_REPLICATION_HANDLERS_STATS_NOTAVAILABLE_0"), (String)null));
         }

         html.append("\n\t</table>\n");
         if (stats != null && isFirst) {
            html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super("date", this.key("GUI_REPLICATION_HANDLERS_STATS_DATE_HELP_0")));
            html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super("duration", this.key("GUI_REPLICATION_HANDLERS_STATS_DURATION_HELP_0")));
            html.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super("user", this.key("GUI_REPLICATION_HANDLERS_STATS_USERNAME_HELP_0")));
            isFirst = false;
         }
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

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      Iterator it = CmsReplicationManager.getInstance().getConfiguration().getReplicationHandlers().iterator();

      while(it.hasNext()) {
         Object handler = it.next();
         this.addMessages(handler.getClass().getPackage().getName() + ".messages");
      }

      super.initMessages();
   }

   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(String helpId, String helpText) {
      StringBuffer html = new StringBuffer(1024);
      html.append("\t<div class='help' id='help");
      html.append(helpId);
      html.append("' onmouseover=\"sMH('");
      html.append(helpId);
      html.append("');\" onmouseout=\"hMH('");
      html.append(helpId);
      html.append("');\">");
      html.append(helpText);
      html.append("</div>");
      html.append("\n");
      return html.toString();
   }

   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(String key, String value, String helpId) {
      StringBuffer html = new StringBuffer(1024);
      html.append("\t\t<tr>\n");
      if (key != null) {
         html.append("\t\t\t<td class='xmlLabel'>");
         html.append(key);
         html.append(":&nbsp;</td>\n");
      } else {
         html.append("\t\t\t<td class='xmlLabel'>&nbsp;</td>\n");
      }

      if (helpId != null) {
         html.append("\t\t\t<td><img id='img");
         html.append(helpId);
         html.append("' src='");
         html.append(getSkinUri());
         html.append("commons/help.png");
         html.append("' alt='' border='0' onmouseover=\"sMH('");
         html.append(helpId);
         html.append("');\" onmouseout=\"hMH('");
         html.append(helpId);
         html.append("');\"></td>\n");
      } else {
         html.append("\t\t\t<td>&nbsp;</td>\n");
      }

      html.append("\t\t\t<td class='xmlTd'><span class='xmlInput textInput' style='border: 0px solid black;'>&nbsp;");
      html.append(value);
      html.append("</span></td>\n");
      html.append("\t\t\t<td><span style='display:block; height: 1px; width: 5px;'></span></td>\n");
      html.append("\t\t\t<td></td>\n");
      html.append("\t\t</tr>\n");
      return html.toString();
   }
}
