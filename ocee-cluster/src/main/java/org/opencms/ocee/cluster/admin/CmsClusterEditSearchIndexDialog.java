package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.searchindex.CmsEditSearchIndexDialog;

public class CmsClusterEditSearchIndexDialog extends CmsEditSearchIndexDialog {
   private String m_paramServer;

   public CmsClusterEditSearchIndexDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterEditSearchIndexDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void actionCommit() {
      ArrayList errors = new ArrayList();

      try {
         CmsClusterRemoteCmdHelper.writeSearchIndexes(this.getCms(), this.getServer(), Collections.singletonList(this.m_index));
      } catch (Throwable var3) {
         errors.add(var3);
      }

      this.setCommitErrors(errors);
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void defineWidgets() {
      super.defineWidgets();
      if (this.m_index != null && this.m_index.getName() != null) {
         this.addWidget(new CmsWidgetDialogParameter(this.m_index, "name", PAGES[0], new CmsDisplayWidget()));
      } else {
         this.addWidget(new CmsWidgetDialogParameter(this.m_index, "name", PAGES[0], new CmsInputWidget()));
      }

      this.addWidget(new CmsWidgetDialogParameter(this.m_index, "rebuildMode", "", PAGES[0], new CmsSelectWidget(this.getRebuildModeWidgetConfiguration()), 0, 1));
      this.addWidget(new CmsWidgetDialogParameter(this.m_index, "localeString", "", PAGES[0], new CmsSelectWidget(this.getLocaleWidgetConfiguration()), 0, 1));
      this.addWidget(new CmsWidgetDialogParameter(this.m_index, "project", "", PAGES[0], new CmsSelectWidget(this.getProjectWidgetConfiguration()), 0, 1));
      this.addWidget(new CmsWidgetDialogParameter(this.m_index, "fieldConfigurationName", "", PAGES[0], new CmsSelectWidget(this.getFieldConfigurationWidgetConfiguration()), 0, 1));
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initUserObject() {
      try {
         if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(this.getParamIndexName())) {
            this.m_index = CmsClusterRemoteCmdHelper.getSearchIndex(this.getCms(), this.getServer(), this.getParamIndexName());
         }

         if (this.m_index == null) {
            this.m_index = this.createDummySearchIndex();
         }
      } catch (Exception var2) {
         this.m_index = this.createDummySearchIndex();
      }

   }

   protected void validateParamaters() throws Exception {
      if (this.getCurrentToolPath().endsWith("/config-all/searchindex/searchindex-new")) {
         this.setParamServer(CmsClusterManager.getInstance().getWpServer().getName());
      }

      super.validateParamaters();
      if (this.getServer() == null) {
         throw new Exception();
      }
   }

   private CmsSearchIndexSource createDummyIndexSource() {
      CmsSearchIndexSource result = new CmsSearchIndexSource();
      result.setName("default");
      result.setIndexerClassName("org.opencms.search.CmsVfsIndexer");
      result.addDocumentType("html");
      result.addDocumentType("generic");
      result.addDocumentType("pdf");
      CmsClusterRemoteCmdHelper.createIndexSource(this.getCms(), this.getServer(), result);
      return result;
   }

   private CmsSearchIndex createDummySearchIndex() {
      CmsSearchIndex result = new CmsSearchIndex();
      result.setLocale(Locale.ENGLISH);
      result.setProjectName("Online");
      result.setRebuildMode("auto");
      List sources = CmsClusterRemoteCmdHelper.getIndexSources(this.getCms(), this.getServer());
      if (sources.isEmpty()) {
         CmsSearchIndexSource source = this.createDummyIndexSource();
         sources.add(source);
      }

      result.addSourceName(((CmsSearchIndexSource)sources.iterator().next()).getName());
      return result;
   }

   private List getFieldConfigurationWidgetConfiguration() {
      List result = new ArrayList();
      List fieldConfigurations = CmsClusterRemoteCmdHelper.getFieldConfigurations(this.getCms(), this.getServer());
      Iterator itFieldConfigs = fieldConfigurations.iterator();

      while(itFieldConfigs.hasNext()) {
         String curFieldConfig = (String)itFieldConfigs.next();
         CmsSelectWidgetOption option = new CmsSelectWidgetOption(curFieldConfig, curFieldConfig.equals("standard"));
         result.add(option);
      }

      return result;
   }

   private List getLocaleWidgetConfiguration() {
      List result = new ArrayList();
      Locale indexLocale = this.m_index.getLocale();
      Iterator analyzers = CmsClusterRemoteCmdHelper.getAnalyzers(this.getCms(), this.getServer()).iterator();
      HashSet distinctLocales = new HashSet();

      while(analyzers.hasNext()) {
         distinctLocales.add(analyzers.next());
      }

      Iterator locales = distinctLocales.iterator();

      while(locales.hasNext()) {
         String locale = (String)locales.next();
         CmsSelectWidgetOption option = new CmsSelectWidgetOption(locale, locale.equals(indexLocale));
         result.add(option);
      }

      return result;
   }

   private List getProjectWidgetConfiguration() {
      List result = new ArrayList();
      List projects = CmsClusterRemoteCmdHelper.getAllProjects(this.getCms(), this.getServer());
      Iterator itProjects = projects.iterator();
      String project = this.m_index.getProject();

      while(itProjects.hasNext()) {
         String curProject = (String)itProjects.next();
         CmsSelectWidgetOption option = new CmsSelectWidgetOption(curProject, curProject.equals(project));
         result.add(option);
      }

      return result;
   }

   private List getRebuildModeWidgetConfiguration() {
      List result = new ArrayList();
      String rebuildMode = this.m_index.getRebuildMode();
      CmsSelectWidgetOption option = new CmsSelectWidgetOption("auto", "auto".equals(rebuildMode));
      result.add(option);
      option = new CmsSelectWidgetOption("manual", "manual".equals(rebuildMode));
      result.add(option);
      return result;
   }

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }
}
