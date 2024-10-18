package org.opencms.ocee.vfsdoctor.plugins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.util.CmsUUID;

public class CmsVfsTempFlagPlugin extends A_CmsVfsDoctorPlugin {
   public CmsVfsTempFlagPlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_TEMPFLAG_DESC_0");
   }

   public String getName() {
      return "tempflag";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_TEMPFLAG_NAME_0");
   }

   public int recover() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_RECOVER_OFFLINE_0"));
      int offline = this.recover(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
      if (offline == 0) {
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_NOTHING_TORECOVER_0"));
      }

      return offline;
   }

   public int validate() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_VALIDATE_OFFLINE_0"));
      return this.validate(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
   }

   protected int recover(CmsUUID projectId) {
      List resources = this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      if (resources.isEmpty()) {
         return 0;
      } else {
         int nofix = 0;
         CmsProject oldProj = this.getCms().getRequestContext().currentProject();
         CmsProject proj = null;
         CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
         Iterator itRes = resources.iterator();

         try {
            proj = this.getCms().createProject("VfsDoctor - TempFlag Plugin", "Temporary project for publishing of changes", OpenCms.getDefaultUsers().getGroupAdministrators(), OpenCms.getDefaultUsers().getGroupAdministrators());
            this.getCms().getRequestContext().setCurrentProject(proj);

            while(itRes.hasNext()) {
               CmsResource res = (CmsResource)itRes.next();
               int flags = res.getFlags();
               if ((flags & 1024) == 1024) {
                  flags ^= 1024;
               }

               try {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_TEMPFLAG_REC1_1", new Object[]{res.getRootPath()}));
                  this.getCms().lockResource(res.getRootPath());
                  this.getCms().chflags(res.getRootPath(), flags);
                  this.getCms().unlockResource(res.getRootPath());
                  this.getCms().copyResourceToProject(res.getRootPath());
               } catch (CmsException var17) {
                  this.writeError(var17);
                  ++nofix;
               }
            }

            this.getCms().unlockProject(proj.getUuid());
            OpenCms.getPublishManager().publishProject(this.getCms(), this.getReport());
         } catch (CmsException var18) {
            this.writeError(var18);
         } finally {
            if (oldProj != null) {
               this.getCms().getRequestContext().setCurrentProject(oldProj);
            }

         }

         if (proj != null) {
            try {
               this.getCms().deleteProject(proj.getUuid());
            } catch (CmsException var16) {
               this.writeError(var16);
            }
         }

         if (nofix > 0) {
            this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_TEMPFLAG_REC2_1", new Object[]{new Integer(nofix)}));
         }

         return resources.size() - nofix;
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      List resources = this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      Iterator itRes = resources.iterator();

      while(itRes.hasNext()) {
         CmsResource res = (CmsResource)itRes.next();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_TEMPFLAG_VAL1_1", new Object[]{res.getRootPath()}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_TEMPFLAG_VAL2_1", new Object[]{new Integer(resources.size())}));
      return resources.size();
   }

   private List Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new() {
      String storedSiteRoot = this.getCms().getRequestContext().getSiteRoot();
      ArrayList ret = new ArrayList();

      try {
         this.getCms().getRequestContext().setSiteRoot("/");
         ret.addAll(this.getCms().readResources("/", CmsResourceFilter.ALL.addRequireFlags(1024), true));
         Iterator itRes = ret.iterator();

         while(itRes.hasNext()) {
            CmsResource res = (CmsResource)itRes.next();
            if (res.getName().startsWith("~")) {
               itRes.remove();
            }
         }
      } catch (Exception var8) {
         this.writeError(var8);
      } finally {
         this.getCms().getRequestContext().setSiteRoot(storedSiteRoot);
      }

      return ret;
   }
}
