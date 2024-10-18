package org.opencms.ocee.cluster.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.base.A_CmsDiffViewDialog;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.util.CmsStringUtil;

public class CmsClusterConfigDiffViewDialog extends A_CmsDiffViewDialog {
   public static final String PARAM_CONFIG_FILE = "filename";
   public static final String PARAM_SERVER = "server";
   private String m_paramFilename;
   private String m_paramServer;

   public CmsClusterConfigDiffViewDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterConfigDiffViewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public String getParamFilename() {
      return this.m_paramFilename;
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setParamFilename(String paramFilename) {
      this.m_paramFilename = paramFilename;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void validateParamaters() throws Exception {
      CmsClusterServer server = CmsClusterManager.getInstance().getServer(this.getParamServer());
      if (server == null) {
         throw new Exception();
      } else if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamFilename())) {
         throw new Exception();
      }
   }

   protected String getCopySource() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer()).getConfigFile(this.getParamFilename());
   }

   protected int getLinesBeforeSkip() {
      return 2;
   }

   protected String getOriginalSource() {
      return CmsClusterManager.getInstance().getThisServer().getConfigFile(this.getParamFilename());
   }
}
