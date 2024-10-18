package org.opencms.ocee.cluster.admin;

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
import org.opencms.workplace.tools.searchindex.CmsOverviewSearchIndexDialog;

public class CmsClusterOverviewSearchIndexDialog extends CmsOverviewSearchIndexDialog {
   private String m_paramServer;

   public CmsClusterOverviewSearchIndexDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterOverviewSearchIndexDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void initUserObject() {
      try {
         this.m_index = CmsClusterRemoteCmdHelper.getSearchIndex(this.getCms(), this.getServer(), this.getParamIndexName());
         if (this.m_index == null) {
            this.m_index = this.createDummySearchIndex();
         }
      } catch (Exception var2) {
         this.m_index = this.createDummySearchIndex();
      }

   }

   protected void validateParamaters() throws Exception {
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

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }
}
