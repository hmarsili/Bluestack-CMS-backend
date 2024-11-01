package com.alkacon.opencms.newsletter.admin;

import com.alkacon.opencms.newsletter.CmsNewsletterManager;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListSearchAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

public class CmsOrgUnitsAdminList extends org.opencms.workplace.tools.accounts.CmsOrgUnitsAdminList {
   public static final String PATH_NL_BUTTONS = "tools/newsletter/buttons/";

   public CmsOrgUnitsAdminList(CmsJspActionElement jsp) {
      super(jsp);
      this.getList().setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_NAME_0"));
   }

   public CmsOrgUnitsAdminList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public String getGroupIcon() {
      return "tools/newsletter/buttons/mailinglist.png";
   }

   public String getOverviewIcon() {
      return "tools/newsletter/buttons/newsletter_overview.png";
   }

   public String getUserIcon() {
      return "tools/newsletter/buttons/subscriber.png";
   }

   public boolean hasMoreAdminOUs() throws CmsException {
      boolean result = super.hasMoreAdminOUs();
      Iterator it = CmsNewsletterManager.getOrgUnits(this.getCms()).iterator();

      while(it.hasNext()) {
         CmsOrganizationalUnit ou = (CmsOrganizationalUnit)it.next();
         String groupName = ou.getName() + OpenCms.getDefaultUsers().getGroupUsers();

         try {
            this.getCms().readGroup(groupName);
            CmsObject cms = OpenCms.initCmsObject(this.getCms());
            String projectName = OpenCms.getModuleManager().getModule(CmsNewsletterManager.MODULE_NAME).getParameter("project_name", "Offline");
            CmsProject project = cms.readProject(projectName);
            cms.getRequestContext().setCurrentProject(project);
            this.getCms().deleteGroup(groupName);
         } catch (Exception var8) {
         }
      }

      return result;
   }

   protected String getForwardToolPath() {
      return "/newsletter/orgunit";
   }

   protected String getGroupsToolPath() {
      return this.getCurrentToolPath() + "/orgunit/mailinglists";
   }

   protected List getOrgUnits() throws CmsException {
      return CmsNewsletterManager.getOrgUnits(this.getCms());
   }

   protected String getUsersToolPath() {
      return this.getCurrentToolPath() + "/orgunit/subscribers";
   }

   protected void setColumns(CmsListMetadata metadata) {
      super.setColumns(metadata);
      CmsListColumnDefinition overviewCol = metadata.getColumnDefinition("co");
      overviewCol.setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_OVERVIEW_0"));
      overviewCol.setHelpText(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_OVERVIEW_HELP_0"));
      overviewCol.removeDirectAction("ao");
      I_CmsListDirectAction overviewAction = new CmsListDirectAction("ao");
      overviewAction.setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_ACTION_OVERVIEW_NAME_0"));
      overviewAction.setHelpText(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_OVERVIEW_HELP_0"));
      overviewAction.setIconPath(this.getOverviewIcon());
      overviewCol.addDirectAction(overviewAction);
      CmsListColumnDefinition subscribersCol = metadata.getColumnDefinition("cu");
      subscribersCol.setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_USER_0"));
      subscribersCol.setHelpText(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_USER_HELP_0"));
      I_CmsListDirectAction subscribersAction = subscribersCol.getDirectAction("au");
      subscribersAction.setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_ACTION_USER_NAME_0"));
      subscribersAction.setHelpText(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_USER_HELP_0"));
      CmsListColumnDefinition mailinglistsCol = metadata.getColumnDefinition("cg");
      mailinglistsCol.setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_GROUP_0"));
      mailinglistsCol.setHelpText(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_GROUP_HELP_0"));
      I_CmsListDirectAction mailinglistsAction = mailinglistsCol.getDirectAction("ag");
      mailinglistsAction.setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_ACTION_GROUP_NAME_0"));
      mailinglistsAction.setHelpText(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_GROUP_HELP_0"));
      CmsListColumnDefinition descriptionCol = metadata.getColumnDefinition("cb");
      CmsListDefaultAction defAction = descriptionCol.getDefaultAction("do");
      defAction.setName(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_DEFACTION_OVERVIEW_NAME_0"));
      defAction.setHelpText(Messages.get().container("GUI_ALK_NEWSLETTER_ORGUNITS_LIST_COLS_OVERVIEW_HELP_0"));
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails mailinglistsDetails = new CmsListItemDetails("dg");
      mailinglistsDetails.setAtColumn("cb");
      mailinglistsDetails.setVisible(false);
      mailinglistsDetails.setShowActionName(Messages.get().container("GUI_ORGUNITS_DETAIL_SHOW_ALK_MAILINGLISTS_NAME_0"));
      mailinglistsDetails.setShowActionHelpText(Messages.get().container("GUI_ORGUNITS_DETAIL_SHOW_ALK_MAILINGLISTS_HELP_0"));
      mailinglistsDetails.setHideActionName(Messages.get().container("GUI_ORGUNITS_DETAIL_HIDE_ALK_MAILINGLISTS_NAME_0"));
      mailinglistsDetails.setHideActionHelpText(Messages.get().container("GUI_ORGUNITS_DETAIL_HIDE_ALK_MAILINGLISTS_HELP_0"));
      mailinglistsDetails.setName(Messages.get().container("GUI_ORGUNITS_DETAIL_ALK_MAILINGLISTS_NAME_0"));
      mailinglistsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_ORGUNITS_DETAIL_ALK_MAILINGLISTS_NAME_0")));
      metadata.addItemDetails(mailinglistsDetails);
      CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition("cn"));
      searchAction.addColumn(metadata.getColumnDefinition("cb"));
      metadata.setSearchAction(searchAction);
   }
}
