package org.opencms.ocee.cluster.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.tools.searchindex.CmsSearchIndexSourceRemoveList;

public class CmsClusterSearchIndexSourceRemoveList extends CmsSearchIndexSourceRemoveList {
   private String m_paramServer;

   public CmsClusterSearchIndexSourceRemoveList(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsClusterSearchIndexSourceRemoveList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() {
      if (this.getParamListAction().equals("mars")) {
         CmsSearchIndex idx = CmsClusterRemoteCmdHelper.getSearchIndex(this.getCms(), this.getServer(), this.getParamIndexName());
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem item = (CmsListItem)itItems.next();
            String indexSource = (String)item.get("cn");
            if (idx.getSourceNames().size() > 1) {
               idx.removeSourceName(indexSource);
            }
         }

         CmsClusterRemoteCmdHelper.writeSearchIndexes(this.getCms(), this.getServer(), Collections.singletonList(idx));
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public void executeListSingleActions() {
      CmsListItem item = this.getSelectedItem();
      String action = this.getParamListAction();
      if (!action.equals("ars") && !action.equals("ars2")) {
         this.throwListUnsupportedActionException();
      } else {
         CmsSearchIndex idx = CmsClusterRemoteCmdHelper.getSearchIndex(this.getCms(), this.getServer(), this.getParamIndexName());
         if (idx.getSourceNames().size() > 1) {
            String indexSource = (String)item.get("cn");
            idx.removeSourceName(indexSource);
         }

         CmsClusterRemoteCmdHelper.writeSearchIndexes(this.getCms(), this.getServer(), Collections.singletonList(idx));
      }

      this.listSave();
   }

   public String getParamServer() {
      return this.m_paramServer;
   }

   public void setParamServer(String paramServer) {
      this.m_paramServer = paramServer;
   }

   protected void fillDetails(String detailId) {
      List items = this.getList().getAllContent();
      Iterator itItems = items.iterator();
      CmsListItem item;
      if (detailId.equals("dd")) {
         while(itItems.hasNext()) {
            item = (CmsListItem)itItems.next();
            this.fillDetailDocTypes(item, detailId);
         }
      }

      if (detailId.equals("dr")) {
         while(itItems.hasNext()) {
            item = (CmsListItem)itItems.next();
            this.fillDetailResources(item, detailId);
         }
      }

   }

   protected List getListItems() {
      List result = new ArrayList();
      List sources = this.searchIndexSources();
      Iterator itSources = sources.iterator();

      while(itSources.hasNext()) {
         CmsSearchIndexSource source = (CmsSearchIndexSource)itSources.next();
         CmsListItem item = this.getList().newItem(source.getName());
         item.set("cn", source.getName());
         item.set("ca", source.getIndexer().getClass().getName());
         result.add(item);
      }

      return result;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (this.getServer() == null) {
         throw new Exception();
      }
   }

   private void fillDetailDocTypes(CmsListItem item, String detailId) {
      StringBuffer html = new StringBuffer();
      String idxSourceName = (String)item.get("cn");
      CmsSearchIndexSource idxSource = CmsClusterRemoteCmdHelper.getIndexSource(this.getCms(), this.getServer(), idxSourceName);
      List docTypes = idxSource.getDocumentTypes();
      Iterator itDocTypes = docTypes.iterator();
      html.append("<ul>\n");

      while(itDocTypes.hasNext()) {
         CmsSearchDocumentType docType = CmsClusterRemoteCmdHelper.getDocumentTypeConfig(this.getCms(), this.getServer(), itDocTypes.next().toString());
         if (docType != null) {
            html.append("  <li>\n").append("  ").append(docType.getName()).append("\n");
            html.append("  </li>");
         }
      }

      html.append("</ul>\n");
      item.set(detailId, html.toString());
   }

   private void fillDetailResources(CmsListItem item, String detailId) {
      StringBuffer html = new StringBuffer();
      String idxSourceName = (String)item.get("cn");
      CmsSearchIndexSource idxSource = CmsClusterRemoteCmdHelper.getIndexSource(this.getCms(), this.getServer(), idxSourceName);
      List resources = idxSource.getResourcesNames();
      Iterator itResources = resources.iterator();
      html.append("<ul>\n");

      while(itResources.hasNext()) {
         html.append("  <li>\n").append("  ").append(itResources.next().toString()).append("\n");
         html.append("  </li>");
      }

      html.append("</ul>\n");
      item.set(detailId, html.toString());
   }

   private CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }

   private List searchIndexSources() {
      CmsSearchIndex index = CmsClusterRemoteCmdHelper.getSearchIndex(this.getCms(), this.getServer(), this.getParamIndexName());
      List sources = index.getSources();
      return sources;
   }
}
