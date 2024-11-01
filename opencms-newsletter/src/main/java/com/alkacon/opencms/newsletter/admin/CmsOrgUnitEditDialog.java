package com.alkacon.opencms.newsletter.admin;

import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

public class CmsOrgUnitEditDialog extends org.opencms.workplace.tools.accounts.CmsOrgUnitEditDialog {
   public CmsOrgUnitEditDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsOrgUnitEditDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      super(context, req, res);
   }

   public void actionCommit() {
      ArrayList errors = new ArrayList();

      try {
         if (this.isNewOrgUnit()) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.m_orgUnitBean.getName())) {
               throw new CmsException(Messages.get().container("EXC_NEWSLETTER_OU_NO_NAME_0"));
            }

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.m_orgUnitBean.getDescription())) {
               throw new CmsException(Messages.get().container("EXC_NEWSLETTER_OU_NO_DESCRIPTION_0"));
            }

            this.m_orgUnitBean.setFqn(this.m_orgUnitBean.getParentOu() + "nl_" + this.m_orgUnitBean.getName());
            OpenCms.getOrgUnitManager().createOrganizationalUnit(this.getCms(), this.m_orgUnitBean.getFqn(), this.m_orgUnitBean.getDescription(), 8, (String)null);
         } else {
            CmsOrganizationalUnit orgunit = OpenCms.getOrgUnitManager().readOrganizationalUnit(this.getCms(), this.m_orgUnitBean.getFqn());
            orgunit.setDescription(this.m_orgUnitBean.getDescription());
            OpenCms.getOrgUnitManager().writeOrganizationalUnit(this.getCms(), orgunit);
         }
      } catch (Throwable var3) {
         errors.add(var3);
      }

      this.setCommitErrors(errors);
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("GUI_NEWSLETTER_ORGUNIT_EDITOR_LABEL_IDENTIFICATION_BLOCK_0")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 1));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.initOrgUnitObject();
      this.setKeyPrefix("newsletterorgunit");
      if (this.m_orgUnitBean.getName() == null) {
         this.addWidget(new CmsWidgetDialogParameter(this.m_orgUnitBean, "name", PAGES[0], new CmsInputWidget()));
      } else {
         this.addWidget(new CmsWidgetDialogParameter(this.m_orgUnitBean, "name", PAGES[0], new CmsDisplayWidget()));
      }

      this.addWidget(new CmsWidgetDialogParameter(this.m_orgUnitBean, "description", PAGES[0], new CmsTextareaWidget()));
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initOrgUnitObject() {
      super.initOrgUnitObject();
   }

   protected boolean isNewOrgUnit() {
      return this.getCurrentToolPath().equals("/accounts/orgunit/mgmt/newnewsletter");
   }
}
