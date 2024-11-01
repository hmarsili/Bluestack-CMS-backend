package com.alkacon.opencms.newsletter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.workplace.commons.CmsGroupSelectionList;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;
import org.opencms.workplace.tools.CmsToolMacroResolver;

public class CmsMailinglistSelectionList extends CmsGroupSelectionList {
   public CmsMailinglistSelectionList(CmsJspActionElement jsp) {
      super(jsp);
      this.getList().setName(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_NAME_0"));
   }

   public CmsMailinglistSelectionList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public String dialogTitle() {
      StringBuffer html = new StringBuffer(512);
      html.append("<div class='screenTitle'>\n");
      html.append("\t<table width='100%' cellspacing='0'>\n");
      html.append("\t\t<tr>\n");
      html.append("\t\t\t<td>\n");
      html.append(Messages.get().getBundle(this.getLocale()).key("GUI_ALK_MAILINGLISTSELECTION_INTRO_TITLE_0"));
      html.append("\n\t\t\t</td>");
      html.append("\t\t</tr>\n");
      html.append("\t</table>\n");
      html.append("</div>\n");
      return CmsToolMacroResolver.resolveMacros(html.toString(), this);
   }

   protected List getGroups() throws CmsException {
      List ret = new ArrayList();
      Iterator i = CmsNewsletterManager.getOrgUnits(this.getCms()).iterator();

      while(i.hasNext()) {
         CmsOrganizationalUnit ou = (CmsOrganizationalUnit)i.next();
         ret.addAll(OpenCms.getOrgUnitManager().getGroups(this.getCms(), ou.getName(), false));
      }

      return ret;
   }

   protected void setColumns(CmsListMetadata metadata) {
      super.setColumns(metadata);
      CmsListColumnDefinition iconCol = metadata.getColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_COLS_ICON_0"));
      iconCol.setHelpText(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_COLS_ICON_HELP_0"));
      I_CmsListDirectAction iconAction = iconCol.getDirectAction("ai");
      iconAction.setName(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_ICON_HELP_0"));
      iconAction.setIconPath("buttons/mailinglist.png");
      CmsListColumnDefinition nameCol = metadata.getColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_COLS_NAME_0"));
      CmsListDefaultAction selectAction = nameCol.getDefaultAction("js");
      selectAction.setName(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_ACTION_SELECT_NAME_0"));
      selectAction.setHelpText(Messages.get().container("GUI_ALK_MAILINGLISTSELECTION_LIST_ACTION_SELECT_HELP_0"));
   }
}
