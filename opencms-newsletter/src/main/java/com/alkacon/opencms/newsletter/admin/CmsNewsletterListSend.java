package com.alkacon.opencms.newsletter.admin;

import com.alkacon.opencms.newsletter.CmsNewsletterMail;
import com.alkacon.opencms.newsletter.CmsNewsletterManager;
import com.alkacon.opencms.newsletter.I_CmsNewsletterMailData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListResourceCollector;

public class CmsNewsletterListSend extends A_CmsListExplorerDialog {
   public static final String LIST_ACTION_SEND = "ease";
   public static final String LIST_COLUMN_DATA = "ecda";
   public static final String LIST_COLUMN_SCORE = "cs";
   public static final String LIST_COLUMN_SEND = "ecse";
   public static final String LIST_ID = "anls";
   public static final String PATH_BUTTONS = "tools/newsletter/buttons/";
   private I_CmsListResourceCollector m_collector;
   private String m_paramGroupId;
   private String m_paramOufqn;
   private static final Log LOG = CmsLog.getLog(CmsNewsletterListSend.class);

   public CmsNewsletterListSend(CmsJspActionElement jsp) {
      super(jsp, "anls", Messages.get().container("GUI_NEWSLETTER_LIST_NAME_0"), "ecn", CmsListOrderEnum.ORDER_ASCENDING, (String)null);
   }

   public CmsNewsletterListSend(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() {
      if (this.getParamListAction().equals("ease")) {
         String resourceName = (String)this.getSelectedItem().get("ecn");
         CmsUUID groupId = new CmsUUID(this.getParamGroupId());

         try {
            CmsGroup group = this.getCms().readGroup(groupId);
            I_CmsNewsletterMailData mailData = CmsNewsletterManager.getMailData(this.getJsp(), group, resourceName);
            String rootPath = resourceName;
            if (mailData.getContent() != null) {
               rootPath = mailData.getContent().getFile().getRootPath();
            }

            if (mailData.isSendable()) {
               CmsNewsletterMail nlMail = new CmsNewsletterMail(mailData.getEmail(), mailData.getRecipients(), rootPath);
               nlMail.start();
               this.getList().clear();
            }
         } catch (Exception var7) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().container("LOG_NEWSLETTER_SEND_FAILED_0"), var7);
            }

            this.throwListUnsupportedActionException();
         }
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

   public String getParamGroupId() {
      return this.m_paramGroupId;
   }

   public String getParamOufqn() {
      return this.m_paramOufqn;
   }

   public void setParamGroupId(String paramGroupId) {
      this.m_paramGroupId = paramGroupId;
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
      CmsListColumnDefinition sendIconCol = new CmsListColumnDefinition("ecse");
      sendIconCol.setName(Messages.get().container("GUI_NEWSLETTER_LIST_COLS_SEND_0"));
      sendIconCol.setHelpText(Messages.get().container("GUI_NEWSLETTER_LIST_COLS_SEND_HELP_0"));
      sendIconCol.setWidth("20");
      sendIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      CmsListDirectAction sendAction = new CmsListSendNewsletterAction("ease", "ecn");
      sendAction.setEnabled(true);
      sendAction.setConfirmationMessage(Messages.get().container("GUI_NEWSLETTER_LIST_ACTION_SEND_CONF_0"));
      sendIconCol.addDirectAction(sendAction);
      CmsListDirectAction nosendAction = new CmsListSendNewsletterAction("eased", "ecn");
      nosendAction.setEnabled(false);
      sendIconCol.addDirectAction(nosendAction);
      metadata.addColumn(sendIconCol, 0);
      CmsListColumnDefinition newsletterCol = new CmsListExplorerColumn("ecda");
      newsletterCol.setName(Messages.get().container("GUI_NEWSLETTER_LIST_COLS_DATA_0"));
      newsletterCol.setHelpText(Messages.get().container("GUI_NEWSLETTER_LIST_COLS_DATA_HELP_0"));
      newsletterCol.setVisible(true);
      newsletterCol.setSorteable(false);
      metadata.addColumn(newsletterCol);
   }

   protected void setColumnVisibilities() {
      super.setColumnVisibilities();
      this.setColumnVisibility("ece".hashCode(), "ece".hashCode());
      this.setColumnVisibility(2, 0);
      this.setColumnVisibility(2, 0);
      this.setColumnVisibility(8, 0);
      this.setColumnVisibility(128, 0);
      this.setColumnVisibility(1024, 0);
      this.setColumnVisibility(32, 0);
      this.setColumnVisibility(16, 0);
      this.setColumnVisibility(256, 0);
      this.setColumnVisibility(4, 0);
      this.setColumnVisibility(4096, 0);
      this.setColumnVisibility(8192, 0);
      this.setColumnVisibility("ece".hashCode(), 0);
      this.setColumnVisibility("ecti".hashCode(), 0);
      this.setColumnVisibility("ecli".hashCode(), 0);
      this.setColumnVisibility("ecpi".hashCode(), 0);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }

   protected void validateParamaters() throws Exception {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
