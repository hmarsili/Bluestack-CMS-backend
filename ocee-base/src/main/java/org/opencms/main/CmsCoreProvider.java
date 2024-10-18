package org.opencms.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.I_CmsDbContextFactory;
import org.opencms.file.CmsRequestContext;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.util.CmsStringUtil;

public final class CmsCoreProvider {
   private static CmsCoreProvider coreProvider = null;

   private CmsCoreProvider() {
   }

   public static CmsCoreProvider getInstance() {
      if (coreProvider == null) {
         coreProvider = new CmsCoreProvider();
      }

      return coreProvider;
   }

   public List getAvailableDbPoolNames() {
      CmsConfigurationManager manager = this.getConfigurationManager();
      if (manager == null) {
         return new ArrayList();
      } else {
         Object val = manager.getConfiguration().get("db.pools");
         return val instanceof String ? CmsStringUtil.splitAsList((String)val, ',') : (List)val;
      }
   }

   public CmsConfigurationManager getConfigurationManager() {
      return OpenCmsCore.getInstance().getConfigurationManager();
   }

   public String getDbName() {
      Map config = this.getConfigurationManager().getConfiguration();
      return (String)config.get("db.name");
   }

   public CmsDbContext getNewDbContext(CmsRequestContext reqContext) {
      return reqContext == null ? this.getRuntimeInfoFactory().getDbContext() : this.getRuntimeInfoFactory().getDbContext(reqContext);
   }

   public String getPoolUrl(String poolName) {
      try {
         return this.getConfigurationManager().getConfiguration().get("db.pool." + poolName + "." + "poolUrl").toString().trim();
      } catch (Throwable var3) {
         return "opencms:default";
      }
   }

   public I_CmsRequestHandler getRequestHandler(String className) {
      CmsSystemConfiguration systemConfiguration = (CmsSystemConfiguration)this.getConfigurationManager().getConfiguration(CmsSystemConfiguration.class);
      Iterator it = systemConfiguration.getRequestHandlers().iterator();

      I_CmsRequestHandler handler;
      do {
         if (!it.hasNext()) {
            return null;
         }

         handler = (I_CmsRequestHandler)it.next();
      } while(!handler.getClass().getName().equals(className));

      return handler;
   }

   public I_CmsDbContextFactory getRuntimeInfoFactory() {
      CmsSystemConfiguration systemConfiguration = (CmsSystemConfiguration)this.getConfigurationManager().getConfiguration(CmsSystemConfiguration.class);
      return systemConfiguration.getRuntimeInfoFactory();
   }

   public String getSequenceForDriver(String driver) {
      Map config = this.getConfigurationManager().getConfiguration();
      Object val = config.get("driver." + driver);
      return val != null ? val.toString() : "";
   }

   public String getSqlManagerClassnameForDriver(String driver) {
      Map config = this.getConfigurationManager().getConfiguration();
      return (String)config.get("db." + driver + ".sqlmanager");
   }

   public String getVersionNumber() {
      return CmsOceeManager.getInstance().getVersionNumber();
   }
}
