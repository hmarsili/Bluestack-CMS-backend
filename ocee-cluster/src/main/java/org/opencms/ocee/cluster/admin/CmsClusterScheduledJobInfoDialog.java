package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsContextInfo;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.scheduler.CmsEditScheduledJobInfoDialog;

public class CmsClusterScheduledJobInfoDialog extends CmsEditScheduledJobInfoDialog {
   public static final String PARAM_JOBNAME = "jobname";
   private String m_paramJobname;
   private String m_paramServer;

   public CmsClusterScheduledJobInfoDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterScheduledJobInfoDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      ArrayList errors = new ArrayList();

      try {
         CmsClusterRemoteCmdHelper.writeScheduleJobs(this.getCms(), this.getServer(), Collections.singletonList(this.m_jobInfo));
         if (this.getServer().isWpServer()) {
            Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

            while(it.hasNext()) {
               CmsClusterServer server = (CmsClusterServer)it.next();
               if (this.isNewJob()) {
                  CmsClusterRemoteCmdHelper.createScheduleJobIfMissing(this.getCms(), server, this.m_jobInfo);
               } else {
                  CmsClusterRemoteCmdHelper.overwriteScheduleJobs(this.getCms(), server, this.m_jobInfo);
               }
            }
         }

         Map objects = (Map)this.getSettings().getListObject();
         if (objects != null) {
            objects.remove(CmsClusterSchedulerList.class.getName());
         }
      } catch (Throwable var4) {
         errors.add(var4);
      }

      this.setCommitErrors(errors);
   }

   public String getParamJobname() {
      return this.m_paramJobname;
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public String getServerTime() {
      if (this.m_serverTime == null) {
         long time = CmsClusterRemoteCmdHelper.getSystemMillis(this.getCms(), this.getServer());
         this.m_serverTime = CmsDateUtil.getDateTime(new Date(time), 1, this.getLocale());
      }

      return this.m_serverTime;
   }

   public void setParamJobname(String paramJobname) {
      this.m_paramJobname = paramJobname;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initScheduledJobObject() {
      boolean setActive = false;
      Object o;
      if (!CmsStringUtil.isEmpty(this.getParamAction()) && !"initial".equals(this.getParamAction()) && !"copyjob".equals(this.getParamAction())) {
         o = this.getDialogObject();
      } else if (CmsStringUtil.isNotEmpty(this.getParamJobid())) {
         o = CmsClusterRemoteCmdHelper.getScheduledJob(this.getCms(), this.getServer(), this.getParamJobid());
         setActive = ((CmsScheduledJobInfo)o).isActive();
      } else {
         o = null;
      }

      if (!(o instanceof CmsScheduledJobInfo)) {
         this.m_jobInfo = new CmsScheduledJobInfo();
         this.m_jobInfo.setContextInfo(new CmsContextInfo());
      } else {
         this.m_jobInfo = (CmsScheduledJobInfo)o;
      }

      if (setActive) {
         this.m_jobInfo.setActive(true);
      }

      if ("copyjob".equals(this.getParamAction())) {
         this.m_jobInfo.clearId();
      }

   }

   protected boolean isNewJob() {
      return this.getCurrentToolPath().endsWith("/scheduler/new");
   }

   protected void validateParamaters() throws Exception {
      if (this.getCurrentToolPath().endsWith("/config-all/scheduler/new")) {
         this.setParamServer(CmsClusterManager.getInstance().getWpServer().getName());
      }

      if (this.getServer() == null) {
         throw new Exception();
      } else if (!this.isNewJob()) {
         if (!CmsStringUtil.isNotEmptyOrWhitespaceOnly(this.getParamJobid()) || !CmsClusterRemoteCmdHelper.existsScheduleJob(this.getCms(), this.getServer(), this.getParamJobid())) {
            throw new Exception();
         }
      }
   }

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }
}
