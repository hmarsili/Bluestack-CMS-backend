package org.opencms.ocee.cluster;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.opencms.file.CmsObject;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.I_CmsShellCommands;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;

public class CmsClusterShellCommands implements I_CmsShellCommands {
   private CmsObject m_cms;

   public static void main(String[] args) {
      boolean wrongUsage = false;
      String webInfPath = null;
      String script = null;
      String servletMapping = null;
      String defaultWebApp = null;
      if (args.length > 4) {
         wrongUsage = true;
      } else {
         for(int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.startsWith("-base=")) {
               webInfPath = arg.substring("-base=".length());
            } else if (arg.startsWith("-script=")) {
               script = arg.substring("-script=".length());
            } else if (arg.startsWith("-servletMapping=")) {
               servletMapping = arg.substring("-servletMapping=".length());
            } else if (arg.startsWith("-defaultWebApp=")) {
               defaultWebApp = arg.substring("-defaultWebApp=".length());
            } else {
               System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_WRONG_USAGE_0"));
               wrongUsage = true;
            }
         }
      }

      if (wrongUsage) {
         System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_USAGE_1", CmsShell.class.getName()));
      } else {
         System.out.println("webInfPath: " + webInfPath + ", servletMapping: " + servletMapping + ", defaultWebApp: " + defaultWebApp);
         FileInputStream stream = null;
         if (script != null) {
            try {
               stream = new FileInputStream(script);
            } catch (IOException var9) {
               System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_ERR_SCRIPTFILE_1", script));
            }
         }

         if (stream == null) {
            stream = new FileInputStream(FileDescriptor.in);
         }

         CmsClusterShellCommands clusterCommands = new CmsClusterShellCommands();
         CmsShell shell = new CmsShell(webInfPath, servletMapping, defaultWebApp, "${user}@${project}:${siteroot}|${uri}>", clusterCommands);
         shell.start(stream);
      }

   }

   public void initShellCmsObject(CmsObject cms, CmsShell shell) {
      this.m_cms = cms;
   }

   public void deleteClusterModule(String moduleName) throws Exception {
      I_CmsReport report = new CmsShellReport(this.m_cms.getRequestContext().getLocale());
      CmsObject cms = OpenCms.initCmsObject(this.m_cms);
      OpenCms.getModuleManager().deleteModule(cms, moduleName, false, report);
      List<String> modules = new ArrayList();
      modules.add(moduleName);
      Iterator<?> it = CmsClusterManager.getInstance().getOtherServers().iterator();
      report.println(Messages.get().container("GUI_SHELL_DELETE_MODULE_START_2", moduleName, String.valueOf(CmsClusterManager.getInstance().getOtherServers().size())));

      while(it.hasNext()) {
         CmsClusterServer server = (CmsClusterServer)it.next();
         report.println(Messages.get().container("GUI_SHELL_DELETING_MODULE_1", server.getName()));
         CmsClusterRemoteCmdHelper.deleteModules(cms, server, modules);
      }

      report.println(Messages.get().container("GUI_SHELL_DONE_0"));
   }

   public void importClusterModule(String importFile) throws Exception {
      CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(importFile);
      if (module != null) {
         I_CmsReport report = new CmsShellReport(this.m_cms.getRequestContext().getLocale());
         CmsImportParameters params = new CmsImportParameters(importFile, "/", true);
         OpenCms.getImportExportManager().importData(this.m_cms, report, params);
         CmsObject cms = OpenCms.initCmsObject(this.m_cms);
         Iterator<?> it = CmsClusterManager.getInstance().getOtherServers().iterator();
         report.println(Messages.get().container("GUI_SHELL_IMPORT_MODULE_START_2", importFile, String.valueOf(CmsClusterManager.getInstance().getOtherServers().size())));

         while(it.hasNext()) {
            try {
               CmsClusterServer server = (CmsClusterServer)it.next();
               report.println(Messages.get().container("GUI_SHELL_IMPORTING_MODULE_1", server.getName()));
               CmsClusterRemoteCmdHelper.createModules(cms, server, Collections.singletonList(module));
            } catch (Exception var8) {
               report.addError(var8);
            }
         }

         report.println(Messages.get().container("GUI_SHELL_DONE_0"));
      }
   }

   public void importClusterModuleFromDefault(String importFile) throws Exception {
      String exportPath = OpenCms.getSystemInfo().getPackagesRfsPath();
      String fileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(exportPath + CmsSystemInfo.FOLDER_MODULES + importFile);
      this.importClusterModule(fileName);
   }

   public void shellExit() {
      System.out.println();
      System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_GOODBYE_0"));
   }

   public void shellStart() {
      System.out.println();
      System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_WELCOME_0"));
      System.out.println();
      System.out.println();
      System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_VERSION_1", OpenCms.getSystemInfo().getVersionNumber()));
      String[] copy = org.opencms.main.Messages.COPYRIGHT_BY_ALKACON;

      for(int i = 0; i < copy.length; ++i) {
         System.out.println(copy[i]);
      }

      System.out.println();
      System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_HELP1_0"));
      System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_HELP2_0"));
      System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_HELP3_0"));
      System.out.println(org.opencms.main.Messages.get().getBundle().key("GUI_SHELL_HELP4_0"));
      System.out.println();
      System.out.println();
      System.out.println(Messages.get().getBundle().key("GUI_SHELL_START_0"));
      System.out.println();
      System.out.println();
   }
}
