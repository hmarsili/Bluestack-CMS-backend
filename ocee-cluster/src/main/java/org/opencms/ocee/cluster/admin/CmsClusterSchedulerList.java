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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.tools.scheduler.CmsSchedulerList;

public class CmsClusterSchedulerList extends CmsSchedulerList {
   private String m_paramServer;

   public CmsClusterSchedulerList(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterSchedulerList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      ArrayList ids;
      Iterator itItems;
      CmsListItem listItem;
      if (this.getParamListAction().equals("md")) {
         ids = new ArrayList();
         itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            listItem = (CmsListItem)itItems.next();
            ids.add(listItem.getId());
         }

         CmsClusterRemoteCmdHelper.deleteScheduleJobs(this.getCms(), this.getServer(), ids);
      } else if (!this.getParamListAction().equals("ma") && !this.getParamListAction().equals("mc")) {
         this.throwListUnsupportedActionException();
      } else {
         ids = new ArrayList();
         itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            listItem = (CmsListItem)itItems.next();
            ids.add(listItem.getId());
         }

         boolean activate = this.getParamListAction().equals("ma");
         CmsClusterRemoteCmdHelper.activateScheduleJobs(this.getCms(), this.getServer(), ids, activate);
      }

      this.listSave();
   }

   public void displayEmpty() throws IOException {
      if (!this.isForwarded()) {
         JspWriter out = this.getJsp().getJspContext().getOut();
         out.print(this.defaultActionHtmlStart());
         out.print(this.defaultActionHtmlEnd());
      }
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
            params.put("server", this.getParamServer());
            params.put("action", "copyjob");
            this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/new", params);
         } else if (this.getParamListAction().equals("aa")) {
            jobId = this.getSelectedItem().getId();
            CmsClusterRemoteCmdHelper.activateScheduleJobs(this.getCms(), this.getServer(), Collections.singletonList(jobId), true);
            this.refreshList();
         } else if (this.getParamListAction().equals("at")) {
            jobId = this.getSelectedItem().getId();
            CmsClusterRemoteCmdHelper.activateScheduleJobs(this.getCms(), this.getServer(), Collections.singletonList(jobId), false);
            this.refreshList();
         } else if (this.getParamListAction().equals("ad")) {
            jobId = this.getSelectedItem().getId();
            CmsClusterRemoteCmdHelper.deleteScheduleJobs(this.getCms(), this.getServer(), Collections.singletonList(jobId));
         } else {
            this.throwListUnsupportedActionException();
         }
      } else {
         jobId = this.getSelectedItem().getId();
         params = new HashMap();
         params.put("jobid", jobId);
         params.put("jobname", this.getSelectedItem().get("cn"));
         params.put("server", this.getParamServer());
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/edit", params);
      }

      this.listSave();
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected List getListItems() {
      List items = new ArrayList();
      Iterator itJobs = CmsClusterRemoteCmdHelper.getScheduledJobs(this.getCms(), this.getServer()).iterator();

      while(itJobs.hasNext()) {
         CmsScheduledJobInfo job = (CmsScheduledJobInfo)itJobs.next();
         List times = CmsClusterRemoteCmdHelper.getExecutionTimesForJob(this.getCms(), this.getServer(), job.getId());
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
         items.add(item);
      }

      return items;
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

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (this.getServer() == null) {
         throw new Exception();
      }
   }

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }
}
