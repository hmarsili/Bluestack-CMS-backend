package org.opencms.ocee.vfsdoctor;

import java.util.Collections;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

public class CmsVfsDoctorSqlConsoleDialog extends CmsWidgetDialog {
   public static final String DIALOG_TYPE = "sqlconsole";
   public static final String[] PAGES = new String[]{"page1"};
   public static final String PARAM_QUERY = "query";
   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public CmsVfsDoctorSqlConsoleDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsVfsDoctorSqlConsoleDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      try {
         if (!CmsStringUtil.isNotEmptyOrWhitespaceOnly(this.getQuery())) {
            throw new CmsIllegalArgumentException(Messages.get().container("ERR_NOTHING_TO_EXECUTE_0"));
         }

         this.getToolManager().jspForwardTool(this, "/ocee-vfsdoctor/sqlconsole/output", new HashMap(Collections.singletonMap("query", this.getQuery())));
      } catch (Exception var2) {
         this.setCommitErrors(Collections.singletonList(var2));
      }

   }

   public String getQuery() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public void setQuery(String query) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = query;
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.createWidgetBlockStart(""));
         result.append(this.createDialogRowsHtml(0, 1));
         result.append(this.createWidgetBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.addWidget(new CmsWidgetDialogParameter(this, "info", PAGES[0], new CmsDisplayWidget()));
      this.addWidget(new CmsWidgetDialogParameter(this, "query", PAGES[0], new CmsTextareaWidget(10)));
   }

   public String getInfo() {
      return Messages.get().getBundle(this.getLocale()).key("GUI_OCEE_VFSDOC_SQLCONSOLE_INFO_0");
   }

   public void setInfo(String info) {
      info.toString();
   }

   protected String[] getPageArray() {
      return PAGES;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }
}
