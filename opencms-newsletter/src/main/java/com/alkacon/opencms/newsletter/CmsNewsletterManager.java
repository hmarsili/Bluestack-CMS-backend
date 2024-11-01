package com.alkacon.opencms.newsletter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;

public class CmsNewsletterManager extends A_CmsModuleAction {
   public static final String MODULE_NAME = CmsNewsletterManager.class.getPackage().getName();
   public static final String MODULE_PARAM_CLASS_MAILDATA = "class_maildata";
   public static final String MODULE_PARAM_PASSWORD_USER = "user_password";
   public static final String MODULE_PARAM_PROJECT_NAME = "project_name";
   public static final String NEWSLETTER_OU_NAMEPREFIX = "nl_";
   public static final Pattern PATTERN_VALIDATION_EMAIL = Pattern.compile("(\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,4})");
   public static final String PROPERTY_NEWSLETTER_DATA = "newsletter";
   public static final String USER_ADDITIONALINFO_ACTIVE = "AlkNewsletter_ActiveUser:";
   public static final String USER_ADDITIONALINFO_TODELETE = "AlkNewsletter_UserToDelete:";
   private static final String PASSWORD_USER = "Uw82-Qn!";
   private CmsObject m_adminCms;

   public static I_CmsNewsletterMailData getMailData() throws Exception {
      String className = OpenCms.getModuleManager().getModule(MODULE_NAME).getParameter("class_maildata", CmsNewsletterMailData.class.getName());
      return (I_CmsNewsletterMailData)Class.forName(className).newInstance();
   }

   public static I_CmsNewsletterMailData getMailData(CmsJspActionElement jsp, CmsGroup group, String fileName) throws Exception {
      I_CmsNewsletterMailData result = getMailData();
      result.initialize(jsp, group, fileName);
      return result;
   }

   public static String getMailDataResourceTypeName() throws Exception {
      return getMailData().getResourceTypeName();
   }

   public static List getOrgUnits(CmsObject cms) throws CmsException {
      List ous = OpenCms.getRoleManager().getOrgUnitsForRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(""), true);
      Iterator it = ous.iterator();

      while(it.hasNext()) {
         CmsOrganizationalUnit ou = (CmsOrganizationalUnit)it.next();
         if (!ou.getSimpleName().startsWith("nl_")) {
            it.remove();
         }
      }

      return ous;
   }

   public static String getPassword() {
      return OpenCms.getModuleManager().getModule(MODULE_NAME).getParameter("user_password", "Uw82-Qn!");
   }

   public static boolean isActiveUser(CmsUser user, String groupName) {
      Boolean active = (Boolean)user.getAdditionalInfo("AlkNewsletter_ActiveUser:" + groupName);
      return (active != null && active || active == null) && user.isEnabled();
   }

   public static boolean isValidEmail(String email) {
      return PATTERN_VALIDATION_EMAIL.matcher(email).matches();
   }

   public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {
      this.m_adminCms = adminCms;
   }

   protected boolean activateNewsletterUser(String email, String groupName) {
      try {
         CmsUser user = this.getAdminCms().readUser(this.getAdminCms().readGroup(groupName).getOuFqn() + email);
         if (user.getAdditionalInfo().get("AlkNewsletter_ActiveUser:") != null) {
            user.deleteAdditionalInfo("AlkNewsletter_ActiveUser:");
         }

         user.setAdditionalInfo("AlkNewsletter_ActiveUser:" + groupName, true);
         this.getAdminCms().writeUser(user);
         return true;
      } catch (CmsException var4) {
         return false;
      }
   }

   protected CmsUser createNewsletterUser(String email, String groupName, boolean activate) {
      CmsUser user = null;

      try {
         String ouFqn = this.getAdminCms().readGroup(groupName).getOuFqn();

         try {
            user = this.getAdminCms().readUser(ouFqn + email);
         } catch (CmsException var7) {
         }

         if (user == null) {
            user = this.getAdminCms().createUser(ouFqn + email, getPassword(), "Alkacon OpenCms Newsletter", Collections.EMPTY_MAP);
            user.setEmail(email);
            if (!activate) {
               user.setAdditionalInfo("AlkNewsletter_ActiveUser:", Boolean.FALSE);
            }
         } else {
            Object o = user.getAdditionalInfo("AlkNewsletter_ActiveUser:" + groupName);
            if (o != null) {
               return null;
            }
         }

         user.setAdditionalInfo("AlkNewsletter_ActiveUser:" + groupName, activate);
         if (activate && user.getAdditionalInfo().get("AlkNewsletter_ActiveUser:") != null) {
            user.deleteAdditionalInfo("AlkNewsletter_ActiveUser:");
         }

         this.getAdminCms().writeUser(user);
         this.getAdminCms().addUserToGroup(user.getName(), groupName);
      } catch (CmsException var8) {
      }

      return user;
   }

   protected boolean deleteNewsletterUser(String email, String groupName, boolean checkDeleteFlag) {
      try {
         String ouFqn = this.getAdminCms().readGroup(groupName).getOuFqn();
         CmsUser user = this.getAdminCms().readUser(ouFqn + email);
         boolean isToDelete = !checkDeleteFlag || (Boolean)user.getAdditionalInfo("AlkNewsletter_UserToDelete:" + groupName);
         if (isToDelete) {
            CmsObject cms = OpenCms.initCmsObject(this.getAdminCms());
            String projectName = OpenCms.getModuleManager().getModule(MODULE_NAME).getParameter("project_name", "Offline");
            CmsProject project = cms.readProject(projectName);
            cms.getRequestContext().setCurrentProject(project);
            cms.removeUserFromGroup(user.getName(), groupName);
            if (cms.getGroupsOfUser(user.getName(), true).size() < 1) {
               cms.deleteUser(user.getName());
            } else {
               user.getAdditionalInfo().remove("AlkNewsletter_UserToDelete:" + groupName);
               user.getAdditionalInfo().remove("AlkNewsletter_ActiveUser:" + groupName);
               cms.writeUser(user);
            }

            return true;
         }
      } catch (CmsException var10) {
      }

      return false;
   }

   protected boolean existsNewsletterUser(String email, String groupName) {
      try {
         String ouFqn = this.getAdminCms().readGroup(groupName).getOuFqn();
         CmsUser user = this.getAdminCms().readUser(ouFqn + email);
         CmsGroup group = this.getAdminCms().readGroup(groupName);
         return this.getAdminCms().getGroupsOfUser(user.getName(), true).contains(group);
      } catch (CmsException var6) {
         return false;
      }
   }

   protected boolean markToDeleteNewsletterUser(String email, String groupName) {
      try {
         String ouFqn = this.getAdminCms().readGroup(groupName).getOuFqn();
         CmsUser user = this.getAdminCms().readUser(ouFqn + email);
         user.setAdditionalInfo("AlkNewsletter_UserToDelete:" + groupName, true);
         this.getAdminCms().writeUser(user);
         return true;
      } catch (CmsException var5) {
         return false;
      }
   }

   private CmsObject getAdminCms() {
      return this.m_adminCms;
   }
}
