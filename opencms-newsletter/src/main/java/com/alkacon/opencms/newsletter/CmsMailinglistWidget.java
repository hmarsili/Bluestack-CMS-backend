package com.alkacon.opencms.newsletter;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

public class CmsMailinglistWidget extends CmsGroupWidget {
   public CmsMailinglistWidget() {
      this("");
   }

   public CmsMailinglistWidget(Integer flags, String userName) {
      super(flags, userName);
   }

   public CmsMailinglistWidget(Integer flags, String userName, String ouFqn) {
      super(flags, userName, ouFqn);
   }

   public CmsMailinglistWidget(String configuration) {
      super(configuration);
   }

   public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
      String id = param.getId();
      StringBuffer result = new StringBuffer(128);
      result.append("<td class=\"xmlTd\">");
      result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td style=\"width: 100%;\">");
      result.append("<input style=\"width: 99%;\" class=\"xmlInput");
      if (param.hasError()) {
         result.append(" xmlInputError");
      }

      result.append("\" value=\"");
      result.append(param.getStringValue(cms));
      result.append("\" name=\"");
      result.append(id);
      result.append("\" id=\"");
      result.append(id);
      result.append("\"></td>");
      result.append(widgetDialog.dialogHorizontalSpacer(10));
      result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
      StringBuffer buttonJs = new StringBuffer(8);
      buttonJs.append("javascript:openGroupWin('");
      buttonJs.append(OpenCms.getSystemInfo().getOpenCmsContext());
      buttonJs.append("/system/modules/");
      buttonJs.append(CmsNewsletterManager.MODULE_NAME);
      buttonJs.append("/widgets/mailinglist.jsp");
      buttonJs.append("','EDITOR',  '");
      buttonJs.append(id);
      buttonJs.append("', document, ");
      if (this.getFlags() != null) {
         buttonJs.append("'");
         buttonJs.append(this.getFlags());
         buttonJs.append("'");
      } else {
         buttonJs.append("null");
      }

      buttonJs.append(", ");
      if (this.getUserName() != null) {
         buttonJs.append("'");
         buttonJs.append(this.getUserName());
         buttonJs.append("'");
      } else {
         buttonJs.append("null");
      }

      buttonJs.append(", ");
      if (this.getOufqn() != null) {
         buttonJs.append("'");
         buttonJs.append(this.getOufqn());
         buttonJs.append("'");
      } else {
         buttonJs.append("null");
      }

      buttonJs.append(");");
      result.append(widgetDialog.button(buttonJs.toString(), (String)null, "mailinglist", "GUI_DIALOG_BUTTON_SEARCH_0", widgetDialog.getButtonStyle()));
      result.append("</tr></table>");
      result.append("</td></tr></table>");
      result.append("</td>");
      return result.toString();
   }

   public I_CmsWidget newInstance() {
      return new CmsMailinglistWidget(this.getConfiguration());
   }
}
