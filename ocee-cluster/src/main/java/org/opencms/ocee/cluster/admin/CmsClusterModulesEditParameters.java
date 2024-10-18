package org.opencms.ocee.cluster.admin;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.modules.CmsModulesEditParameters;
import org.opencms.workplace.tools.modules.CmsModulesList;

public class CmsClusterModulesEditParameters extends CmsModulesEditParameters {
   private SortedMap m_moduleParameters;
   private String m_paramServer;

   public CmsClusterModulesEditParameters(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterModulesEditParameters(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      if (!this.hasCommitErrors()) {
         try {
            CmsClusterRemoteCmdHelper.setModuleParameters(this.getCms(), this.getServer(), this.getParamModule(), this.m_moduleParameters);
            Map objects = (Map)this.getSettings().getListObject();
            if (objects != null) {
               objects.remove(CmsModulesList.class.getName());
            }
         } catch (Exception var2) {
            this.addCommitError(var2);
         }
      }

   }

   public SortedMap getModuleParameters() {
      return this.m_moduleParameters;
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setModuleParameters(SortedMap parameters) {
      this.m_moduleParameters = parameters;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void validateParamaters() throws Exception {
      if (this.getServer() == null) {
         throw new Exception();
      } else {
         String moduleName = this.getParamModule();
         if (!CmsClusterRemoteCmdHelper.existsModule(this.getCms(), this.getServer(), moduleName)) {
            throw new Exception();
         }
      }
   }

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }

   protected String createDialogHtml(String dialog) {
      StringBuffer result = new StringBuffer(1024);
      result.append(this.createWidgetTableStart());
      result.append(this.createWidgetErrorHeader());
      if (dialog.equals(PAGES[0])) {
         result.append(this.dialogBlockStart(this.key("label.parameter")));
         result.append(this.createWidgetTableStart());
         result.append(this.createDialogRowsHtml(0, 0));
         result.append(this.createWidgetTableEnd());
         result.append(this.dialogBlockEnd());
      }

      result.append(this.createWidgetTableEnd());
      return result.toString();
   }

   protected void defineWidgets() {
      this.initModule();
      this.setKeyPrefix("modules");
      this.addWidget(new CmsWidgetDialogParameter(this, "moduleParameters", PAGES[0], new CmsInputWidget()));
   }

   protected void initModule() {
      Object o = null;
      if (!CmsStringUtil.isEmpty(this.getParamAction()) && !"initial".equals(this.getParamAction())) {
         o = this.getDialogObject();
      } else if (CmsStringUtil.isNotEmpty(this.m_paramModule)) {
         Map params = CmsClusterRemoteCmdHelper.getModuleParameters(this.getCms(), this.getServer(), this.getParamModule());
         if (params != null) {
            o = new TreeMap(params);
         }
      }

      if (!(o instanceof SortedMap)) {
         o = new TreeMap();
      }

      this.m_moduleParameters = (SortedMap)o;
   }
}
