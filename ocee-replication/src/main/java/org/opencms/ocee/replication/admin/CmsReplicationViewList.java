package org.opencms.ocee.replication.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.ocee.replication.CmsReplicationManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListResourceIconAction;

public class CmsReplicationViewList extends A_CmsListDialog {
   public static final String LIST_ACTION_ICON = "ai";
   public static final String LIST_COLUMN_CREATED = "cc";
   public static final String LIST_COLUMN_ICON = "ci";
   public static final String LIST_COLUMN_LASTMODIFIED = "cl";
   public static final String LIST_COLUMN_NAME = "cn";
   public static final String LIST_COLUMN_TYPE = "ct";
   public static final String LIST_ID = "lrv";
   public static final String PARAM_RESOURCENAME = "resourcename";
   public static final String PARAM_SERVER = "server";
   public static final String PARAM_SERVERTITLE = "servertitle";
   private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   private String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   private String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;

   public CmsReplicationViewList(CmsJspActionElement jsp) {
      this("lrv", jsp);
   }

   public CmsReplicationViewList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected CmsReplicationViewList(String listId, CmsJspActionElement jsp) {
      super(jsp, listId, Messages.get().container("GUI_REPLICATION_VIEW_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public void executeListMultiActions() {
      this.throwListUnsupportedActionException();
   }

   public void executeListSingleActions() {
      this.throwListUnsupportedActionException();
   }

   public String getParamResourcename() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public String getParamServer() {
      return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   }

   public String getParamServerTitle() {
      return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
   }

   public void setParamResourcename(String resourceName) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = resourceName;
   }

   public void setParamServer(String server) {
      this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = server;
   }

   public void setParamServerTitle(String paramServerTitle) {
      this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = paramServerTitle;
   }

   protected void fillDetails(String detailId) {
   }

   protected List getListItems() throws CmsException {
      CmsReplicationManager.getInstance().lazyInitialization(this.getCms());
      String storedSiteRoot = this.getCms().getRequestContext().getSiteRoot();
      ArrayList ret = new ArrayList();

      try {
         this.getCms().getRequestContext().setSiteRoot("/");
         List resources = CmsReplicationManager.getInstance().getConfiguration().getReplicationServer(new CmsUUID(this.getParamServer())).getServerResources(this.getCms().getRequestContext(), this.getParamResourcename());

         CmsListItem item;
         for(Iterator itRes = resources.iterator(); itRes.hasNext(); ret.add(item)) {
            CmsResource resource = (CmsResource)itRes.next();
            item = this.getList().newItem(resource.getResourceId().toString());
            item.set("cn", resource.getRootPath());
            item.set("ct", new Integer(resource.getTypeId()));

            try {
               item.set("cc", this.getCms().readUser(resource.getUserCreated()).getFullName());
            } catch (Exception var14) {
               item.set("cc", resource.getUserCreated());
            }

            try {
               item.set("cl", this.getCms().readUser(resource.getUserLastModified()).getFullName());
            } catch (Exception var13) {
               item.set("cl", resource.getUserLastModified());
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
      iconCol.setName(Messages.get().container("GUI_REPLICATION_VIEW_LIST_COLS_ICON_0"));
      iconCol.setWidth("20");
      iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      iconCol.setListItemComparator(new CmsListItemActionIconComparator());
      CmsListDirectAction iconAction = new CmsListResourceIconAction("ai", "ct", this.getCms());
      iconAction.setName(Messages.get().container("GUI_REPLICATION_VIEW_LIST_ACTION_ICON_NAME_0"));
      iconAction.setHelpText(Messages.get().container("GUI_REPLICATION_VIEW_LIST_ACTION_ICON_HELP_0"));
      iconAction.setEnabled(false);
      iconCol.addDirectAction(iconAction);
      metadata.addColumn(iconCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_REPLICATION_VIEW_LIST_COLS_NAME_0"));
      nameCol.setWidth("60%");
      metadata.addColumn(nameCol);
      CmsListColumnDefinition createdCol = new CmsListColumnDefinition("cc");
      createdCol.setName(Messages.get().container("GUI_REPLICATION_VIEW_LIST_COLS_CREATED_0"));
      createdCol.setWidth("20%");
      metadata.addColumn(createdCol);
      CmsListColumnDefinition lastModifiedCol = new CmsListColumnDefinition("cl");
      lastModifiedCol.setName(Messages.get().container("GUI_REPLICATION_VIEW_LIST_COLS_LASTMODIFIED_0"));
      lastModifiedCol.setWidth("20%");
      metadata.addColumn(lastModifiedCol);
      CmsListColumnDefinition typeCol = new CmsListColumnDefinition("ct");
      typeCol.setName(new CmsMessageContainer((I_CmsMessageBundle)null, "type"));
      typeCol.setVisible(false);
      metadata.addColumn(typeCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
   }

   protected void setMultiActions(CmsListMetadata metadata) {
   }

   protected void validateParamaters() throws Exception {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.getParamResourcename()) || CmsReplicationManager.getInstance().getConfiguration().getReplicationServer(new CmsUUID(this.getParamServer())) == null) {
         throw new Exception();
      }
   }
}
