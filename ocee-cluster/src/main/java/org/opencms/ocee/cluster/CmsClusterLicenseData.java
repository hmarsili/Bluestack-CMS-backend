package org.opencms.ocee.cluster;

import org.opencms.util.CmsStringUtil;

public final class CmsClusterLicenseData {
   public static final String TYPE_NOACCESS = "No accessible";
   private String m_activationKey = "";
   private String m_distribution = "";
   private String m_formatedTimeLeft;
   private String m_name = "";
   private long m_timeLeft;
   private long m_timestamp = -1L;
   private final String m_type;

   public CmsClusterLicenseData(String type) {
      this.m_type = type;
   }

   public String getActivationKey() {
      return this.m_activationKey;
   }

   public String getDistribution() {
      return this.m_distribution;
   }

   public String getFormatedTimeLeft() {
      if (this.m_formatedTimeLeft == null) {
         if (this.getTimeLeft() >= 86400000L) {
            long days = this.getTimeLeft() / 86400000L;
            this.m_formatedTimeLeft = Messages.get().getBundle().key("GUI_LICENSE_TIMELEFT_DAYS_1", new Object[]{new Long(days)});
         } else if (this.getTimeLeft() > 0L) {
            this.m_formatedTimeLeft = CmsStringUtil.formatRuntime(this.getTimeLeft()).replaceFirst(":", "h").replaceFirst(":", "m") + "s";
         } else if (this.getTimeLeft() < 0L) {
            this.m_formatedTimeLeft = Messages.get().getBundle().key("GUI_LICENSE_EXPIRED_0");
         } else {
            this.m_formatedTimeLeft = "";
         }
      }

      return this.m_formatedTimeLeft;
   }

   public String getKey() {
      return this.m_type;
   }

   public String getName() {
      return this.m_name;
   }

   public long getTimeLeft() {
      return this.m_timeLeft;
   }

   public String getType() {
      return !this.isAccessible() ? Messages.get().getBundle().key("GUI_LICENSE_TYPE_NOACCESS_0") : Messages.get().getBundle().key("GUI_LICENSE_TYPE_FORMAT_1", new Object[]{this.m_type});
   }

   public int getWidgetCount() {
      if (!this.isAccessible()) {
         return 1;
      } else {
         int wc = 3;
         if (!this.isActivated()) {
            ++wc;
         }

         if (this.isTimeLimited()) {
            ++wc;
         }

         return wc;
      }
   }

   public boolean hasTimestampOlderThan(long duration) {
      if (this.m_timestamp <= 0L) {
         return false;
      } else {
         boolean result = this.m_timestamp + duration < System.currentTimeMillis();
         return result;
      }
   }

   public boolean isAccessible() {
      return !this.m_type.equals("No accessible");
   }

   public boolean isActivated() {
      return this.m_type.equals("Production") || this.m_type.equals("Development");
   }

   public boolean isTimeLimited() {
      return this.m_type.equals("Evaluation") || this.m_type.equals("Development");
   }

   public void setActivationKey(String key) {
      this.m_activationKey = key;
   }

   public void setDistribution(String distribution) {
      this.m_distribution = distribution;
   }

   public void setFormatedTimeLeft(String formatedTimeLeft) {
      formatedTimeLeft.length();
   }

   public void setName(String name) {
      this.m_name = name;
   }

   public void setTimeLeft(long leftTime) {
      this.m_timeLeft = leftTime;
   }

   public void setTimestamp(long timestamp) {
      this.m_timestamp = timestamp;
   }

   public void setType(String type) {
      type.length();
   }

   public boolean validateAgainst(CmsClusterLicenseData master) {
      boolean val = this.getType().equals(master.getType());
      val = val && this.getName().equals(master.getName());
      val = val && this.getDistribution().equals(master.getDistribution());
      val = val && this.getTimeLeft() >= 0L;
      return val;
   }

   public String validationDebugString() {
      return "[type:" + this.getType() + ", name:" + this.getName() + ", dist:" + this.getDistribution() + ", timeleft:" + this.getTimeLeft() + "]";
   }
}
