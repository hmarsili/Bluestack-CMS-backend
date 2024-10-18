package org.opencms.ocee.cluster.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.list.A_CmsListReport;

public class CmsClusterExportReport extends A_CmsListReport {
   public static final String PARAM_SERVERS = "servers";
   private String m_paramServers;

   public CmsClusterExportReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      super(context, req, res);
   }

   public String getParamServers() {
      return this.m_paramServers;
   }

   public I_CmsReportThread initializeThread() {
      return new CmsClusterExportThread(this.getCms(), this.getParamServers());
   }

   public void setParamServers(String paramServers) {
      this.m_paramServers = paramServers;
   }
}
