package org.opencms.ocee.cluster.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterRemoteCmdHelper;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.searchindex.CmsSearchIndexList;

public class CmsClusterSearchIndexList extends CmsSearchIndexList {
   private String m_paramServer;

   public CmsClusterSearchIndexList(CmsJspActionElement jsp) {
      this(jsp, "lssi", org.opencms.workplace.tools.searchindex.Messages.get().container("GUI_LIST_SEARCHINDEX_NAME_0"));
   }

   public CmsClusterSearchIndexList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {
      super(jsp, listId, listName, "cn", CmsListOrderEnum.ORDER_ASCENDING, (String)null);
   }

   public CmsClusterSearchIndexList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() throws CmsRuntimeException {
      if (this.getParamListAction().equals("mad")) {
         Iterator itItems = this.getSelectedItems().iterator();

         while(itItems.hasNext()) {
            CmsListItem listItem = (CmsListItem)itItems.next();
            CmsClusterRemoteCmdHelper.deleteSearchIndex(this.getCms(), this.getServer(), (String)listItem.get("cn"));
         }
      } else if (this.getParamListAction().equals("mar")) {
         List<String> siNames = new ArrayList();
         Iterator i$ = this.getSelectedItems().iterator();

         while(i$.hasNext()) {
            CmsListItem li = (CmsListItem)i$.next();
            siNames.add(li.getId());
         }

         CmsClusterRemoteCmdHelper.rebuildSearchIndexes(this.getCms(), this.getServer(), siNames);
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {
      String index = this.getSelectedItem().getId();
      Map params = new HashMap();
      params.put("server", this.getParamServer());
      String action = this.getParamListAction();
      if (action.equals("ad")) {
         CmsClusterRemoteCmdHelper.deleteSearchIndex(this.getCms(), this.getServer(), index);
      } else if (action.equals("ar")) {
         List indexes = new ArrayList();
         indexes.add(this.getSelectedItem().getId());
         CmsClusterRemoteCmdHelper.rebuildSearchIndexes(this.getCms(), this.getServer(), indexes);
      } else if (action.equals("ae")) {
         params.put("style", "new");
         params.put("indexname", index);
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/singleindex/edit", params);
      } else if (action.equals("asio")) {
         params.put("action", "initial");
         params.put("style", "new");
         params.put("indexname", index);
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/singleindex", params);
      } else if (action.equals("ais")) {
         params.put("action", "initial");
         params.put("style", "new");
         params.put("indexname", index);
         this.getToolManager().jspForwardTool(this, this.getCurrentToolPath() + "/singleindex/indexsources", params);
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
      List allSrcs = new ArrayList();
      if (detailId.equals("di")) {
         allSrcs = CmsClusterRemoteCmdHelper.getIndexSources(this.getCms(), this.getServer());
      }

      List items = this.getList().getAllContent();
      Iterator itItems = items.iterator();

      while(true) {
         while(itItems.hasNext()) {
            CmsListItem item = (CmsListItem)itItems.next();
            String idxName = (String)item.get("cn");
            CmsSearchIndex idx = CmsClusterRemoteCmdHelper.getSearchIndex(this.getCms(), this.getServer(), idxName);
            if (detailId.equals("di")) {
               List sources = new ArrayList();
               Iterator it = ((List)allSrcs).iterator();

               while(it.hasNext()) {
                  CmsSearchIndexSource src = (CmsSearchIndexSource)it.next();
                  if (idx.getSourceNames().contains(src.getName())) {
                     sources.add(src);
                  }
               }

               item.set(detailId, this.fillDetailIndexSource(sources));
            } else if (detailId.equals("df")) {
               item.set(detailId, this.fillDetailFieldConfiguration(idx));
            }
         }

         return;
      }
   }

   protected List getListItems() {
      List result = new ArrayList();
      List indexes = CmsClusterRemoteCmdHelper.getSearchIndexes(this.getCms(), this.getServer());
      Iterator itIndexes = indexes.iterator();

      while(itIndexes.hasNext()) {
         CmsSearchIndex index = (CmsSearchIndex)itIndexes.next();
         CmsListItem item = this.getList().newItem(index.getName());
         item.set("cn", index.getName());
         item.set("cc", index.getFieldConfigurationName());
         item.set("cr", index.getRebuildMode());
         item.set("cp", index.getProject());
         item.set("cl", index.getLocale().toString());
         result.add(item);
      }

      return result;
   }

   protected CmsClusterServer getServer() {
      return CmsClusterManager.getInstance().getServer(this.getParamServer());
   }

   protected void initMessages() {
      this.addMessages(org.opencms.workplace.tools.searchindex.Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      super.setColumns(metadata);
      metadata.getColumnDefinition("cas").setVisible(false);
   }

   protected void validateParamaters() throws Exception {
      super.validateParamaters();
      if (this.getServer() == null) {
         throw new Exception();
      }
   }

   protected String fillDetailFieldConfiguration(CmsSearchIndex idx) {
      StringBuffer html = new StringBuffer();
      CmsSearchFieldConfiguration idxFieldConfiguration = idx.getFieldConfiguration();
      if (idxFieldConfiguration == null) {
         return html.toString();
      } else {
         List fields = idxFieldConfiguration.getFields();
         html.append("<ul>\n");
         html.append("  <li>\n").append("    ").append("name      : ").append(idxFieldConfiguration.getName()).append("\n");
         html.append("  </li>");
         html.append("  <li>\n").append("    ").append("fields : ").append("\n");
         html.append("    <ul>\n");

         for(Iterator itFields = fields.iterator(); itFields.hasNext(); html.append("  </li>")) {
            CmsSearchField field = (CmsSearchField)itFields.next();
            String fieldName = field.getName();
            boolean fieldStore = field.isStored();
            String fieldIndex = field.getIndexed();
            boolean fieldExcerpt = field.isInExcerpt();
            float fieldBoost = field.getBoost();
            String fieldDefault = field.getDefaultValue();
            html.append("  <li>\n").append("    ");
            html.append("name=").append(fieldName);
            if (fieldStore) {
               html.append(", ").append("store=").append(fieldStore);
            }

            if (!fieldIndex.equals("false")) {
               html.append(", ").append("index=").append(fieldIndex);
            }

            if (fieldExcerpt) {
               html.append(", ").append("excerpt=").append(fieldExcerpt);
            }

            if (fieldBoost != 1.0F) {
               html.append(", ").append("boost=").append(fieldBoost);
            }

            if (fieldDefault != null) {
               html.append(", ").append("default=").append(field.getDefaultValue());
            }
         }

         html.append("    </ul>\n");
         html.append("  </li>");
         html.append("</ul>\n");
         return html.toString();
      }
   }

   protected String fillDetailIndexSource(List idxSources) {
      StringBuffer html = new StringBuffer();
      Iterator itIdxSources = idxSources.iterator();
      html.append("<ul>\n");

      while(itIdxSources.hasNext()) {
         CmsSearchIndexSource idxSource = (CmsSearchIndexSource)itIdxSources.next();
         html.append("  <li>\n").append("    ").append("name      : ").append(idxSource.getName()).append("\n");
         html.append("  </li>");
         html.append("  <li>\n").append("    ").append("indexer   : ").append(idxSource.getIndexerClassName()).append("\n");
         html.append("  </li>");
         html.append("  <li>\n").append("    ").append("resources : ").append("\n");
         html.append("    <ul>\n");
         Iterator itResources = idxSource.getResourcesNames().iterator();

         while(itResources.hasNext()) {
            html.append("    <li>\n").append("      ").append((String)itResources.next()).append("\n");
            html.append("    </li>\n");
         }

         html.append("    </ul>\n");
         html.append("  </li>");
         html.append("  <li>\n").append("    ").append("doctypes : ").append("\n");
         html.append("    <ul>\n");
         Iterator itDocTypes = idxSource.getDocumentTypes().iterator();

         while(itDocTypes.hasNext()) {
            html.append("    <li>\n").append("      ").append((String)itDocTypes.next()).append("\n");
            html.append("    </li>\n");
         }

         html.append("    </ul>\n");
         html.append("  </li>");
      }

      html.append("</ul>\n");
      return html.toString();
   }
}
