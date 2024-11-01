package com.alkacon.opencms.newsletter.admin;

import com.alkacon.opencms.newsletter.CmsNewsletterManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;

public class CmsNewsletterResourcesCollector extends A_CmsListResourceCollector {
   public static final String COLLECTOR_NAME = "newsletterresources";

   public CmsNewsletterResourcesCollector(A_CmsListExplorerDialog wp) {
      super(wp);
   }

   public List getCollectorNames() {
      List names = new ArrayList();
      names.add("newsletterresources");
      return names;
   }

   public List getResources(CmsObject cms, Map params) throws CmsException {
      String typeName = "alkacon-newsletter";

      try {
         typeName = CmsNewsletterManager.getMailDataResourceTypeName();
      } catch (Exception var6) {
      }

      int typeId = OpenCms.getResourceManager().getResourceType(typeName).getTypeId();
      CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(typeId);
      return cms.readResources("/", filter, true);
   }

   protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {
      String value = "";

      try {
         CmsProperty property = resUtil.getCms().readPropertyObject((String)item.get("ecn"), "newsletter", false);
         value = property.getValue();
         if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            List vals = CmsStringUtil.splitAsList(value, '|');
            Date date = new Date(Long.parseLong((String)vals.get(0)));
            String groupId = (String)vals.get(1);
            String groupName = "";

            try {
               CmsGroup group = resUtil.getCms().readGroup(new CmsUUID(groupId));
               groupName = group.getSimpleName();
            } catch (CmsException var11) {
               groupName = Messages.get().getBundle(this.getWp().getLocale()).key("GUI_NEWSLETTER_LIST_DATA_SEND_GROUPDUMMY_0");
            }

            value = Messages.get().getBundle(this.getWp().getLocale()).key("GUI_NEWSLETTER_LIST_DATA_SEND_AT_2", date, groupName);
         } else {
            value = Messages.get().getBundle(this.getWp().getLocale()).key("GUI_NEWSLETTER_LIST_DATA_SEND_NEVER_0");
         }
      } catch (CmsException var12) {
      }

      try {
         item.set("ecda", value);
      } catch (CmsIllegalArgumentException var10) {
      }

   }
}
