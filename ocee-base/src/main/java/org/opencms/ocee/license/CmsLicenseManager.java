package org.opencms.ocee.license;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;

public final class CmsLicenseManager {
   public static final String TYPE_DEVELOPMENT = "Development";
   public static final String TYPE_EVALUATION = "Evaluation";
   public static final String TYPE_NONE = "No";
   public static final String TYPE_PRODUCT = "Production";
   private CmsLicenseConfiguration Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   private boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   private static final Log Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = CmsLog.getLog(CmsLicenseManager.class);

   public static CmsLicenseManager getInstance() {
      try {
         return (CmsLicenseManager)CmsOceeManager.getInstance().getClassLoader().loadObject(CmsLicenseManager.class.getName());
      } catch (Exception var1) {
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(var1);
         return null;
      }
   }

   public CmsLicenseConfiguration getConfiguration() {
      if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object == null) {
         this.initialize((CmsLicenseConfiguration)CmsCoreProvider.getInstance().getConfigurationManager().getConfiguration(CmsLicenseConfiguration.class));
      }

      return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
   }

   public long getDevExpirationDate() {
      return OpenCms.getSystemInfo().getStartupTime() + (long)(this.getConfiguration().getDevelopmentTime() * 60 * 60) * 1000L;
   }

   public long getEvaluationDaysLeft() {
      return 1L + (this.getExpirationDate() - System.currentTimeMillis()) / 86400000L;
   }

   public String getEvaluationName() {
      if (this.isEvaluation() == null) {
         return "";
      } else {
         return this.isEvaluation() ? "GUI_LICENSE_PROVISIONAL_KEY_0" : "GUI_LICENSE_EVALUATION_KEY_0";
      }
   }

   public long getExpirationDate() {
      return this.getConfiguration().getExpirationDate();
   }

   public String getFormatedDevExpirationDate(Locale locale) {
      return locale != null ? DateFormat.getDateTimeInstance(2, 2, locale).format(new Date(this.getDevExpirationDate())) : DateFormat.getDateTimeInstance(2, 2).format(new Date(this.getDevExpirationDate()));
   }

   public String getFormatedExpDate(Locale locale) {
      return locale != null ? DateFormat.getDateTimeInstance(2, 2, locale).format(new Date(this.getExpirationDate())) : DateFormat.getDateTimeInstance(2, 2).format(new Date(this.getExpirationDate()));
   }

   public String getFormattedLicenseDate(Locale locale) {
      return locale != null ? DateFormat.getDateTimeInstance(2, 2, locale).format(new Date(this.getConfiguration().getLicenseDate())) : DateFormat.getDateTimeInstance(2, 2).format(new Date(this.getConfiguration().getLicenseDate()));
   }

   public void initialize(CmsLicenseConfiguration configuration) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = true;
      this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = configuration;
   }

   public boolean isActivated() {
      return this.getConfiguration().getLicenseDate() != 0L;
   }

   public boolean isDevelopmentVersion() {
      return this.getConfiguration().getActivationKey().startsWith("Development".toUpperCase());
   }

   public Boolean isEvaluation() {
      return this.getConfiguration().isEvaluation();
   }

   public boolean isInitialized() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public void writeConfiguration() {
      OpenCms.writeConfiguration(this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.getClass());
   }
}
