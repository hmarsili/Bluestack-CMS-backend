package org.opencms.ocee.replication.admin;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListDefaultJsAction;

public class CmsListOpenResourceAction extends A_CmsListDefaultJsAction {
   private CmsObject cmsObject;
   private final String resColumnPathId;

   public CmsListOpenResourceAction(String id, CmsObject cms, String resColumnPathId) {
      super(id);
      this.resColumnPathId = resColumnPathId;
      this.cmsObject = cms;
      this.setName(org.opencms.workplace.list.Messages.get().container("GUI_OPENRESOURCE_ACTION_NAME_0"));
      this.setHelpText(org.opencms.workplace.list.Messages.get().container("GUI_OPENRESOURCE_ACTION_HELP_0"));
   }

   public CmsMessageContainer getHelpText() {
      return this.isEnabled() ? super.getHelpText() : org.opencms.workplace.list.Messages.get().container("GUI_OPENRESOURCE_ACTION_DISABLED_HELP_0");
   }

   public boolean isEnabled() {
      if (this.getItem().get("ch").toString().endsWith("D")) {
         return false;
      } else {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() != null ? super.isEnabled() : false;
      }
   }

   public String jsCode() {
      StringBuffer jsCode = new StringBuffer(256);
      jsCode.append("{ w = screen.availWidth - 50; h = screen.availHeight - 200; workplace = window.open('");
      jsCode.append(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super());
      jsCode.append("', 'preview', 'toolbar = yes, location = yes, directories = no, status = yes, menubar = 1, scrollbars = yes, resizable = yes, left = 20, top = 20, width = '+w+', height = '+h);");
      jsCode.append("if (workplace != null) { workplace.focus(); } }");
      return jsCode.toString();
   }

   public void setCms(CmsObject cms) {
      this.cmsObject = cms;
   }

   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
      String resource = this.getItem().get(this.resColumnPathId).toString();
      CmsProject currentProject = this.cmsObject.getRequestContext().currentProject();

      try {
         CmsSite currentSite;
         try {
            this.cmsObject.getRequestContext().setCurrentProject(this.cmsObject.readProject(CmsProject.ONLINE_PROJECT_ID));
            String secureResource;
            if (!this.cmsObject.existsResource(resource, CmsResourceFilter.DEFAULT)) {
               secureResource = OpenCms.getSiteManager().getSiteRoot(resource);
               if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(secureResource)) {
                  resource = resource.substring(secureResource.length());
               }

               if (!this.cmsObject.existsResource(resource, CmsResourceFilter.DEFAULT)) {
                 return null;
               }
            }

            secureResource = this.cmsObject.readPropertyObject(resource, "secure", true).getValue();
            currentSite = OpenCms.getSiteManager().getCurrentSite(this.cmsObject);
            String uri = OpenCms.getLinkManager().substituteLink(this.cmsObject, resource, currentSite.getSiteRoot());
            String serverPrefix = "";
            if (currentSite.equals(OpenCms.getSiteManager().getDefaultSite())) {
               serverPrefix = OpenCms.getSiteManager().getWorkplaceServer();
            } else if (Boolean.valueOf(secureResource)) {
               serverPrefix = currentSite.getSecureUrl();
            } else {
               serverPrefix = currentSite.getUrl();
            }

            if (!uri.startsWith(serverPrefix)) {
               uri = serverPrefix + uri;
            }

            return uri;
         } catch (CmsException var12) {
           return null;
         }
      } finally {
         this.cmsObject.getRequestContext().setCurrentProject(currentProject);
      }
   }
}
