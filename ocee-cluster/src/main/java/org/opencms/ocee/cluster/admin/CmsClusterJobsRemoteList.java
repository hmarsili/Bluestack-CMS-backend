package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDefaultComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListItemComparator;
import org.opencms.workplace.tools.scheduler.CmsContextInfoDetailsFormatter;

public class CmsClusterJobsRemoteList extends A_CmsListDialog {
   public static final String LIST_ACTION_DELETE = "ad";
   public static final String LIST_COLUMN_DELETE = "cd";
   public static final String LIST_MACTION_DELETE = "md";
   public static final String LIST_ACTION_ICON = "aic";
   public static final String LIST_COLUMN_ICON = "cic";
   public static final String LIST_ID = "lcjr";
   private String m_paramServer;

   public CmsClusterJobsRemoteList(CmsJspActionElement jsp) {
      this(jsp, "lcjr");
   }

   public CmsClusterJobsRemoteList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsClusterJobsRemoteList(CmsJspActionElement jsp, String listId) {
      super(jsp, listId, Messages.get().container("GUI_CLUSTER_JOBS_REMOTE_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      if (this.getParamListAction().equals("md")) {
         List ids = new ArrayList();
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            ids.add(listItem.getId());
         }

         CmsClusterRemoteCmdHelper.deleteScheduleJobs(this.getCms(), this.getServer(), ids);
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public void executeListSingleActions() throws CmsRuntimeException {
      if (this.getParamListAction().equals("ad")) {
         String jobId = this.getSelectedItem().getId();
         CmsClusterRemoteCmdHelper.deleteScheduleJobs(this.getCms(), this.getServer(), Collections.singletonList(jobId));
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void fillDetails(String detailId) {
      Iterator i = this.getList().getAllContent().iterator();

      while(true) {
         while(i.hasNext()) {
            CmsListItem item = (CmsListItem)i.next();
            CmsScheduledJobInfo job = CmsClusterRemoteCmdHelper.getScheduledJob(this.getCms(), this.getServer(), item.getId());
            if (detailId.equals("dc")) {
               item.set("dc", job.getContextInfo());
            } else if (detailId.equals("dp")) {
               StringBuffer params = new StringBuffer(32);
               Iterator paramIt = job.getParameters().keySet().iterator();

               while(paramIt.hasNext()) {
                  String param = (String)paramIt.next();
                  String value = (String)job.getParameters().get(param);
                  params.append(param).append("=");
                  params.append(value).append("<br>");
               }

               item.set("dp", params);
            }
         }

         return;
      }
   }

   protected List getListItems() {
      List items = new ArrayList();
      Iterator itJobs = CmsClusterRemoteCmdHelper.getScheduledJobs(this.getCms(), this.getServer()).iterator();

      while(itJobs.hasNext()) {
         CmsScheduledJobInfo job = (CmsScheduledJobInfo)itJobs.next();
         CmsListItem item = this.getList().newItem(job.getId());
         item.set("cn", job.getJobName());
         item.set("cs", job.getClassName());
         items.add(item);
      }

      return items;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("cic");
      iconCol.setName(Messages.get().container("GUI_JOBS_LIST_COL_ICON_0"));
      iconCol.setHelpText(Messages.get().container("GUI_JOBS_LIST_COL_ICON_HELP_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setSorteable(false);
      CmsListDirectAction iconColAction = new CmsListDirectAction("aic");
      iconColAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_ICON_NAME_0"));
      iconColAction.setIconPath("tools/scheduler/buttons/edit.png");
      iconColAction.setEnabled(false);
      iconCol.addDirectAction(iconColAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition delCol = new CmsListColumnDefinition("cd");
      delCol.setName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_COL_DELETE_0"));
      delCol.setHelpText(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_COL_DELETE_HELP_0"));
      delCol.setWidth("20");
      delCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      delCol.setListItemComparator((I_CmsListItemComparator)null);
      CmsListDirectAction delJob = new CmsListDirectAction("ad");
      delJob.setName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_ACTION_DELETE_NAME_0"));
      delJob.setConfirmationMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_ACTION_DELETE_CONF_0"));
      delJob.setIconPath("list/delete.png");
      delJob.setHelpText(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_ACTION_DELETE_HELP_0"));
      delCol.addDirectAction(delJob);
      metadata.addColumn(delCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_COL_NAME_0"));
      nameCol.setWidth("50%");
      nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      nameCol.setListItemComparator(new CmsListItemDefaultComparator());
      metadata.addColumn(nameCol);
      CmsListColumnDefinition classCol = new CmsListColumnDefinition("cs");
      classCol.setName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_COL_CLASS_0"));
      classCol.setWidth("50%");
      classCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      classCol.setListItemComparator(new CmsListItemDefaultComparator());
      metadata.addColumn(classCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails jobsContextInfoDetails = new CmsListItemDetails("dc");
      jobsContextInfoDetails.setAtColumn("cn");
      jobsContextInfoDetails.setVisible(false);
      jobsContextInfoDetails.setShowActionName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_SHOW_CONTEXTINFO_NAME_0"));
      jobsContextInfoDetails.setShowActionHelpText(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_SHOW_CONTEXTINFO_HELP_0"));
      jobsContextInfoDetails.setHideActionName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_HIDE_CONTEXTINFO_NAME_0"));
      jobsContextInfoDetails.setHideActionHelpText(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_HIDE_CONTEXTINFO_HELP_0"));
      CmsContextInfoDetailsFormatter contextFormatter = new CmsContextInfoDetailsFormatter();
      contextFormatter.setUserMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_CONTEXTINFO_USER_0"));
      contextFormatter.setProjectMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_CONTEXTINFO_PROJECT_0"));
      contextFormatter.setLocaleMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_CONTEXTINFO_LOCALE_0"));
      contextFormatter.setRootSiteMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_CONTEXTINFO_ROOTSITE_0"));
      contextFormatter.setEncodingMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_CONTEXTINFO_ENCODING_0"));
      contextFormatter.setRemoteAddrMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_CONTEXTINFO_REMADR_0"));
      contextFormatter.setRequestedURIMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_CONTEXTINFO_REQURI_0"));
      jobsContextInfoDetails.setFormatter(contextFormatter);
      metadata.addItemDetails(jobsContextInfoDetails);
      CmsListItemDetails jobsParameterDetails = new CmsListItemDetails("dp");
      jobsParameterDetails.setAtColumn("cn");
      jobsParameterDetails.setVisible(false);
      jobsParameterDetails.setShowActionName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_SHOW_PARAMETER_NAME_0"));
      jobsParameterDetails.setShowActionHelpText(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_SHOW_PARAMETER_HELP_0"));
      jobsParameterDetails.setHideActionName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_HIDE_PARAMETER_NAME_0"));
      jobsParameterDetails.setHideActionHelpText(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_HIDE_PARAMETER_HELP_0"));
      jobsParameterDetails.setFormatter(new CmsListItemDetailsFormatter(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_DETAIL_PARAMETER_FORMAT_0")));
      metadata.addItemDetails(jobsParameterDetails);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction deleteJobs = new CmsListMultiAction("md");
      deleteJobs.setName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_ACTION_MDELETE_NAME_0"));
      deleteJobs.setConfirmationMessage(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_ACTION_MDELETE_CONF_0"));
      deleteJobs.setIconPath("list/multi_delete.png");
      deleteJobs.setHelpText(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_ACTION_MDELETE_HELP_0"));
      metadata.addMultiAction(deleteJobs);
   }

   protected void validateParamaters() throws Exception {
      if (this.getServer() == null) {
         throw new Exception();
      }
   }

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }
}
