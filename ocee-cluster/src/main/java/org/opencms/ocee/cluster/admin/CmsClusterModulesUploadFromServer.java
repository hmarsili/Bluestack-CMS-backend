package org.opencms.ocee.cluster.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.modules.CmsModulesList;
import org.opencms.workplace.tools.modules.CmsModulesUploadFromServer;

public class CmsClusterModulesUploadFromServer extends CmsModulesUploadFromServer {
   public CmsClusterModulesUploadFromServer(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterModulesUploadFromServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() throws IOException, ServletException {
      List errors = new ArrayList();
      CmsModule module = null;

      try {
         String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
         importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importpath + "modules/" + this.getModuleupload());
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

            errors.add(new CmsRuntimeException(Messages.get().container("ERR_ACTION_MODULE_DEPENDENCY_2", this.getModuleupload(), new String(dep))));
         }
      } catch (CmsConfigurationException var8) {
         errors.add(new CmsRuntimeException(Messages.get().container("ERR_ACTION_MODULE_UPLOAD_1", this.getModuleupload()), var8));
      }

      if (module != null && errors.isEmpty()) {
         Map objects = (Map)this.getSettings().getListObject();
         if (objects != null) {
            objects.remove(CmsModulesList.class.getName());
         }

         Map param = new HashMap();
         param.put("module", this.getModuleupload());
         param.put("style", "new");
         param.put("closelink", CmsToolManager.linkForToolPath(this.getJsp(), "/ocee-cluster/config-all/modules"));
         if (OpenCms.getModuleManager().hasModule(module.getName())) {
            param.put("modulename", module.getName());
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/modules/reports/replace.jsp", param);
         } else {
            param.put("modulename", module.getName());
            this.getToolManager().jspForwardPage(this, "/system/workplace/admin/ocee-cluster/modules/reports/import.jsp", param);
         }
      }

      this.setCommitErrors(errors);
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }
}
