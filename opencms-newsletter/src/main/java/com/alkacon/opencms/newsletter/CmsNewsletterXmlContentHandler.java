package com.alkacon.opencms.newsletter;

import java.util.Locale;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class CmsNewsletterXmlContentHandler extends CmsDefaultXmlContentHandler {
   private static final String MACRO_DOMAIN = "domain";

   public CmsNewsletterXmlContentHandler() {
      this.init();
   }

   public String getDefault(CmsObject cms, I_CmsXmlContentValue value, Locale locale) {
      String defaultValue;
      if (value.getElement() == null) {
         defaultValue = value.getDefault(locale);
      } else {
         String xpath = value.getPath();
         defaultValue = (String)this.m_defaultValues.get(xpath);
         if (defaultValue == null) {
            xpath = CmsXmlUtils.removeXpath(xpath);
            xpath = CmsXmlUtils.createXpath(xpath, 1);
            defaultValue = (String)this.m_defaultValues.get(xpath);
         }
      }

      if (defaultValue != null) {
         CmsObject newCms = cms;

         CmsSite site;
         try {
            CmsResource file = value.getDocument().getFile();
            site = OpenCms.getSiteManager().getSiteForRootPath(file.getRootPath());
            if (site != null) {
               newCms = OpenCms.initCmsObject(cms);
               newCms.getRequestContext().setSiteRoot(site.getSiteRoot());
               newCms.getRequestContext().setUri(newCms.getSitePath(file));
            }
         } catch (Exception var9) {
         }

         CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(newCms).setMessages(this.getMessages(locale));
         site = OpenCms.getSiteManager().getSiteForSiteRoot(cms.getRequestContext().getSiteRoot());
         String serverName = "";
         if (site != null) {
            serverName = site.getSiteMatcher().getServerName();
         }

         if (!CmsStringUtil.isEmptyOrWhitespaceOnly(serverName) && !"*".equals(serverName)) {
            if (serverName.startsWith("www.")) {
               serverName = serverName.substring(4);
            }
         } else {
            serverName = "yourdomain.com";
         }

         resolver.addMacro("domain", serverName);
         resolver.setKeepEmptyMacros(true);
         return resolver.resolveMacros(defaultValue);
      } else {
         return null;
      }
   }
}
