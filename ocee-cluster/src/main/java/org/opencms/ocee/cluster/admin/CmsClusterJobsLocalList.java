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
import org.opencms.main.OpenCms;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDefaultComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.scheduler.CmsContextInfoDetailsFormatter;

public class CmsClusterJobsLocalList extends A_CmsListDialog {
   public static final String LIST_ACTION_COPY = "ac";
   public static final String LIST_ACTION_ICON = "aic";
   public static final String LIST_COLUMN_COPY = "cc";
   public static final String LIST_COLUMN_ICON = "cic";
   public static final String LIST_DEFACTION_COPY = "dac";
   public static final String LIST_ID = "lcjl";
   public static final String LIST_MACTION_COPY = "mac";
   private String m_paramServer;

   public CmsClusterJobsLocalList(CmsJspActionElement jsp) {
      this(jsp, "lcjl");
   }

   public CmsClusterJobsLocalList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsClusterJobsLocalList(CmsJspActionElement jsp, String listId) {
      super(jsp, listId, Messages.get().container("GUI_CLUSTER_JOBS_LOCAL_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      if (this.getParamListAction().equals("mac")) {
         List jobs = new ArrayList();
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(listItem.getId()).clone();
            job.clearId();
            jobs.add(job);
         }

         CmsClusterRemoteCmdHelper.writeScheduleJobs(this.getCms(), this.getServer(), jobs);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   public void executeListSingleActions() throws CmsRuntimeException {
      if (!this.getParamListAction().equals("ac") && !this.getParamListAction().equals("dac")) {
         this.throwListUnsupportedActionException();
      } else {
         CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(this.getSelectedItem().getId()).clone();
         job.clearId();
         CmsClusterRemoteCmdHelper.writeScheduleJobs(this.getCms(), this.getServer(), Collections.singletonList(job));
      }

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
            CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(item.getId());
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
      Iterator itJobs = OpenCms.getScheduleManager().getJobs().iterator();

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
      CmsListColumnDefinition copyCol = new CmsListColumnDefinition("cc");
      copyCol.setName(Messages.get().container("GUI_JOBS_LIST_COL_COPY_0"));
      copyCol.setHelpText(Messages.get().container("GUI_JOBS_LIST_COL_COPY_HELP_0"));
      copyCol.setWidth("20");
      copyCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      copyCol.setSorteable(false);
      metadata.addColumn(copyCol);
      CmsListDirectAction copyAction = new CmsListDirectAction("ac");
      copyAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_NAME_0"));
      copyAction.setHelpText(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_HELP_0"));
      copyAction.setConfirmationMessage(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_CONF_0"));
      copyAction.setIconPath("list/add.png");
      copyCol.addDirectAction(copyAction);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(org.opencms.workplace.tools.scheduler.Messages.get().container("GUI_JOBS_LIST_COL_NAME_0"));
      nameCol.setWidth("50%");
      nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
      nameCol.setListItemComparator(new CmsListItemDefaultComparator());
      CmsListDefaultAction nameColAction = new CmsListDefaultAction("dac");
      nameColAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_NAME_0"));
      nameColAction.setHelpText(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_HELP_0"));
      nameColAction.setConfirmationMessage(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_CONF_0"));
      nameCol.addDefaultAction(nameColAction);
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
      CmsListMultiAction copyJob = new CmsListMultiAction("mac");
      copyJob.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_NAME_0"));
      copyJob.setHelpText(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_HELP_0"));
      copyJob.setHelpText(Messages.get().container("GUI_JOBS_LIST_ACTION_COPY_CONF_0"));
      copyJob.setIconPath("tools/scheduler/buttons/copy.png");
      metadata.addMultiAction(copyJob);
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
