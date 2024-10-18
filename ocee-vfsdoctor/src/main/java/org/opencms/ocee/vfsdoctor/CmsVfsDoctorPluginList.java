package org.opencms.ocee.vfsdoctor;

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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsVfsDoctorPluginList extends A_CmsListDialog {
   public static final String LIST_ACTION_VALIDATE = "av";
   public static final String LIST_ACTION_RECOVER = "ar";
   public static final String LIST_COLUMN_NAME = "cn";
   public static final String LIST_COLUMN_VALIDATE = "cv";
   public static final String LIST_COLUMN_RECOVER = "cr";
   public static final String LIST_DEFACTION_VALIDATE = "dv";
   public static final String LIST_DETAIL_INFO = "di";
   public static final String LIST_ID = "lvdp";
   public static final String LIST_MACTION_VALIDATE = "mv";
   public static final String LIST_MACTION_RECOVER = "mr";
   public static final String PATH_BUTTONS = "tools/ocee-vfsdoctor/buttons/";

   public CmsVfsDoctorPluginList(CmsJspActionElement jsp) {
      super(jsp, "lvdp", Messages.get().container("GUI_PLUGINS_LIST_NAME_0"), "cn", CmsListOrderEnum.ORDER_ASCENDING, "cn");
   }

   public CmsVfsDoctorPluginList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   public void executeListMultiActions() throws IOException, ServletException {
      Map params = new HashMap();
      params.put("pluginids", this.getParamSelItems());
      params.put("style", "new");
      params.put("action", "initial");
      if (this.getParamListAction().equals("mr")) {
         params.put("mode", CmsVfsDoctorPluginExeMode.RECOVER.toString());
         this.getToolManager().jspForwardPage(this, "executeplugins.jsp", params);
      } else if (this.getParamListAction().equals("mv")) {
         params.put("mode", CmsVfsDoctorPluginExeMode.VALIDATE.toString());
         this.getToolManager().jspForwardPage(this, "executeplugins.jsp", params);
      } else {
         this.throwListUnsupportedActionException();
      }

   }

   public void executeListSingleActions() throws IOException, ServletException {
      Map params = new HashMap();
      params.put("pluginids", this.getParamSelItems());
      params.put("style", "new");
      params.put("action", "initial");
      if (this.getParamListAction().equals("dv")) {
         params.put("mode", CmsVfsDoctorPluginExeMode.VALIDATE.toString());
         this.getToolManager().jspForwardPage(this, "executeplugins.jsp", params);
      } else if (this.getParamListAction().equals("ar")) {
         params.put("mode", CmsVfsDoctorPluginExeMode.RECOVER.toString());
         this.getToolManager().jspForwardPage(this, "executeplugins.jsp", params);
      } else if (this.getParamListAction().equals("av")) {
         params.put("mode", CmsVfsDoctorPluginExeMode.VALIDATE.toString());
         this.getToolManager().jspForwardPage(this, "executeplugins.jsp", params);
      } else {
         this.throwListUnsupportedActionException();
      }

      this.listSave();
   }

   protected void fillDetails(String detailId) {
      Iterator itPlugins = this.getList().getAllContent().iterator();

      while(itPlugins.hasNext()) {
         CmsListItem item = (CmsListItem)itPlugins.next();

         try {
            if (detailId.equals("di")) {
               I_CmsVfsDoctorPlugin plugin = CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(item.getId());
               item.set("di", "<div>" + plugin.getDescription().key(this.getLocale()) + "</div>");
            }
         } catch (Exception var5) {
         }
      }

   }

   protected List getListItems() {
      List ret = new ArrayList();
      Iterator itPlugins = CmsVfsDoctorManager.getInstance().getPluginManager().getPlugins().iterator();

      while(itPlugins.hasNext()) {
         I_CmsVfsDoctorPlugin plugin = (I_CmsVfsDoctorPlugin)itPlugins.next();
         CmsListItem item = this.getList().newItem(plugin.getClass().getName());
         item.set("cn", plugin.getNiceName().key(this.getLocale()));
         ret.add(item);
      }

      return ret;
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected void setColumns(CmsListMetadata metadata) {
      CmsListColumnDefinition recoverCol = new CmsListColumnDefinition("cr");
      recoverCol.setName(Messages.get().container("GUI_PLUGINS_LIST_COLS_RECOVER_0"));
      recoverCol.setWidth("20");
      recoverCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      CmsListDirectAction recoverAction = new CmsListDirectAction("ar") {
         public boolean isVisible() {
            return CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(this.getItem().getId()).canRunInMode(CmsVfsDoctorPluginExeMode.RECOVER);
         }
      };
      recoverAction.setName(Messages.get().container("GUI_PLUGINS_LIST_ACTION_RECOVER_NAME_0"));
      recoverAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_ACTION_RECOVER_HELP_0"));
      recoverAction.setIconPath("tools/ocee-vfsdoctor/buttons/recover.png");
      recoverCol.addDirectAction(recoverAction);
      CmsListDirectAction disabledRecoverAction = new CmsListDirectAction("ard") {
         public boolean isVisible() {
            return !CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(this.getItem().getId()).canRunInMode(CmsVfsDoctorPluginExeMode.RECOVER);
         }
      };
      disabledRecoverAction.setName(Messages.get().container("GUI_PLUGINS_LIST_ACTION_NO_REC_NAME_0"));
      disabledRecoverAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_ACTION_NO_REC_HELP_0"));
      disabledRecoverAction.setIconPath("tools/ocee-vfsdoctor/buttons/recover.png");
      disabledRecoverAction.setEnabled(false);
      recoverCol.addDirectAction(disabledRecoverAction);
      metadata.addColumn(recoverCol);
      CmsListColumnDefinition validateCol = new CmsListColumnDefinition("cv");
      validateCol.setName(Messages.get().container("GUI_PLUGINS_LIST_COLS_VALIDATE_0"));
      validateCol.setWidth("20");
      validateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
      CmsListDirectAction validateAction = new CmsListDirectAction("av") {
         public boolean isVisible() {
            return CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(this.getItem().getId()).canRunInMode(CmsVfsDoctorPluginExeMode.VALIDATE);
         }
      };
      validateAction.setName(Messages.get().container("GUI_PLUGINS_LIST_ACTION_VALIDATE_NAME_0"));
      validateAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_ACTION_VALIDATE_HELP_0"));
      validateAction.setIconPath("tools/ocee-vfsdoctor/buttons/validate.png");
      validateCol.addDirectAction(validateAction);
      CmsListDirectAction disabledValidateAction = new CmsListDirectAction("avd") {
         public boolean isVisible() {
            return !CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(this.getItem().getId()).canRunInMode(CmsVfsDoctorPluginExeMode.VALIDATE);
         }
      };
      disabledValidateAction.setName(Messages.get().container("GUI_PLUGINS_LIST_ACTION_NO_VAL_NAME_0"));
      disabledValidateAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_ACTION_NO_VAL_HELP_0"));
      disabledValidateAction.setIconPath("tools/ocee-vfsdoctor/buttons/validate.png");
      disabledValidateAction.setEnabled(false);
      validateCol.addDirectAction(disabledValidateAction);
      metadata.addColumn(validateCol);
      CmsListColumnDefinition nameCol = new CmsListColumnDefinition("cn");
      nameCol.setName(Messages.get().container("GUI_PLUGINS_LIST_COLS_NAME_0"));
      nameCol.setWidth("100%");
      CmsListDefaultAction validateDefAction = new CmsListDefaultAction("dv") {
         public boolean isVisible() {
            return CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(this.getItem().getId()).canRunInMode(CmsVfsDoctorPluginExeMode.VALIDATE);
         }
      };
      validateDefAction.setName(Messages.get().container("GUI_PLUGINS_LIST_DEFACTION_VALIDATE_NAME_0"));
      validateDefAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_DEFACTION_VALIDATE_HELP_0"));
      nameCol.addDefaultAction(validateDefAction);
      CmsListDefaultAction disabledValidateDefAction = new CmsListDefaultAction("dvd") {
         public boolean isVisible() {
            return !CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(this.getItem().getId()).canRunInMode(CmsVfsDoctorPluginExeMode.VALIDATE);
         }
      };
      disabledValidateDefAction.setName(Messages.get().container("GUI_PLUGINS_LIST_DEFACTION_NO_VAL_NAME_0"));
      disabledValidateDefAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_DEFACTION_NO_VAL_HELP_0"));
      disabledValidateDefAction.setEnabled(false);
      nameCol.addDirectAction(disabledValidateDefAction);
      metadata.addColumn(nameCol);
   }

   protected void setIndependentActions(CmsListMetadata metadata) {
      CmsListItemDetails infoDetails = new CmsListItemDetails("di");
      infoDetails.setAtColumn("cn");
      infoDetails.setVisible(false);
      infoDetails.setShowActionName(Messages.get().container("GUI_PLUGINS_DETAIL_SHOW_INFO_NAME_0"));
      infoDetails.setShowActionHelpText(Messages.get().container("GUI_PLUGINS_DETAIL_SHOW_INFO_HELP_0"));
      infoDetails.setHideActionName(Messages.get().container("GUI_PLUGINS_DETAIL_HIDE_INFO_NAME_0"));
      infoDetails.setHideActionHelpText(Messages.get().container("GUI_PLUGINS_DETAIL_HIDE_INFO_HELP_0"));
      infoDetails.setName(Messages.get().container("GUI_PLUGINS_DETAIL_INFO_NAME_0"));
      infoDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container("GUI_PLUGINS_DETAIL_INFO_NAME_0")));
      metadata.addItemDetails(infoDetails);
   }

   protected void setMultiActions(CmsListMetadata metadata) {
      CmsListMultiAction validateAction = new CmsListMultiAction("mv");
      validateAction.setName(Messages.get().container("GUI_PLUGINS_LIST_MACTION_VALIDATE_NAME_0"));
      validateAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_MACTION_VALIDATE_HELP_0"));
      validateAction.setConfirmationMessage(Messages.get().container("GUI_PLUGINS_LIST_MACTION_VALIDATE_CONF_0"));
      validateAction.setIconPath("tools/ocee-vfsdoctor/buttons/multi_validate.png");
      metadata.addMultiAction(validateAction);
      CmsListMultiAction recoverAction = new CmsListMultiAction("mr");
      recoverAction.setName(Messages.get().container("GUI_PLUGINS_LIST_MACTION_RECOVER_NAME_0"));
      recoverAction.setHelpText(Messages.get().container("GUI_PLUGINS_LIST_MACTION_RECOVER_HELP_0"));
      recoverAction.setConfirmationMessage(Messages.get().container("GUI_PLUGINS_LIST_MACTION_RECOVER_CONF_0"));
      recoverAction.setIconPath("tools/ocee-vfsdoctor/buttons/multi_recover.png");
      metadata.addMultiAction(recoverAction);
   }
}
