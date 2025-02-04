package org.opencms.ocee.db.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsIdentifiableObjectContainer;

public class CmsTransactionStatusDialog extends CmsWidgetDialog {
   public static final String KEY_PREFIX = "status";
   public static final String[] PAGES = new String[]{"page1"};
   protected CmsTransactionStatusBean Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   private List o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public CmsTransactionStatusDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsTransactionStatusDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      List errors = new ArrayList();
      this.setCommitErrors(errors);
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("GUI_TRANSACTION_DBCONTEXT_FACTORY_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 0));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
         result.append(this.dialogBlockStart(this.key("GUI_TRANSACTION_SQL_MANAGERS_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(1, this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super().size()));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.initStatusBeanObject();
      this.setKeyPrefix("status");
      this.addWidget(new CmsWidgetDialogParameter(this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new, "dbContextFactory", PAGES[0], new CmsDisplayWidget()));
      Iterator it = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super().iterator();

      while(it.hasNext()) {
         CmsTransactionInstanceType type = (CmsTransactionInstanceType)it.next();
         this.addWidget(new CmsWidgetDialogParameter(this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new, type.getType() + "SqlManager", PAGES[0], new CmsDisplayWidget()));
      }

   }

   protected String[] getPageArray() {
      return PAGES;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      this.addMessages(org.opencms.ocee.transaction.Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initStatusBeanObject() {
      this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = new CmsTransactionStatusBean();
   }

   protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
      super.initWorkplaceRequestValues(settings, request);
      this.setDialogObject(this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new);
   }

   private List o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
      if (this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super == null) {
         CmsIdentifiableObjectContainer container = new CmsIdentifiableObjectContainer(true, true);
         Iterator it = CmsTransactionInstanceType.VALUES.iterator();

         while(it.hasNext()) {
            CmsTransactionInstanceType type = (CmsTransactionInstanceType)it.next();
            container.addIdentifiableObject(type.getType(), type, (float)type.getOrder());
         }

         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = container.elementList();
      }

      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }
}
