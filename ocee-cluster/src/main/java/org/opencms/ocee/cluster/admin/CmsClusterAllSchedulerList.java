package org.opencms.ocee.cluster.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.tools.scheduler.CmsSchedulerList;

public class CmsClusterAllSchedulerList extends CmsSchedulerList {
   public static final String LIST_COLUMN_SERVER = "csv";

   public CmsClusterAllSchedulerList(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterAllSchedulerList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void displayEmpty() throws IOException {
      if (!this.isForwarded()) {
         JspWriter out = this.getJsp().getJspContext().getOut();
         out.print(this.defaultActionHtmlStart());
         out.print(this.defaultActionHtmlEnd());
      }
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      if (this.getParamListAction().equals("md")) {
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            CmsClusterRemoteCmdHelper.deleteScheduleJobs(this.getCms(), this.getServer((String)listItem.get("csv")), Collections.singletonList(listItem.getId()));
         }
      } else if (!this.getParamListAction().equals("ma") && !this.getParamListAction().equals("mc")) {
         this.throwListUnsupportedActionException();
      } else {
         boolean activate = this.getParamListAction().equals("ma");
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            CmsClusterRemoteCmdHelper.activateScheduleJobs(this.getCms(), this.getServer((String)listItem.get("csv")), Collections.singletonList(listItem.getId()), activate);
         }
      }

      this.listSave();
   }

   public void executeListSingleActions() throws IOException, ServletException {
      String jobId;
      HashMap params;
      if (!this.getParamListAction().equals("ae") && !this.getParamListAction().equals("de")) {
         if (this.getParamListAction().equals("ac")) {
            jobId = this.getSelectedItem().getId();
            params = new HashMap();
            params.put("jobid", jobId);
            params.put("jobname", this.getSelectedItem().get("cn"));
            params.put("server", this.getSelectedItem().get("csv"));
            params.put("action", "copyjob");
            this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/new", params);
         } else if (this.getParamListAction().equals("aa")) {
            jobId = this.getSelectedItem().getId();
            CmsClusterRemoteCmdHelper.activateScheduleJobs(this.getCms(), this.getServer((String)this.getSelectedItem().get("csv")), Collections.singletonList(jobId), true);
            this.refreshList();
         } else if (this.getParamListAction().equals("at")) {
            jobId = this.getSelectedItem().getId();
            CmsClusterRemoteCmdHelper.activateScheduleJobs(this.getCms(), this.getServer((String)this.getSelectedItem().get("csv")), Collections.singletonList(jobId), false);
            this.refreshList();
         } else if (this.getParamListAction().equals("ad")) {
            jobId = this.getSelectedItem().getId();
            CmsClusterRemoteCmdHelper.deleteScheduleJobs(this.getCms(), this.getServer((String)this.getSelectedItem().get("csv")), Collections.singletonList(jobId));
         } else {
            this.throwListUnsupportedActionException();
         }
      } else {
         jobId = this.getSelectedItem().getId();
         params = new HashMap();
         params.put("jobid", jobId);
         params.put("jobname", this.getSelectedItem().get("cn"));
         params.put("server", this.getSelectedItem().get("csv"));
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/edit", params);
      }

      this.listSave();
   }

   protected void fillDetails(String detailId) {
      Iterator i = this.getList().getAllContent().iterator();

      while(true) {
         while(i.hasNext()) {
            CmsListItem item = (CmsListItem)i.next();
            CmsScheduledJobInfo job = CmsClusterRemoteCmdHelper.getScheduledJob(this.getCms(), this.getServer((String)item.get("csv")), item.getId());
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
      Iterator itLocaleJobs = OpenCms.getScheduleManager().getJobs().iterator();

      while(itLocaleJobs.hasNext()) {
         CmsScheduledJobInfo job = (CmsScheduledJobInfo)itLocaleJobs.next();
         CmsListItem item = this.getList().newItem(job.getId());
         item.set("cn", job.getJobName());
         item.set("cs", job.getClassName());
         item.set("cl", job.getExecutionTimePrevious());
         item.set("cx", job.getExecutionTimeNext());
         item.set("cac", job.isActive());
         item.set("csv", CmsClusterManager.getInstance().getThisServer().getName());
         items.add(item);
      }

      Iterator itServers = CmsClusterManager.getInstance().getOtherServers().iterator();

      while(itServers.hasNext()) {
         CmsClusterServer server = (CmsClusterServer)itServers.next();
         Iterator itJobs = CmsClusterRemoteCmdHelper.getScheduledJobs(this.getCms(), server).iterator();

         while(itJobs.hasNext()) {
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)itJobs.next();
            List times = CmsClusterRemoteCmdHelper.getExecutionTimesForJob(this.getCms(), server, job.getId());
            CmsListItem item = this.getList().newItem(job.getId());
            item.set("cn", job.getJobName());
            item.set("cs", job.getClassName());
            Long prev = (Long)times.get(0);
            if (prev == 0L) {
               prev = null;
            }

            item.set("cl", prev);
            Long next = (Long)times.get(1);
            if (next == 0L) {
               next = null;
            }

            item.set("cx", next);
            item.set("cac", job.isActive());
            item.set("csv", server.getName());
            items.add(item);
         }
      }

      return items;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setListItemComparator(new CmsListItemActionIconComparator());
      CmsListDirectAction iconAction = new CmsListDirectAction("ai") {
         public String getIconPath() {
            return CmsClusterManager.getInstance().getWpServer().getName().equals(this.getItem().get("csv")) ? "tools/ocee-cluster/buttons/wpserver.png" : "tools/ocee-cluster/buttons/server.png";
         }

         public CmsMessageContainer getName() {
            return CmsClusterManager.getInstance().getWpServer().getName().equals(this.getItem().get("csv")) ? Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_WP_NAME_0") : Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_NAME_0");
         }
      };
      iconAction.setHelpText(Messages.get().container("GUI_CLUSTER_SERVERS_LIST_ACTION_ICON_HELP_0"));
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      super.setColumns(metadata);
      CmsListColumnDefinition serverCol = new CmsListColumnDefinition("csv");
      serverCol.setName(Messages.get().container("GUI_CLUSTER_CONFIG_LIST_COLS_SERVER_0"));
      serverCol.setWidth("10%");
      metadata.addColumn(serverCol);
   }

   private CmsClusterServer getServer(String serverName) {
      return CmsClusterManager.getInstance().getServer(serverName);
   }
}
