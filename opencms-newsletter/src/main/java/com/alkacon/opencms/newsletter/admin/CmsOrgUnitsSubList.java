package com.alkacon.opencms.newsletter.admin;

import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;

public class CmsOrgUnitsSubList extends org.opencms.workplace.tools.accounts.CmsOrgUnitsSubList {
   public CmsOrgUnitsSubList(CmsJspActionElement jsp) {
      super(jsp);
      this.getList().setName(Messages.get().container("GUI_NEWSLETTER_ORGUNITS_LIST_NAME_0"));
   }

   public CmsOrgUnitsSubList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionDelete() throws Exception {
      List childOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(this.getCms(), this.getParamOufqn(), false);
      Iterator i = childOus.iterator();

      while(i.hasNext()) {
         CmsOrganizationalUnit unit = (CmsOrganizationalUnit)i.next();
         if (unit.getSimpleName().startsWith("nl_")) {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(this.getCms(), unit.getName());
         }
      }

      this.actionCloseDialog();
   }
}
