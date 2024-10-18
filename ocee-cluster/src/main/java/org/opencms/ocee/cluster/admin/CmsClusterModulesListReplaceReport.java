package org.opencms.ocee.cluster.admin;

import java.util.Collections;
import java.util.Iterator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.threads.CmsModuleReplaceThread;
import org.opencms.workplace.tools.modules.CmsModulesListReplaceReport;

public class CmsClusterModulesListReplaceReport extends CmsModulesListReplaceReport {
   protected static final Log LOG = CmsLog.getLog(CmsClusterModulesListReplaceReport.class);

   public CmsClusterModulesListReplaceReport(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterModulesListReplaceReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public I_CmsReportThread initializeThread() {
      String module = this.getParamModule();
      final String modulename = this.getParamModulename();
      String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
      importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importpath + "modules/" + module);
      final CmsModuleReplaceThread replaceThread = new CmsModuleReplaceThread(this.getCms(), modulename, importpath);

      try {
         final CmsObject cms = OpenCms.initCmsObject(this.getCms());
         (new Thread() {
            public synchronized void run() {
               try {
                  this.wait(1000L);
                  ((Thread)replaceThread).join();
               } catch (InterruptedException var4) {
                  if (CmsClusterModulesListReplaceReport.LOG.isErrorEnabled()) {
                     CmsClusterModulesListReplaceReport.LOG.error(var4.getLocalizedMessage(), var4);
                  }
               }

               CmsModule moduleObj = OpenCms.getModuleManager().getModule(modulename);
               if (moduleObj != null) {
                  Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

                  while(it.hasNext()) {
                     CmsClusterServer server = (CmsClusterServer)it.next();
                     CmsClusterRemoteCmdHelper.createModules(cms, server, Collections.singletonList(moduleObj));
                  }

               }
            }
         }).start();
      } catch (CmsException var6) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var6.getLocalizedMessage(), var6);
         }
      }

      return replaceThread;
   }
}
