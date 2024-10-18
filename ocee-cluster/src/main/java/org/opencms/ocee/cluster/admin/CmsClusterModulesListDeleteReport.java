package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
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
import org.opencms.workplace.threads.CmsModuleDeleteThread;
import org.opencms.workplace.tools.modules.CmsModulesListDeleteReport;

public class CmsClusterModulesListDeleteReport extends CmsModulesListDeleteReport {
   protected static final Log LOG = CmsLog.getLog(CmsClusterModulesListDeleteReport.class);

   public CmsClusterModulesListDeleteReport(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterModulesListDeleteReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public I_CmsReportThread initializeThread() {
      final List modules = this.extractModuleNames();
      final CmsModuleDeleteThread deleteModuleThread = new CmsModuleDeleteThread(this.getCms(), modules, false);

      try {
         final CmsObject cms = OpenCms.initCmsObject(this.getCms());
         (new Thread() {
            public synchronized void run() {
               try {
                  this.wait(1000L);
                  deleteModuleThread.join();
               } catch (InterruptedException var4) {
                  if (CmsClusterModulesListDeleteReport.LOG.isErrorEnabled()) {
                     CmsClusterModulesListDeleteReport.LOG.error(var4.getLocalizedMessage(), var4);
                  }
               }

               Iterator itModules = modules.iterator();

               while(itModules.hasNext()) {
                  String moduleName = (String)itModules.next();
                  CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
                  if (module != null) {
                     itModules.remove();
                  }
               }

               if (!modules.isEmpty()) {
                  Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

                  while(it.hasNext()) {
                     CmsClusterServer server = (CmsClusterServer)it.next();
                     CmsClusterRemoteCmdHelper.deleteModules(cms, server, modules);
                  }

               }
            }
         }).start();
      } catch (CmsException var4) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var4.getLocalizedMessage(), var4);
         }
      }

      return deleteModuleThread;
   }

   private List extractModuleNames() {
      List modules = new ArrayList();
      StringTokenizer tok = new StringTokenizer(this.getParamModule(), ",");

      while(tok.hasMoreTokens()) {
         String module = tok.nextToken();
         modules.add(module);
      }

      return modules;
   }
}
