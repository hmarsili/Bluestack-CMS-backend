package com.alkacon.opencms.newsletter.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListResourceCollector;

public class CmsNewsletterList extends A_CmsListExplorerDialog {
   public static final String LIST_COLUMN_SCORE = "cs";
   public static final String LIST_DETAIL_EXCERPT = "de";
   public static final String LIST_ID = "anl";
   public static final String PATH_BUTTONS = "tools/newsletter/buttons/";
   private I_CmsListResourceCollector m_collector;
   private String m_paramOufqn;

   public CmsNewsletterList(CmsJspActionElement jsp) {
      super(jsp, "anl", Messages.get().container("GUI_NEWSLETTER_LIST_NAME_0"), "ecn", CmsListOrderEnum.ORDER_ASCENDING, (String)null);
   }

   public CmsNewsletterList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() throws IOException, ServletException {
      if (this.getParamListAction().equals("eae")) {
         Map params = new HashMap();
         params.put("action", "initial");
         String link = "/system/workplace/views/workplace.jsp?oufqn=" + this.getParamOufqn();
         params.put("backlink", link);
         params.put("oufqn", this.getParamOufqn());
         params.put("resource", this.getSelectedItem().get("ecn"));
         this.getToolManager().jspForwardPage(this, "/system/workplace/admin/newsletter/edit.jsp", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   public I_CmsListResourceCollector getCollector() {
      if (this.m_collector == null) {
         this.m_collector = new CmsNewsletterResourcesCollector(this);
         CmsResourceUtil resUtil = this.getResourceUtil();
         resUtil.setAbbrevLength(50);
         resUtil.setSiteMode(CmsResourceUtil.SITE_MODE_MATCHING);
      }

      return this.m_collector;
   }

   public String getParamOufqn() {
      return this.m_paramOufqn;
   }

   public void setParamOufqn(String ouFqn) {
      if (ouFqn == null) {
         ouFqn = "";
      }

      this.m_paramOufqn = ouFqn;
   }

   protected String defaultActionHtmlStart() {
      StringBuffer result = new StringBuffer(2048);
      result.append(this.htmlStart((String)null));
      result.append(this.getList().listJs());
      result.append(CmsListExplorerColumn.getExplorerStyleDef());
      result.append("<script language='JavaScript'>\n");
      result.append((new CmsExplorer(this.getJsp())).getInitializationHeader());
      result.append("\ntop.updateWindowStore();\n");
      result.append("top.displayHead(top.win.head, 0, 1);\n}\n");
      result.append("</script>");
      result.append(this.bodyStart("dialog", "onload='initialize();'"));
      result.append(this.dialogStart());
      result.append(this.dialogContentStart(this.getParamTitle()));
      return result.toString();
   }

   protected void fillDetails(String detailId) {
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      super.setColumns(metadata);
   }

   protected void setColumnVisibilities() {
      super.setColumnVisibilities();
      this.setColumnVisibility("ece".hashCode(), "ece".hashCode());
      this.setColumnVisibility(2, 0);
      this.setColumnVisibility(8, 0);
      this.setColumnVisibility(128, 0);
      this.setColumnVisibility(1024, 0);
      this.setColumnVisibility(32, 0);
      this.setColumnVisibility(16, 0);
      this.setColumnVisibility(256, 0);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }

   protected void validateParamaters() throws Exception {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
