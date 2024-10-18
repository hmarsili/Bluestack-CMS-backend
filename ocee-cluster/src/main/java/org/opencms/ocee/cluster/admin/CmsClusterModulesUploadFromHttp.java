package org.opencms.ocee.cluster.admin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.modules.CmsModulesList;
import org.opencms.workplace.tools.modules.CmsModulesUploadFromHttp;

public class CmsClusterModulesUploadFromHttp extends CmsModulesUploadFromHttp {
   protected static final String IMPORT_ACTION_REPORT = "/system/workplace/admin/ocee-cluster/modules/reports/import.jsp";
   protected static final String REPLACE_ACTION_REPORT = "/system/workplace/admin/ocee-cluster/modules/reports/replace.jsp";
   private static final Log LOG = CmsLog.getLog(CmsModulesUploadFromHttp.class);

   public CmsClusterModulesUploadFromHttp(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterModulesUploadFromHttp(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() throws IOException, ServletException {
      try {
         this.copyFileToServer(OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + CmsSystemInfo.FOLDER_MODULES);
      } catch (CmsException var8) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var8.getLocalizedMessage(this.getLocale()), var8);
         }

         this.setException(var8);
         return;
      }

      CmsConfigurationException exception = null;
      CmsModule module = null;

      try {
         String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
         importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importpath + "modules/" + this.getParamImportfile());
         module = CmsModuleImportExportHandler.readModuleFromImport(importpath);
         List dependencies = OpenCms.getModuleManager().checkDependencies(module, 1);
         if (!dependencies.isEmpty()) {
            StringBuffer dep = new StringBuffer(32);

            for(int i = 0; i < dependencies.size(); ++i) {
               CmsModuleDependency dependency = (CmsModuleDependency)dependencies.get(i);
               dep.append("\n - ");
               dep.append(dependency.getName());
               dep.append(" (Version: ");
               dep.append(dependency.getVersion());
               dep.append(")");
            }

            exception = new CmsConfigurationException(Messages.get().container("ERR_ACTION_MODULE_DEPENDENCY_2", this.getParamImportfile(), new String(dep)));
         }
      } catch (CmsConfigurationException var9) {
         exception = var9;
      }

      if (module != null && exception == null) {
         Map objects = (Map)this.getSettings().getListObject();
         if (objects != null) {
            objects.remove(CmsModulesList.class.getName());
         }

         Map param = new HashMap();
         param.put("module", this.getParamImportfile());
         param.put("style", "new");
         param.put("closelink", CmsToolManager.linkForToolPath(this.getJsp(), "/ocee-cluster/config-all/modules"));
         if (OpenCms.getModuleManager().hasModule(module.getName())) {
            param.put("modulename", module.getName());
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/modules/reports/replace.jsp", param);
         } else {
            param.put("modulename", module.getName());
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/modules/reports/import.jsp", param);
         }
      } else if (exception != null) {
         if (LOG.isErrorEnabled()) {
            LOG.error(exception.getLocalizedMessage(this.getLocale()), exception);
         }

         throw new CmsRuntimeException(exception.getMessageContainer(), exception);
      }

   }

   public String getDialogReturnUri() {
      return "/system/workplace/admin/ocee-cluster/modules/modules_import.jsp";
   }
}
