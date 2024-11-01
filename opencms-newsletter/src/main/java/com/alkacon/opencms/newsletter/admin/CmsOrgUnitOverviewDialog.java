package com.alkacon.opencms.newsletter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.util.CmsStringUtil;

public class CmsOrgUnitOverviewDialog extends org.opencms.workplace.tools.accounts.CmsOrgUnitOverviewDialog {
   public CmsOrgUnitOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      super(context, req, res);
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamOufqn())) {
         throw new Exception();
      }
   }
}
