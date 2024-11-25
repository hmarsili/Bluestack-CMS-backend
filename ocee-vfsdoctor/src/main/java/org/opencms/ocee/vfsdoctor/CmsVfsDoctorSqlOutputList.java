package org.opencms.ocee.vfsdoctor;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.CmsStringBufferReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsVfsDoctorSqlOutputList extends A_CmsListDialog {
   public static final String LIST_ID = "lso";
   private String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   private List Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;

   public CmsVfsDoctorSqlOutputList(CmsJspActionElement jsp) {
      super(jsp, "lso", Messages.get().container("GUI_SQLOUTPUT_LIST_NAME_0"), (String)null, (CmsListOrderEnum)null, (String)null);
   }

   public CmsVfsDoctorSqlOutputList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() {
      this.throwListUnsupportedActionException();
   }

   public String getParamQuery() {
      return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   }

   public void setParamQuery(String paramQuery) {
      this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = paramQuery;
   }

   protected String customHtmlStart() {
      StringBuffer result = new StringBuffer(2048);
      result.append(this.dialogBlockStart(this.key("GUI_SQLOUTPUT_LIST_NAME_0")));
      result.append(CmsStringUtil.substitute(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, "\n", "<br>\n"));
      result.append(this.dialogBlockEnd());
      return result.toString();
   }

   protected String defaultActionHtmlContent() {
      return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object == null ? "" : super.defaultActionHtmlContent();
   }

   protected void fillDetails(String detailId) {
   }

   protected List getListItems() {
      List ret = new ArrayList();
      if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object == null) {
         return ret;
      } else {
         for(int j = 1; j < this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.size(); ++j) {
            CmsListItem item = this.getList().newItem("item" + j);
            List row = (List)this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.get(j);

            for(int i = 0; i < row.size(); ++i) {
               Object value = row.get(i);
               item.set("col" + i, value.toString());
            }

            ret.add(item);
         }

         return ret;
      }
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      metadata.setVolatile(true);
      CmsStringBufferReport report = new CmsStringBufferReport(this.getLocale());
      this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = CmsVfsDoctorManager.getInstance().getSqlConsole().execute(this.getParamQuery(), report);
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = report.toString();
      if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object != null) {
         List heading = (List)this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.get(0);

         for(int i = 0; i < heading.size(); ++i) {
            CmsListColumnDefinition col = new CmsListColumnDefinition("col" + i);
            col.setName(new CmsMessageContainer((I_CmsMessageBundle)null, heading.get(i).toString()));
            col.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
            col.setSorteable(true);
            metadata.addColumn(col);
         }

      }
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }

   protected void validateParamaters() throws Exception {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamQuery())) {
         throw new Exception();
      }
   }
}