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
import org.opencms.workplace.threads.CmsDatabaseImportThread;
import org.opencms.workplace.tools.modules.CmsModulesListImportReport;

public class CmsClusterModulesListImportReport extends CmsModulesListImportReport {
   protected static final Log LOG = CmsLog.getLog(CmsClusterModulesListImportReport.class);
   private String m_paramModulename;

   public CmsClusterModulesListImportReport(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterModulesListImportReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public String getParamModulename() {
      return this.m_paramModulename;
   }

   public I_CmsReportThread initializeThread() {
      String modulezip = this.getParamModule();
      String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
      importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importpath + "modules/" + modulezip);
      final String modulename = this.getParamModulename();
      final CmsDatabaseImportThread importThread = new CmsDatabaseImportThread(this.getCms(), importpath, true);

      try {
         final CmsObject cms = OpenCms.initCmsObject(this.getCms());
         (new Thread() {
            public synchronized void run() {
               try {
                  this.wait(1000L);
                  ((Thread)importThread).join();
               } catch (InterruptedException var4) {
                  if (CmsClusterModulesListImportReport.LOG.isErrorEnabled()) {
                     CmsClusterModulesListImportReport.LOG.error(var4.getLocalizedMessage(), var4);
                  }
               }

               CmsModule module = OpenCms.getModuleManager().getModule(modulename);
               if (module != null) {
                  Iterator it = CmsClusterManager.getInstance().getOtherServers().iterator();

                  while(it.hasNext()) {
                     CmsClusterServer server = (CmsClusterServer)it.next();
                     CmsClusterRemoteCmdHelper.createModules(cms, server, Collections.singletonList(module));
                  }

               }
            }
         }).start();
      } catch (CmsException var6) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var6.getLocalizedMessage(), var6);
         }
      }

      return importThread;
   }

   public void setParamModulename(String paramModulename) {
      this.m_paramModulename = paramModulename;
   }
}
