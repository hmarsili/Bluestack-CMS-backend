package com.alkacon.opencms.newsletter.admin;

import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.tools.accounts.A_CmsGroupsList;

public class CmsDummyList extends A_CmsGroupsList {
   public CmsDummyList(CmsJspActionElement jsp) {
      super(jsp, "dummy", Messages.get().container("GUI_MAILINGLISTS_LIST_NAME_0"));
   }

   public CmsDummyList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void displayDialog() {
   }

   protected List getGroups() {
      return Collections.EMPTY_LIST;
   }

   protected void setDeleteAction(CmsListColumnDefinition deleteCol) {
   }

   protected void setEditAction(CmsListColumnDefinition editCol) {
   }

   protected void validateParamaters() throws Exception {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      } else {
         super.validateParamaters();
      }
   }
}
