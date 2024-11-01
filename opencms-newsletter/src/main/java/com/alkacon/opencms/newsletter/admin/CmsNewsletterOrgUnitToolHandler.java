package com.alkacon.opencms.newsletter.admin;

import java.util.Iterator;
import java.util.List;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsDefaultToolHandler;

public class CmsNewsletterOrgUnitToolHandler extends CmsDefaultToolHandler {
   private static final String DELETE_FILE = "/system/workplace/admin/newsletter/orgunit_delete.jsp";

   public String getDisabledHelpText() {
      return this.getLink().equals("/system/workplace/admin/newsletter/orgunit_delete.jsp") ? "${key.GUI_ALK_NEWSLETTER_ORGUNIT_ADMIN_TOOL_DISABLED_DELETE_HELP_0}" : super.getDisabledHelpText();
   }

   public boolean isEnabled(CmsWorkplace wp) {
      if (this.getLink().equals("/system/workplace/admin/newsletter/orgunit_delete.jsp")) {
         String ouFqn = wp.getJsp().getRequest().getParameter("oufqn");
         if (ouFqn == null) {
            ouFqn = wp.getCms().getRequestContext().getOuFqn();
         }

         try {
            List childOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(wp.getCms(), ouFqn, false);
            Iterator i = childOus.iterator();

            while(i.hasNext()) {
               CmsOrganizationalUnit unit = (CmsOrganizationalUnit)i.next();
               if (unit.getName().endsWith("nl_")) {
                  ouFqn = unit.getName();
               }
            }

            if (OpenCms.getOrgUnitManager().getUsers(wp.getCms(), ouFqn, true).size() > 0) {
               return false;
            }

            if (OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true).size() > 0) {
               List groups = OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true);
               Iterator itGroups = groups.iterator();

               while(itGroups.hasNext()) {
                  CmsGroup group = (CmsGroup)itGroups.next();
                  if (!OpenCms.getDefaultUsers().isDefaultGroup(group.getName())) {
                     return false;
                  }
               }
            }

            if (OpenCms.getOrgUnitManager().getOrganizationalUnits(wp.getCms(), ouFqn, true).size() > 0) {
               return false;
            }
         } catch (CmsException var8) {
         }
      }

      return true;
   }

   public boolean isVisible(CmsWorkplace wp) {
      CmsObject cms = wp.getCms();
      String ouFqn = wp.getJsp().getRequest().getParameter("oufqn");
      if (ouFqn == null) {
         ouFqn = cms.getRequestContext().getOuFqn();
      }

      return !this.getLink().equals("/system/workplace/admin/newsletter/orgunit_delete.jsp") && ouFqn != null && ouFqn.indexOf("nl_") == -1 ? OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER) : false;
   }
}
