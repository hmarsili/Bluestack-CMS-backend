package com.alkacon.opencms.newsletter.admin;

import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;

public class CmsListSendNewsletterAction extends CmsListDirectAction {
   private final String m_resColumnPathId;
   private CmsResourceUtil m_resourceUtil;

   public CmsListSendNewsletterAction(String id, String resColumnPathId) {
      super(id);
      this.m_resColumnPathId = resColumnPathId;
   }

   public CmsMessageContainer getHelpText() {
      CmsMessageContainer helptext = super.getHelpText();
      if (helptext == null) {
         if (this.isEnabled()) {
            helptext = Messages.get().container("GUI_NEWSLETTER_LIST_ACTION_SEND_HELP_0");
         } else {
            helptext = Messages.get().container("GUI_NEWSLETTER_LIST_ACTION_SEND_DISABLED_HELP_0");
         }
      }

      return helptext;
   }

   public String getIconPath() {
      String iconpath = super.getIconPath();
      if (iconpath == null) {
         if (this.isEnabled()) {
            iconpath = "tools/newsletter/buttons/newsletter_send.png";
         } else {
            iconpath = "tools/newsletter/buttons/newsletter_send_disabled.png";
         }
      }

      return iconpath;
   }

   public CmsMessageContainer getName() {
      CmsMessageContainer name = super.getName();
      if (name == null) {
         if (this.isEnabled()) {
            name = Messages.get().container("GUI_NEWSLETTER_LIST_ACTION_SEND_0");
         } else {
            name = Messages.get().container("GUI_NEWSLETTER_LIST_ACTION_SEND_DISABLED_0");
         }
      }

      return name;
   }

   public boolean isVisible() {
      if (this.getResourceName() != null) {
         try {
            CmsLock lock = this.getResourceUtil().getLock();
            boolean isUnChanged = this.getResourceUtil().getResource().getState().isUnchanged();
            if (isUnChanged && (lock.isNullLock() || lock.isOwnedBy(this.getWp().getCms().getRequestContext().currentUser()))) {
               return this.isEnabled();
            }
         } catch (Throwable var3) {
         }
      }

      return !this.isEnabled();
   }

   public void setItem(CmsListItem item) {
      this.m_resourceUtil = ((A_CmsListExplorerDialog)this.getWp()).getResourceUtil(item);
      super.setItem(item);
   }

   protected CmsResourceUtil getResourceUtil() {
      return this.m_resourceUtil;
   }

   private String getResourceName() {
      String resource = this.getItem().get(this.m_resColumnPathId).toString();
      if (!this.getWp().getCms().existsResource(resource, CmsResourceFilter.DEFAULT)) {
         String siteRoot = OpenCms.getSiteManager().getSiteRoot(resource);
         if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(siteRoot)) {
            resource = resource.substring(siteRoot.length());
         }

         if (!this.getWp().getCms().existsResource(resource, CmsResourceFilter.DEFAULT)) {
            resource = null;
         }
      }

      return resource;
   }
}
