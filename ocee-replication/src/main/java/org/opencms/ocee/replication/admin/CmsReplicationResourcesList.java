package org.opencms.ocee.replication.admin;

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
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListResourceIconAction;

public class CmsReplicationResourcesList extends A_CmsListDialog {
   public static final String LIST_ACTION_ICON = "ai";
   public static final String LIST_COLUMN_CREATED = "cc";
   public static final String LIST_COLUMN_HISTORY = "ch";
   public static final String LIST_COLUMN_ICON = "ci";
   public static final String LIST_COLUMN_LASTMODIFIED = "cl";
   public static final String LIST_COLUMN_NAME = "cn";
   public static final String LIST_COLUMN_TYPE = "ct";
   public static final String LIST_DETAIL_SERVERS = "ds";
   public static final String LIST_ID = "lrr";
   public static final String PARAM_SERVERID = "serverid";
   public static final String PARAM_SERVERTITLE = "servertitle";
   private static final String Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = "do";
   private static final String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = "mr";
   private String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public CmsReplicationResourcesList(CmsJspActionElement jsp) {
      this("lrr", jsp);
   }

   public CmsReplicationResourcesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsReplicationResourcesList(String listId, CmsJspActionElement jsp) {
      super(jsp, listId, Messages.get().container("GUI_REPLICATION_RES_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void executeListMultiActions() throws IOException, ServletException {
      if (this.getParamListAction().equals("mr")) {
         Map params = new HashMap();
         params.put("servertitle", this.getParamServerTitle());
         params.put("serverid", this.getParamServerid());
         params.put("resourceids", this.getParamSelItems());
         params.put("style", "new");
         params.put("action", "initial");
         this.getToolManager().jspForwardTool(this, "/ocee-replication/resources/incremental", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   public void executeListSingleActions() {
      this.throwListUnsupportedActionException();
   }

   public CmsHtmlList getList() {
      CmsHtmlList list = super.getList();
      if (list != null) {
         CmsListColumnDefinition col = list.getMetadata().getColumnDefinition("cn");
         if (col != null) {
            ((CmsListOpenResourceAction)col.getDefaultAction("do")).setCms(this.getCms());
         }
      }

      return list;
   }

   public String getParamServerid() {
      return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   }

   public String getParamServerTitle() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public void setParamServerid(String paramServerId) {
      this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = paramServerId;
   }

   public void setParamServerTitle(String paramServerTitle) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = paramServerTitle;
   }

   protected void fillDetails(String detailId) {
   }

   protected List getListItems() throws CmsException {
      List ret = new ArrayList();
      String storedSiteRoot = this.getCms().getRequestContext().getSiteRoot();

      try {
         this.getCms().getRequestContext().setSiteRoot("/");
         Map resources = CmsReplicationManager.getInstance().getReplicationResourcesWithHistories(this.getCms(), new CmsUUID(this.getParamServerid()));
         Iterator itRes = resources.keySet().iterator();

         while(itRes.hasNext()) {
            CmsPublishedResource resource = (CmsPublishedResource)itRes.next();
            String history = (String)resources.get(resource);
            if (this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(resource, history.endsWith("D"))) {
               CmsListItem item = this.getList().newItem(resource.getResourceId().toString());
               item.set("cn", resource.getRootPath());
               item.set("ct", new Integer(resource.getType()));
               item.set("ch", history);

               try {
                  CmsResource file = this.getCms().readResource(resource.getRootPath());

                  try {
                     item.set("cc", this.getCms().readUser(file.getUserCreated()).getFullName());
                  } catch (Exception var24) {
                     item.set("cc", file.getUserCreated());
                  }

                  try {
                     item.set("cl", this.getCms().readUser(file.getUserLastModified()).getFullName());
                  } catch (Exception var23) {
                     item.set("cl", file.getUserLastModified());
                  }
               } catch (Exception var25) {
                  try {
                     I_CmsHistoryResource backup = this.getCms().readResource(resource.getStructureId(), resource.getPublishTag());

                     try {
                        item.set("cc", this.getCms().readUser(backup.getUserCreated()).getFullName());
                     } catch (Exception var21) {
                        item.set("cc", backup.getUserCreated());
                     }

                     try {
                        item.set("cl", this.getCms().readUser(backup.getUserLastModified()).getFullName());
                     } catch (Exception var20) {
                        item.set("cl", backup.getUserLastModified());
                     }
                  } catch (Exception var22) {
                  }
               }

               ret.add(item);
            }
         }
      } finally {
         this.getCms().getRequestContext().setSiteRoot(storedSiteRoot);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
      iconCol.setName(Messages.get().container("GUI_REPLICATION_RES_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setListItemComparator(new CmsListItemActionIconComparator());
      CmsListDirectAction iconAction = new CmsListResourceIconAction("ai", "ct", this.getCms());
      iconAction.setName(Messages.get().container("GUI_REPLICATION_RES_LIST_ACTION_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_REPLICATION_RES_LIST_ACTION_ICON_HELP_0"));
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_REPLICATION_RES_LIST_COLS_NAME_0"));
      nameCol.setWidth("50%");
      nameCol.addDefaultAction(new CmsListOpenResourceAction("do", this.getCms(), "cn"));
      metadata.addColumn(nameCol);
      CmsListColumnDefinition historyCol = new CmsListColumnDefinition("ch");
      historyCol.setName(Messages.get().container("GUI_REPLICATION_RES_LIST_COLS_HISTORY_0"));
      historyCol.setWidth("20%");
      metadata.addColumn(historyCol);
      CmsListColumnDefinition createdCol = new CmsListColumnDefinition("cc");
      createdCol.setName(Messages.get().container("GUI_REPLICATION_RES_LIST_COLS_CREATED_0"));
      createdCol.setWidth("15%");
      metadata.addColumn(createdCol);
      CmsListColumnDefinition lastModifiedCol = new CmsListColumnDefinition("cl");
      lastModifiedCol.setName(Messages.get().container("GUI_REPLICATION_RES_LIST_COLS_LASTMODIFIED_0"));
      lastModifiedCol.setWidth("15%");
      metadata.addColumn(lastModifiedCol);
      CmsListColumnDefinition typeCol = new CmsListColumnDefinition("ct");
      typeCol.setName(new CmsMessageContainer((I_CmsMessageBundle)null, "type"));
      typeCol.setVisible(false);
      metadata.addColumn(typeCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction replicateAction = new CmsListMultiAction("mr");
      replicateAction.setName(Messages.get().container("GUI_REPLICATION_RES_LIST_MACTION_REPLICATE_NAME_0"));
      replicateAction.setHelpText(Messages.get().container("GUI_REPLICATION_RES_LIST_MACTION_REPLICATE_HELP_0"));
      replicateAction.setConfirmationMessage(Messages.get().container("GUI_REPLICATION_RES_LIST_MACTION_REPLICATE_CONF_0"));
      replicateAction.setIconPath("tools/ocee-replication/buttons/multi_replicate.png");
      metadata.addMultiAction(replicateAction);
   }

   protected void validateParamaters() throws Exception {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamServerid()) || CmsReplicationManager.getInstance().getConfiguration().getReplicationServer(new CmsUUID(this.getParamServerid())) == null) {
         throw new Exception();
      }
   }

   private boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsPublishedResource resource, boolean isDeleted) {
      if (OpenCms.getRoleManager().hasRole(this.getCms(), CmsRole.PROJECT_MANAGER)) {
         return true;
      } else {
         String path = resource.getRootPath();
         if (isDeleted) {
            while(!path.equals("/") && !this.getCms().existsResource(path)) {
               path = CmsResource.getParentFolder(path);
            }
         }

         try {
            OpenCms.getPublishManager().getPublishList(this.getCms(), this.getCms().readResource(path), false);
            return true;
         } catch (Exception var5) {
            return false;
         }
      }
   }
}
